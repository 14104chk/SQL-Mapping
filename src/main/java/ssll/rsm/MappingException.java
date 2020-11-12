package ssll.rsm;

import java.sql.SQLException;

public class MappingException extends SQLException {

	public MappingException() {
	}

	public MappingException(String message) {
		super(message);
	}

	public MappingException(String message, Throwable cause) {
		super(message, cause);
	}

	public MappingException(Throwable cause) {
		super(cause);
	}

}
