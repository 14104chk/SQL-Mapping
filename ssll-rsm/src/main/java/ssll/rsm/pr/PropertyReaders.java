package ssll.rsm.pr;

import java.sql.SQLException;
import ssll.rsm.MappingContext;
import ssll.rsm.Property;

public interface PropertyReaders {

	Property buildProperty(String text, int columnIndex, String name, String type) throws SQLException;

	Object readPropertyValue(MappingContext context, Property property, Class propertyRawType, String type) throws SQLException;

	Object readPropertyValue(MappingContext context, Property property, Class propertyRawType) throws SQLException;

}
