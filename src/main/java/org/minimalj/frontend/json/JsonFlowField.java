package org.minimalj.frontend.json;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.minimalj.frontend.toolkit.ClientToolkit.IComponent;
import org.minimalj.frontend.toolkit.FlowField;

public class JsonFlowField extends JsonComponent implements FlowField {

	private final List<Map<String, Object>> components = new ArrayList<>();
	
	public JsonFlowField() {
		super("Vertical");
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
