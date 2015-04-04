package org.minimalj.frontend.json;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.minimalj.frontend.toolkit.ClientToolkit.IComponent;

public class JsonComponent implements IComponent {

	private static final Logger logger = Logger.getLogger(JsonComponent.class.getName());
	private static final JsonWriter writer = new JsonWriter();
	
	public static final String VALUE = "value";
	
	private final Map<String, Object> values = new LinkedHashMap<>();
	
	public JsonComponent(String component) {
		values.put("component", component);
	}

	public Object put(String key, Object value) {
		return values.put(key, value);
	}
	
	public Object get(String key) {
		return values.get(key);
	}

	@Override
	public String toString() {
		return writer.write(values);
	}
	
}
