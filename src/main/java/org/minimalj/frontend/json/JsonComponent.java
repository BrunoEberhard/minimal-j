package org.minimalj.frontend.json;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import org.minimalj.frontend.toolkit.ClientToolkit.IComponent;

public class JsonComponent implements IComponent {

	private static final String ID = "id";
	public static final String COMPONENT = "component";
	public static final String VALUE = "value";

	private static final JsonWriter writer = new JsonWriter();
	
	private final Map<String, Object> values = new LinkedHashMap<>();
	
	private JsonComponentListener listener;
	
	public JsonComponent(String component) {
		values.put(COMPONENT, component);
		values.put(ID, UUID.randomUUID().toString());
	}

	public void put(String key, Object value) {
		values.put(key, value);
		if (listener != null) {
			listener.changed((String) values.get(ID), ID, value);
		}
	}
	
	public Object get(String key) {
		return values.get(key);
	}

	@Override
	public String toString() {
		return writer.write(values);
	}
	
	public void setListener(JsonComponentListener listener) {
		this.listener = listener;
	}
	
	public static interface JsonComponentListener {
		public void changed(String uuid, String property, Object value);
	}
}
