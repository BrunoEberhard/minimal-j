package org.minimalj.frontend.impl.json;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class JsonInput {

	public static final String SHOW_DEFAULT_PAGE = "showDefaultPage";
	public static final String CHANGED_VALUE = "changedValue";
	public static final String ACTIVATED_ACTION = "activatedAction";
	public static final String TABLE_ACTION = "tableAction";
	public static final String CLOSE_DIALOG = "closeDialog";
	
	private final Map<String, Object> input;

	public JsonInput(String json) {
		this((Map<String, Object>) new JsonReader().read(json));
	}
	
	public JsonInput(Map<String, Object> input) {
		this.input = input;
	}
	
	public Map<String, Object> get(String name) {
		return get(input, name);
	}

	public List<Object> getList(String name) {
		return getList(input, name);
	}

	public Object getObject(String name) {
		return input.get(name);
	}
	
	public boolean containsObject(String name) {
		return input.containsKey(name);
	}

	public static Map<String, Object> get(Map<String, Object> object, String name) {
		if (!object.containsKey(name)) {
			return Collections.emptyMap();
		} else {
			return (Map<String, Object>) object.get(name);
		}
	}
	
	public static List<Object> getList(Map<String, Object> object, String name) {
		if (!object.containsKey(name)) {
			return Collections.emptyList();
		} else {
			return (List<Object>) object.get(name);
		}
	}

}
