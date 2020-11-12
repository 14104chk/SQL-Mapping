package ssll.rsm;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.Properties;
import org.apache.ibatis.executor.resultset.ResultSetHandler;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;

@Intercepts(
		@Signature(type = ResultSetHandler.class, method = "handleResultSets", args = {Statement.class}))
public class MyBatis3Interceptor implements Interceptor {

	protected final MappingConfig mapperConfig;

	public MyBatis3Interceptor(MappingConfig mapperConfig) {
		this.mapperConfig = mapperConfig;
	}

	@Override
	public Object intercept(Invocation invocation) throws Throwable {
		ResultSet rs = ((Statement) invocation.getArgs()[0]).getResultSet();
		ResultSetMetaData md = rs.getMetaData();
		for (int i = 1; i <= md.getColumnCount(); i++) {
			String columnLabel = md.getColumnLabel(i);
			if (columnLabel.startsWith(":") && Label.P_LABLE.matcher(columnLabel).matches()) {
				MappingContext context = new MappingContext();
				context.setConfig(mapperConfig);
				return context.mapping(rs);
			}
		}
		return invocation.proceed();
	}

	@Override
	public Object plugin(Object target) {
		return Plugin.wrap(target, this);
	}

	@Override
	public void setProperties(Properties properties) {
	}
}
