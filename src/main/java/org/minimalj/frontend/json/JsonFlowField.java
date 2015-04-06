package org.minimalj.frontend.json;

import java.util.ArrayList;
import java.util.List;

import org.minimalj.frontend.toolkit.ClientToolkit.IComponent;
import org.minimalj.frontend.toolkit.FlowField;

public class JsonFlowField extends JsonValueComponent implements FlowField {

	private final List<JsonComponent> components = new ArrayList<>();
	
	public JsonFlowField() {
		super("Vertical", null);
		
		put("components", components);
	}

	@Override
	public void clear() {
		components.clear();
	}

	@Override
	public void addGap() {
		JsonComponent gap = new JsonComponent("Gap");
		add(gap);
	}

	@Override
	public void add(IComponent component) {
		JsonComponent jsonComponent = (JsonComponent) component;
		components.add(jsonComponent);
	}

}
