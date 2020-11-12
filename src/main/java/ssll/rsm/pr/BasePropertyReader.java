package ssll.rsm.pr;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.sql.SQLException;
import ssll.rsm.MappingContext;
import ssll.rsm.MappingException;
import ssll.rsm.Property;

public class BasePropertyReader implements PropertyReader {

	private final Class type;
	private final Method method;

	public BasePropertyReader(Class type, Method method) {
		this.type = type;
		this.method = method;
		if (!ResultSet.class.isAssignableFrom(method.getDeclaringClass())) {
			throw new UnsupportedOperationException();
		}
	}

	@Override
	public Object read(MappingContext context, Property property, Class propertyRawType, ResultSet resultSet) throws SQLException {
		try {
			Object value = method.invoke(resultSet, property.getColumnIndex());
			if (!type.isPrimitive() && resultSet.wasNull()) {
				return null;
			}
			return value;
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
			throw new MappingException(ex);
		}
	}

}
