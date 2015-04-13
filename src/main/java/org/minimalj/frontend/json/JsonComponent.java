package org.minimalj.frontend.json;

import java.util.LinkedHashMap;
import java.util.Objects;
import java.util.UUID;

import org.minimalj.frontend.toolkit.ClientToolkit.IComponent;

public class JsonComponent extends LinkedHashMap<String, Object> implements IComponent {
	private static final long serialVersionUID = 1L;

	private static final String ID = "id";
	private static final String TYPE = "type";

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
		if (!Objects.equals(oldValue, value)) {
			JsonClientToolkit.getSession().propertyChange(getId(), property, value);
		}
		return oldValue;
	}
	
	public String getId() {
		return (String) get(ID);
	}
	
}
