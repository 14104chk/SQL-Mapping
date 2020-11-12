package ssll.rsm.cs;

import java.sql.SQLException;

import ssll.rsm.BeanUtils;
import ssll.rsm.Mapping;
import ssll.rsm.MappingContext;
import ssll.rsm.Property;

public class PojoSetter implements ColumnSetter {

	@Override
	public boolean set(MappingContext context, Object entity, Property property) throws SQLException {
		BeanUtils bu = context.getConfig().getBeanUtils();
		String propertyName = bu.findAvailablePropertyName(entity, property);
		if (propertyName == null) {
			return false;
		}
		Object value;
		Class propertyRawType = bu.findPropertyRawType(entity, propertyName);
		Mapping mapping = bu.findPropertyAnnotation(entity, propertyName, Mapping.class);
		if (mapping != null && !mapping.propertyType().isEmpty()) {
			value = context.getConfig().getPropertyReaders().readPropertyValue(context, property, propertyRawType, mapping.propertyType());
		} else {
			value = context.getConfig().getPropertyReaders().readPropertyValue(context, property, propertyRawType);
		}
		bu.setProperty(entity, propertyName, value);
		return true;
	}

}
