package ssll.rsm.fn;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import ssll.rsm.BeanUtils;
import ssll.rsm.FunctionChain;
import ssll.rsm.Label;
import ssll.rsm.MappingContext;
import ssll.rsm.MappingException;
import ssll.rsm.VirtualObject;

public class ListFunction implements Function {

	private String className;

	public ListFunction() {
	}

	public ListFunction(MappingContext context, String[] args) {
		if (args.length <= 0) {
		} else {
			this.className = args[0];
		}
	}

	private List newList(MappingContext context) throws MappingException {
		if (className == null) {
			return context.getConfig().buildMappingResult();
		}
		return (List) context.getConfig().getBeanUtils().newInstance(className);
	}

	@Override
	public void execute(MappingContext context, Object parentEntity, Label label, FunctionChain chain) throws MappingException, SQLException {
		BeanUtils beanUtils = context.getConfig().getBeanUtils();
		String propertyName = label.getPropertyName();
		List list;
		if (propertyName.isEmpty()) {
			if ((list = (List) context.getResult()) == null) {
				list = newList(context);
				context.setResult(list);
			}
		} else {
			if ((list = (List) beanUtils.getProperty(parentEntity, propertyName)) == null) {
				list = newList(context);
				beanUtils.setProperty(parentEntity, propertyName, list);
			}
		}
		//
		chain.doNext();
		//
		Object entity = chain.getEntity();
		if (!(entity instanceof VirtualObject)) {
			if (!list.contains(entity)) {
				list.add(entity);
			}
		}
	}

	@Override
	public List<Feature> features() {
		return Arrays.asList(Feature.SET_ENTITY);
	}

	@Override
	public int order() {
		return 100;
	}

}
