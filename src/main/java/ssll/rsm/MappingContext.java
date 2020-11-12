package ssll.rsm;

import java.lang.reflect.InvocationTargetException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class MappingContext {

	private List result;
	private ResultSet resultSet;
	private MappingConfig config;

	public Object mapping(ResultSet rs) throws MappingException, SQLException {
		this.resultSet = rs;
		Label label = Label.parse(this, rs);
		if (rs.next()) {
			do {
				label.mapping(this, null);
			} while (rs.next());
			return result;
		} else {
			return config.buildMappingResult();
		}
	}

	public List getResult() {
		return result;
	}

	public void setResult(List result) {
		this.result = result;
	}

	public ResultSet getResultSet() {
		return resultSet;
	}

	public void setResultSet(ResultSet resultSet) {
		this.resultSet = resultSet;
	}

	public MappingConfig getConfig() {
		return config;
	}

	public void setConfig(MappingConfig config) {
		this.config = config;
	}

	/*
	public void setAttribute(String name, Object value) {
	}

	public Object getAttribute(String name) {
		return null;
	}

	public Object removeAttribute(String name) {
		return null;
	}
	 */
	@SuppressWarnings("ThrowableResultIgnored")
	public static SQLException wrapSQLException(String message, Throwable throwable) {
		if (throwable instanceof InvocationTargetException) {
			throwable = throwable.getCause();
		}
		if (throwable instanceof RuntimeException) {
			throw (RuntimeException) throwable;
		}
		if (throwable instanceof Error) {
			throw (Error) throwable;
		}
		if (throwable instanceof SQLException) {
			return (SQLException) throwable;
		} else {
			return new SQLException(message, throwable);
		}
	}

}
