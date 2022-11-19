package org.minimalj.frontend.impl.json;

import java.util.ArrayList;
import java.util.List;

import org.minimalj.frontend.Frontend.Input;
import org.minimalj.frontend.action.Action;
import org.minimalj.frontend.action.ActionGroup;

public class JsonLookupActions extends JsonComponent implements Input<String> {
	private final Input<String> stringInput;

	public JsonLookupActions(Input<String> stringInput, ActionGroup actions) {
		super("LookupActions");
		this.stringInput = stringInput;

		put("input", stringInput);
		// click on the caption label should focus first component, not the group
		put("firstId", ((JsonComponent) stringInput).get("id"));

		List<JsonAction> actionLabels = new ArrayList<>();
		for (Action action : actions.getItems()) {
			actionLabels.add(new JsonAction(action));
		}
		put("actions", actionLabels);
	}

	@Override
	public void setValue(String text) {
		stringInput.setValue(text);
	}

	@Override
	public String getValue() {
		return stringInput.getValue();
	}

	@Override
	public void setEditable(boolean editable) {
		stringInput.setEditable(editable);
		put(JsonInputComponent.EDITABLE, editable);
	}
}
