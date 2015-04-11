package org.minimalj.frontend.json;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.minimalj.frontend.toolkit.ClientToolkit.IComponent;

public class JsonComponent implements IComponent {

	private static final String ID = "id";
	public static final String TYPE = "type";

	private static final JsonWriter writer = new JsonWriter();
	
	private final Map<String, Object> values = new LinkedHashMap<>();
	private final List<JsonComponent> components = new ArrayList<>();
	
	public JsonComponent(String type) {
		values.put(TYPE, type);
		values.put(ID, UUID.randomUUID().toString());
	}

	public void put(String key, Object value) {
		values.put(key, value);
		JsonClientToolkit.getSession().setProperty(getId(), key, value);
	}
	
	public Object get(String key) {
		return values.get(key);
	}
	
	public Map<String, Object> getValues() {
		return values;
	}

	public String getId() {
		return (String) values.get(ID);
	}
	
	@Override
	public String toString() {
		return writer.write(values);
	}
	
	protected void addComponent(JsonComponent component) {
		components.add(component);
	}
	
	public List<JsonComponent> getComponents() {
		return components;
	}
}
