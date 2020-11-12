package ssll.core;

import java.io.IOException;
import java.util.Arrays;
import java.util.ServiceLoader;

public interface Json {

	public <E> E fromJson(String json, Class<E> clazz) throws IOException;

	public String toJson(Object object) throws IOException;

	public Object getSource();

	public int getOrder();

	public static Json newInstance() {
		Json last = null;
		for (Json t : ServiceLoader.load(Json.class)) {
			if (last == null || t.getOrder() < last.getOrder()) {
				last = t;
			}
		}
		if (last != null) {
			return last;
		}
		for (String className : Arrays.asList("Jackson2Json")) {
			try {
				return (Json) Class.forName(Json.class.getPackage().getName() + "." + className).newInstance();
			} catch (ClassNotFoundException | NoClassDefFoundError | InstantiationException | IllegalAccessException ex) {
			}
		}
		return null;
	}

}
