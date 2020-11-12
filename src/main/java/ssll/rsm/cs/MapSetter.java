package ssll.rsm.cs;

import java.sql.SQLException;
import java.util.Map;
import ssll.rsm.MappingContext;
import ssll.rsm.MappingException;
import ssll.rsm.Property;

public class MapSetter implements ColumnSetter {

	@Override
	public boolean set(MappingContext context, Object entity, Property property) throws MappingException, SQLException {
		if (!(entity instanceof Map)) {
			return false;
		}
		((Map) entity).put(property.getName(), property.readValue(context, null));
		return true;
	}

}
