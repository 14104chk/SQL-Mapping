package ssll.rsm.cs;

import java.sql.SQLException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import ssll.rsm.MappingContext;
import ssll.rsm.Property;

import static ssll.core.LangUitls.getOrElse;

public class CachedColumnSetter implements ColumnSetter {

	protected final ConcurrentMap<Object, ColumnSetter> cache = new ConcurrentHashMap<>();
	protected final ColumnSetter NO = (MappingContext context, Object entity, Property property) -> false;
	protected final ColumnSetter[] columnSetters;

	public CachedColumnSetter(ColumnSetter... columnSetters) {
		this.columnSetters = columnSetters.clone();
	}

	@Override
	public boolean set(MappingContext context, Object entity, Property property) throws SQLException {
		String key = entity.getClass().getName() + ":" + property.getName();
		ColumnSetter setter = cache.get(key);
		if (setter == null) {
			for (ColumnSetter t : columnSetters) {
				if (t.set(context, entity, property)) {
					setter = t;
					break;
				}
			}
			cache.putIfAbsent(key, getOrElse(setter, NO));
			return setter != null;
		} else {
			return setter.set(context, entity, property);
		}
	}

}
