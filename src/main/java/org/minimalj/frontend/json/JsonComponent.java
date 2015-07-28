package org.minimalj.frontend.json;

import java.util.LinkedHashMap;
import java.util.Objects;
import java.util.UUID;

import org.minimalj.frontend.Frontend.IComponent;

public class JsonComponent extends LinkedHashMap<String, Object> implements IComponent {
	private static final long serialVersionUID = 1L;

	private static final String ID = "id";
	private static final String TYPE = "type";

	private JsonPropertyListener propertyListener;
	
	public JsonComponent(String type) {
		this(type, true);
	}
	
	public JsonComponent(String type, boolean identifiable) {
		put(TYPE, type);
		if (identifiable) {
			put(ID, UUID.randomUUID().toString());
		}
	}
	
	public Object put(String property, Object value) {
		Object oldValue = super.put(property, value);
		if (!Objects.equals(oldValue, value) && propertyListener != null) {
			propertyListener.propertyChange(getId(), property, value);
		}
		return oldValue;
	}
	
	public String getId() {
		return (String) get(ID);
	}
	
	public void setPropertyListener(JsonPropertyListener propertyListener) {
		this.propertyListener = propertyListener;
	}
	
	public static interface JsonPropertyListener {
		
		public void propertyChange(String componentId, String property, Object value);
	}
	
}
