package ssll.rsm.fn;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import ssll.rsm.BeanUtils;
import ssll.rsm.FunctionChain;
import ssll.rsm.Label;
import ssll.rsm.MappingContext;
import ssll.rsm.MappingException;
import ssll.rsm.Property;

import static ssll.rsm.VirtualObject.UNKNOWN_ENTITY;

public class EntityFunction extends WriteFunction {

	private String className;
	private Class type;

	public EntityFunction(MappingContext context, String[] args) throws MappingException {
		if (args.length <= 0) {
			this.className = "S";
		} else {
			this.className = args[0];
		}
	}

	public EntityFunction(Class type) {
		this.type = type;
	}

	@Override
	public void execute(MappingContext context, Object parentEntity, Label label, FunctionChain chain) throws MappingException, SQLException {
		if (chain.getEntity() != UNKNOWN_ENTITY) {
			return;
		}
		BeanUtils beanUtils = context.getConfig().getBeanUtils();
		Object entity;
		if ("S".equals(className)) {
			Property property = label.getProperties().get(0);
			entity = property.readValue(context, null);
		} else {
			entity = type != null ? beanUtils.newInstance(type) : beanUtils.newInstance(className);
			writeEntityProperty(context, label, entity);
		}
		chain.setEntity(entity);
		chain.doNext();
	}

	@Override
	public List<Feature> features() {
		return Arrays.asList(Feature.BUILD_ENTITY);
	}

	@Override
	public int order() {
		return 300;
	}
}
