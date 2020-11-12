package ssll.rsm;

import java.sql.SQLException;

public class Property implements Comparable<Property> {

	private final String text;
	private final int columnIndex;
	private final String name;
	private final String type;

	public Property(String text, int columnIndex, String name, String type) {
		this.text = text;
		this.columnIndex = columnIndex;
		this.name = name;
		this.type = type;
	}

	public Object readValue(MappingContext context, Class propertyRawType) throws SQLException {
		return context.getConfig().getPropertyReaders().readPropertyValue(context, this, propertyRawType, type);
	}

	@Override
	public int compareTo(Property o) {
		return columnIndex - o.columnIndex;
	}

	public String getText() {
		return text;
	}

	public int getColumnIndex() {
		return columnIndex;
	}

	public String getName() {
		return name;
	}

	public String getType() {
		return type;
	}

	@Override
	public String toString() {
		return String.format("%s[%s]", getClass().getName(), text);
	}

}
