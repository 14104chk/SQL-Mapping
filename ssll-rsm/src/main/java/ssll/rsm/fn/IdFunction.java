package ssll.rsm.fn;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import ssll.core.SilentCloseable;
import ssll.rsm.FunctionChain;
import ssll.rsm.Label;
import ssll.rsm.MappingContext;
import ssll.rsm.MappingException;
import ssll.rsm.Property;
import ssll.rsm.VirtualObject;

import static ssll.rsm.VirtualObject.INVALID_ENTITY;
import static ssll.rsm.VirtualObject.UNKNOWN_ENTITY;

public class IdFunction implements Function {

	private static final ThreadLocal<Map<String, Map>> LOCAL = new ThreadLocal<>();
	private final String keyName;
	private Map caches;

	public IdFunction(MappingContext context, String[] args) throws MappingException {
		if (args.length <= 0) {
			keyName = "";
		} else {
			keyName = args[0];
		}
		if (args.length <= 1) {
			caches = new HashMap<>();
		} else {
			Map<String, Map> maps = LOCAL.get();
			if (maps == null) {
				throw new MappingException();
			}
			caches = maps.get(args[1]);
			if (caches == null) {
				maps.put(args[1], caches = new HashMap<>());
			}
		}
	}

	private Property findProperty(Label label) throws MappingException {
		List<Property> properties = label.getProperties();
		if (properties != null && !properties.isEmpty()) {
			for (Property t : label.getProperties()) {
				if (t.getName().equals(keyName)) {
					return t;
				}
			}
		}
		throw new MappingException();
	}

	@Override
	public void execute(MappingContext context, Object parentEntity, Label label, FunctionChain chain) throws MappingException, SQLException {
		Object key, value;
		if (keyName.isEmpty() || keyName.equals(".")) {
			key = "";
		} else {
			Property property = findProperty(label);
			key = property.readValue(context, null);
		}
		if (key == null) {
			value = INVALID_ENTITY;
		} else {
			value = caches.getOrDefault(key, UNKNOWN_ENTITY);
		}
		chain.setEntity(value);
		chain.doNext();
		if (value == UNKNOWN_ENTITY && !((value = chain.getEntity()) instanceof VirtualObject)) {
			caches.put(key, value);
		}
	}

	@Override
	public int order() {
		return 200;
	}

	public static SilentCloseable openLocal() {
		LOCAL.set(new HashMap<>());
		return () -> LOCAL.remove();
	}
}
