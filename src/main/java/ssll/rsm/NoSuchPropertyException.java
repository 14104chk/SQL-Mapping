package ssll.rsm;

public class NoSuchPropertyException extends MappingException {

	public NoSuchPropertyException() {
	}

	public NoSuchPropertyException(String message) {
		super(message);
	}

	public NoSuchPropertyException(String message, Throwable cause) {
		super(message, cause);
	}

	public NoSuchPropertyException(Throwable cause) {
		super(cause);
	}

}
