package org.minimalj.frontend.impl.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.minimalj.frontend.action.Action;

public class ActionString {

	private final List<Object> components = new ArrayList<>();
	
	public ActionString add(String s) {
		components.add(s);
		return this;
	}
	
	public ActionString add(Action action) {
		components.add(action);
		return this;
	}
	
	public List<Object> getComponents() {
		return Collections.unmodifiableList(components);
	}
}
