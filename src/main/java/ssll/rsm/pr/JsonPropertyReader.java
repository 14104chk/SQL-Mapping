package ssll.rsm.pr;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import ssll.core.Json;
import ssll.rsm.MappingContext;
import ssll.rsm.MappingException;
import ssll.rsm.Property;

public class JsonPropertyReader implements PropertyReader {

	private final Json json;

	public JsonPropertyReader(Json json) {
		this.json = json;
	}

	@Override
	public Object read(MappingContext context, Property property, Class propertyRawType, ResultSet resultSet) throws SQLException {
		String text = resultSet.getString(property.getColumnIndex());
		if (resultSet.wasNull()) {
			return null;
		} else {
			try {
				return json.fromJson(text, propertyRawType == null ? Object.class : propertyRawType);
			} catch (IOException ex) {
				throw new MappingException("json:" + text, ex);
			}
		}
	}

}
