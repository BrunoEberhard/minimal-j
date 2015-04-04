package org.minimalj.frontend.json;

import java.util.LinkedHashMap;
import java.util.Map;

public class JsonMessage {

	public static final String TYPE = "type";

	private static final JsonWriter writer = new JsonWriter();
	
	private final Map<String, Object> values = new LinkedHashMap<>();
	
	public JsonMessage(String type) {
		values.put(TYPE, type);
	}

	public void put(String key, Object value) {
		values.put(key, value);
	}
	
	@Override
	public String toString() {
		return writer.write(values);
	}
	
}
