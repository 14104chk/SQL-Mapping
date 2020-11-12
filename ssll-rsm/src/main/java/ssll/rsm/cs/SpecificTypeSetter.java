package ssll.rsm.cs;

import java.sql.SQLException;
import ssll.rsm.BeanUtils;
import ssll.rsm.MappingContext;
import ssll.rsm.Property;

public class SpecificTypeSetter implements ColumnSetter {

	@Override
	public boolean set(MappingContext context, Object entity, Property property) throws SQLException {
		if (property.getType().isEmpty()) {
			return false;
		}
		BeanUtils bu = context.getConfig().getBeanUtils();
		String propertyName = bu.findAvailablePropertyName(entity, property);
		bu.setProperty(entity, propertyName, property.readValue(context, null));
		return true;
	}

}
