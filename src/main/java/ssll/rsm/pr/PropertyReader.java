package ssll.rsm.pr;

import java.sql.ResultSet;
import java.sql.SQLException;
import ssll.rsm.MappingContext;
import ssll.rsm.Property;

public interface PropertyReader {

	public static final PropertyReader UNKNOWN_TYPE_READER = (MappingContext context, Property property, Class propertyRawType, ResultSet resultSet) -> resultSet.getObject(property.getColumnIndex());

	public Object read(MappingContext context, Property property, Class propertyRawType, ResultSet resultSet) throws SQLException;
}
