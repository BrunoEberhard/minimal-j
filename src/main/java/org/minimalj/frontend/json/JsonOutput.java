package org.minimalj.frontend.json;

import java.util.LinkedHashMap;
import java.util.Map;

public class JsonOutput {

	public static final String PROPERTY_CHANGE = "propertyChange";
	
	private final JsonWriter writer = new JsonWriter();
	private final Map<String, Object> output = new LinkedHashMap<>();

	public void propertyChange(String componentId, String property, Object value) {
		Map<String, Object> propertyChanges = getOrCreate(property);
		Map<String, Object> componentPropertyChanges = getOrCreate(propertyChanges, componentId);
		componentPropertyChanges.put(property, value);
	}

	public void add(String name, Object object) {
		output.put(name, object);
	}
	
	private Map<String, Object> getOrCreate(String name) {
		return getOrCreate(output, name);
	}
	
	private Map<String, Object> getOrCreate(Map<String, Object> object, String name) {
		if (!object.containsKey(name)) {
			object.put(name, new LinkedHashMap<String, Object>());
		}
		return (Map<String, Object>) object.get(name);
	}
	
	@Override
	public String toString() {
		return writer.write(output);
	}
	
}
