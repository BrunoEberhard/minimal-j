package org.minimalj.frontend.json;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class JsonOutput {

	private static final String PROPERTY_CHANGES = "propertyChanges";
	
	private final JsonWriter writer = new JsonWriter();
	private final Map<String, Object> output = new LinkedHashMap<>();

	public void propertyChange(String componentId, String property, Object value) {
		if (!output.containsKey(PROPERTY_CHANGES)) {
			output.put(PROPERTY_CHANGES, new ArrayList());
		}
		List<Map<String, Object>> propertyChanges = (List<Map<String, Object>>) output.get(PROPERTY_CHANGES);

		Map<String, Object> propertyChange = new HashMap<String, Object>();
		propertyChange.put("id", componentId);
		propertyChange.put("property", property);
		propertyChange.put("value", value);
		
		propertyChanges.add(propertyChange);
	}

	public void add(String name, Object object) {
		output.put(name, object);
	}
	
	@Override
	public String toString() {
		return writer.write(output);
	}
	
}
