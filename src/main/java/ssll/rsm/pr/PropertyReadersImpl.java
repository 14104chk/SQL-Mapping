package ssll.rsm.pr;

import java.io.InputStream;
import java.io.Reader;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import ssll.core.Json;
import ssll.rsm.MappingContext;
import ssll.rsm.MappingException;
import ssll.rsm.Property;

public class PropertyReadersImpl implements PropertyReaders {

	private static final Pattern P_PROPERTY = Pattern.compile("(.+)__([a-zA-Z0-9]+)");
	protected Map<Object, PropertyReader> propertyReaders = new HashMap<>();

	public PropertyReadersImpl() {
		registerBasePropertyReader();
		Json json = Json.newInstance();
		if (json != null) {
			this.registerPropertyReader("json", new JsonPropertyReader(json));
		} else {
			this.registerPropertyReader("json", (MappingContext context, Property property, Class propertyRawType, ResultSet resultSet) -> {
									throw new SQLException("Json.newInstance() is null");
								});
		}
		this.registerPropertyReader("serial", new SerialPropertyReader());
		this.registerPropertyReader("", PropertyReader.UNKNOWN_TYPE_READER);
	}

	protected void registerBasePropertyReader() {
		Map<Class, String> map = new HashMap<>();
		map.put(BigDecimal.class, "getBigDecimal");
		map.put(InputStream.class, "getBinaryStream");
		map.put(Blob.class, "getBlob");
		map.put(Boolean.class, "getBoolean");
		map.put(Boolean.TYPE, "getBoolean");
		map.put(Byte.class, "getByte");
		map.put(Byte.TYPE, "getByte");
		map.put(byte[].class, "getBytes");
		map.put(Reader.class, "getCharacterStream");
		map.put(Clob.class, "getClob");
		map.put(java.sql.Date.class, "getDate");
		map.put(Double.TYPE, "getDouble");
		map.put(Double.class, "getDouble");
		map.put(Float.class, "getFloat");
		map.put(Float.TYPE, "getFloat");
		map.put(Integer.class, "getInt");
		map.put(Integer.TYPE, "getInt");
		map.put(Long.class, "getLong");
		map.put(Long.TYPE, "getLong");
		map.put(String.class, "getString");
		map.put(java.sql.Time.class, "getTime");
		map.put(java.sql.Timestamp.class, "getTimestamp");
		//
		map.put(java.util.Date.class, "getTimestamp");
		map.put(Object.class, "getObject");
		//
		try {
			for (Map.Entry<Class, String> t : map.entrySet()) {
				Class type = t.getKey();
				Method m = ResultSet.class.getMethod(t.getValue(), Integer.TYPE);
				PropertyReader reader = new BasePropertyReader(type, m);
				propertyReaders.put(type, reader);
				propertyReaders.put(type.getName(), reader);
				if (type == String.class) {
					propertyReaders.put("string", reader);
				}
			}
		} catch (NoSuchMethodException | SecurityException ex) {
			throw new RuntimeException(ex);
		}
	}

	public PropertyReader registerPropertyReader(String type, PropertyReader propertyReader) {
		return this.propertyReaders.put(type, propertyReader);
	}

	public PropertyReader deregisterPropertyReader(String type) {
		return this.propertyReaders.remove(type);
	}

	@Override
	public Property buildProperty(String text, int columnIndex, String name, String type) throws SQLException {
		if (type.isEmpty()) {
			Matcher m = P_PROPERTY.matcher(name);
			if (m.matches()) {
				if (propertyReaders.containsKey(m.group(2))) {
					name = m.group(1);
					type = m.group(2);
				}
			}
		}
		return new Property(text, columnIndex, name, type);
	}

	@Override
	public Object readPropertyValue(MappingContext context, Property property, Class propertyRawType, String type) throws SQLException {
		PropertyReader reader = propertyReaders.get(type);
		if (reader != null) {
			return reader.read(context, property, propertyRawType, context.getResultSet());
		}
		Class classType = context.getConfig().getBeanUtils().findClass(type);
		return readPropertyValue(context, property, classType);
	}

	protected Object readEnumPropertyValue(MappingContext context, Property property, Class<? extends Enum> type) throws SQLException {
		String name = context.getResultSet().getString(property.getColumnIndex());
		return name == null ? null : Enum.valueOf(type, name);
	}

	@Override
	public Object readPropertyValue(MappingContext context, Property property, Class propertyRawType) throws SQLException {
		if (propertyRawType.isEnum()) {
			return readEnumPropertyValue(context, property, propertyRawType);
		}
		PropertyReader reader = propertyReaders.get(propertyRawType);
		if (reader == null) {
			throw new MappingException(String.format("[%s] is unsupported", propertyRawType));
		}
		return reader.read(context, property, propertyRawType, context.getResultSet());
	}

}
