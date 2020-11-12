package ssll.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;

public class Jackson2Json implements Json {

	private final ObjectMapper om = new ObjectMapper();

	@Override
	public <E> E fromJson(String json, Class<E> clazz) throws IOException {
		return om.readValue(json, clazz);
	}

	@Override
	public String toJson(Object object) throws IOException {
		return om.writeValueAsString(object);
	}

	@Override
	public Object getSource() {
		return om;
	}

	@Override
	public int getOrder() {
		return 0xFF;
	}

}
