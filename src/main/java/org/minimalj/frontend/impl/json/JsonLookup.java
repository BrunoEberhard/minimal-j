package org.minimalj.frontend.impl.json;

import org.minimalj.frontend.Frontend.Input;

public class JsonLookup extends JsonComponent implements Input<String> {
	private final Input<String> stringInput;
	private final Runnable lookup;

	public JsonLookup(Input<String> stringInput, Runnable lookup) {
		super("Lookup");
		this.stringInput = stringInput;
		this.lookup = lookup;

		put("input", stringInput);
		// click on the caption label should focus first component, not the group
		put("firstId", ((JsonComponent) stringInput).get("id"));
	}

	public void showLookupDialog() {
		lookup.run();
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
