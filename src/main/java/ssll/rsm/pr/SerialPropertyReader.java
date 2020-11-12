package ssll.rsm.pr;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import ssll.rsm.MappingContext;
import ssll.rsm.MappingException;
import ssll.rsm.Property;

public class SerialPropertyReader implements PropertyReader {

	@Override
	public Object read(MappingContext context, Property property, Class propertyRawType, ResultSet resultSet) throws SQLException {
		byte[] bytes = resultSet.getBytes(property.getColumnIndex());
		if (bytes == null) {
			return null;
		}
		try (ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bytes))) {
			return in.readObject();
		} catch (IOException | ClassNotFoundException ex) {
			throw new MappingException("Reading property:" + property.getText(), ex);
		}
	}

}
