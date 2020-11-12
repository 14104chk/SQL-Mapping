package ssll.rsm.fn;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import ssll.rsm.FunctionChain;
import ssll.rsm.Label;
import ssll.rsm.MappingContext;
import ssll.rsm.MappingException;
import ssll.rsm.Property;

import static ssll.rsm.VirtualObject.UNKNOWN_ENTITY;

public class EntryFunction implements Function {

	private final Map<String, Map> map = new HashMap<>();

	public EntryFunction(MappingContext context, String[] args) throws MappingException {
	}

	@Override
	public void execute(MappingContext context, Object parentEntity, Label label, FunctionChain chain) throws MappingException, SQLException {
		if (chain.getEntity() != UNKNOWN_ENTITY) {
			return;
		}
		Object entity = chain.getEntity();
		if (entity == UNKNOWN_ENTITY) {
			String id = "";
			for (Property property : label.getProperties()) {
				if (property.getName().equals("id")) {
					id = (String) property.readValue(context, String.class);
				}
			}
			Map _entity = map.get(id);
			if (_entity == null) {
				_entity = new HashMap<>();
				map.put(id, _entity);
			}
			entity = _entity;
			chain.setEntity(entity);
		}
		if (entity instanceof Map) {
			Object k = null, v = null;
			for (Property property : label.getProperties()) {
				switch (property.getName()) {
					case "k":
						k = property.readValue(context, null);
						break;
					case "v":
						v = property.readValue(context, null);
						break;
				}
			}
			if (k != null) {
				((Map) entity).put(k, v);
			}
		}
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
