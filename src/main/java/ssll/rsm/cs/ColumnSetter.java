package ssll.rsm.cs;

import java.sql.SQLException;
import ssll.rsm.MappingContext;
import ssll.rsm.Property;

public interface ColumnSetter {

	public boolean set(MappingContext context, Object entity, Property property) throws SQLException;
}
