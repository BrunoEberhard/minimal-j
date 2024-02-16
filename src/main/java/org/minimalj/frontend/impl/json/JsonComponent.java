package org.minimalj.frontend.impl.json;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.LinkedHashMap;
import java.util.Objects;
import java.util.UUID;

import org.minimalj.frontend.Frontend.IComponent;

/**
 * Base class for all objects that represent an UI element in a web
 * frontend.<p>
 * 
 * It only implements Externalizable to remove the need for a serialVersionUID in every subclass.
 * Java serialization of this classes will fail. Use JsonWriter and JsonReader.
 *
 */
public class JsonComponent extends LinkedHashMap<String, Object> implements IComponent, Externalizable {
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
	
	@Override
	public Object put(String property, Object value) {
		Object oldValue = super.put(property, value);
		fireChange(property, value, oldValue);
		return oldValue;
	}

	protected void fireChange(String property, Object value, Object oldValue) {
		if (!Objects.equals(oldValue, value) && propertyListener != null) {
			propertyListener.propertyChange(this, property, value);
		}
	}

	Object putSilent(String property, Object value) {
		return super.put(property, value);
	}

	public String getId() {
		return (String) get(ID);
	}
	
	public void setCssClass(String cssClass) {
		put("cssClass", cssClass);
	}
	
	public void setNoCaption() {
		put("noCaption", "true");
	}
	
	public void setFormElementVisible(boolean visible) {
		if (visible && !containsKey("hideFormElement")) {
			return;
		}
		put("hideFormElement", !visible);
	}
	
	public void setPropertyListener(JsonPropertyListener propertyListener) {
		this.propertyListener = propertyListener;
	}
	
	public interface JsonPropertyListener {
		
		public void propertyChange(JsonComponent component, String property, Object value);
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		explainExternalization();
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		explainExternalization();
	}

	private void explainExternalization() {
		throw new RuntimeException("JsonComponent is not meant to be serialized. Does only implement Externalizable to avoid SerialVersionUID in all components");
	}
	
}
