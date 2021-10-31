package org.minimalj.frontend.impl.json;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class JsonOutput {

	private static final String PROPERTY_CHANGES = "propertyChanges";
	private static final String CONTENT_CHANGES = "contentChanges";
	
	private final JsonWriter writer = new JsonWriter();
	private final Map<String, Object> output = new LinkedHashMap<>();

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void propertyChange(String componentId, String property, Object value) {
		if (!output.containsKey(PROPERTY_CHANGES)) {
			output.put(PROPERTY_CHANGES, new HashMap());
		}
		Map<String, Map<String, Object>> propertyChangesByElement = (Map<String, Map<String, Object>>) output.get(PROPERTY_CHANGES);

		if (!propertyChangesByElement.containsKey(componentId)) {
			propertyChangesByElement.put(componentId, new HashMap());
		}
		Map<String, Object> valuesByProperty = propertyChangesByElement.get(componentId);

		valuesByProperty.put(property, value);
	}

	public void removeContent(String componentId) {
		addContent(componentId, "clear");
	}

	public void addContent(String componentId, Object value) {
		List<Object> contentByProperty = getContentById(componentId);
		contentByProperty.add(value);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private List<Object> getContentById(String componentId) {
		if (!output.containsKey(CONTENT_CHANGES)) {
			output.put(CONTENT_CHANGES, new HashMap());
		}
		Map<String, List<Object>> contentChangesByElement = (Map<String, List<Object>>) output.get(CONTENT_CHANGES);

		if (!contentChangesByElement.containsKey(componentId)) {
			contentChangesByElement.put(componentId, new ArrayList());
		}
		List<Object> contentByProperty = contentChangesByElement.get(componentId);
		return contentByProperty;
	}
	
	public void add(String name, Object object) {
		output.put(name, object);
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void addElement(String name, Object object) {
		if (!output.containsKey(name)) {
			output.put(name, new ArrayList());
		}
		((List) output.get(name)).add(object);
	}
	
	public void forEach(Consumer<Object> c) {
		output.values().forEach(c);
	}

	@Override
	public String toString() {
		return writer.write(output);
	}
	
}
