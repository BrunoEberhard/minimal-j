package org.minimalj.frontend.impl.json;

import java.util.Collections;
import java.util.Map;

public class JsonInput {

	public static final String SHOW_DEFAULT_PAGE = "showDefaultPage";
	public static final String CHANGED_VALUE = "changedValue";
	public static final String ACTIVATED_ACTION = "activatedAction";
	public static final String TABLE_ACTION = "tableAction";
	public static final String CLOSE_DIALOG = "closeDialog";
	
	private final Map<String, Object> input;

	public JsonInput(String json) {
		this((Map<String, Object>) JsonReader.read(json));
	}
	
	public JsonInput(Map<String, Object> input) {
		this.input = input;
	}
	
	@SuppressWarnings("unchecked")
	public Map<String, Object> get(String name) {
		if (!input.containsKey(name)) {
			return Collections.emptyMap();
		} else {
			return (Map<String, Object>) input.get(name);
		}
	}

	public Object getObject(String name) {
		return input.get(name);
	}
	
	public boolean containsObject(String name) {
		return input.containsKey(name);
	}
}
