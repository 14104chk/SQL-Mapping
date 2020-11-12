package ssll.rsm;

public class VirtualObject {

	public static final VirtualObject UNKNOWN_ENTITY = new VirtualObject("UNKNOWN_ENTITY");
	public static final VirtualObject INVALID_ENTITY = new VirtualObject("INVALID_ENTITY");
	public static final VirtualObject INVALID_VALUE = new VirtualObject("INVALID_VALUE");

	private final String name;

	private VirtualObject(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return String.format("Object(%s)", name);
	}
}
