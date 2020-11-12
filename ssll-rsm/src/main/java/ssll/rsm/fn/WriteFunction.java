package ssll.rsm.fn;

import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import ssll.rsm.FunctionChain;
import ssll.rsm.Label;
import ssll.rsm.MappingContext;
import ssll.rsm.MappingException;
import ssll.rsm.Property;
import ssll.rsm.VirtualObject;
import ssll.rsm.cs.ColumnSetter;

public class WriteFunction implements Function {

	protected Set _cantSet;

	public WriteFunction() {
	}

	public WriteFunction(MappingContext context, String[] args) throws MappingException {
	}

	@Override
	public void execute(MappingContext context, Object parentEntity, Label label, FunctionChain chain) throws MappingException, SQLException {
		Object entity = chain.getEntity();
		if (entity instanceof VirtualObject) {
			return;
		}
		writeEntityProperty(context, label, entity);
		chain.doNext();
	}

	protected void writeEntityProperty(MappingContext context, Label label, Object entity) throws MappingException, SQLException {
		write_property:
		for (Property property : label.getProperties()) {
			for (ColumnSetter setter : context.getConfig().getColumnSetters()) {
				if (setter.set(context, entity, property)) {
					continue write_property;
				}
			}
		}
	}

	protected Set getCantSet() {
		if (_cantSet == null) {
			_cantSet = new HashSet<>();
		}
		return _cantSet;
	}

	@Override
	public int order() {
		return 350;
	}
}
