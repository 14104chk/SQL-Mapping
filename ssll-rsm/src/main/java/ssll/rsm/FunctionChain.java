package ssll.rsm;

import java.sql.SQLException;
import java.util.Queue;
import ssll.rsm.fn.Function;

import static ssll.rsm.VirtualObject.UNKNOWN_ENTITY;

public class FunctionChain {

	private final MappingContext context;
	private final Object parentEntity;
	private final Label label;
	private final Queue<Function> functions;
	private Object entity = UNKNOWN_ENTITY;

	public FunctionChain(MappingContext context, Object parentEntity, Label label, Queue<Function> functions) {
		this.context = context;
		this.parentEntity = parentEntity;
		this.label = label;
		this.functions = functions;
	}

	public void doNext() throws MappingException, SQLException {
		if (!functions.isEmpty()) {
			functions.remove().execute(context, parentEntity, label, this);
		}
	}

	public Object getParentEntity() {
		return parentEntity;
	}

	public Object getEntity() {
		return entity;
	}

	public void setEntity(Object entity) {
		if (entity == null) {
			throw new NullPointerException();
		}
		this.entity = entity;
	}

}
