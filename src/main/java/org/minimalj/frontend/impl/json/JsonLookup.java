package org.minimalj.frontend.impl.json;

import org.minimalj.frontend.Frontend.InputComponentListener;

public class JsonLookup extends JsonInputComponent<String> {
	private final Runnable lookup;

	private boolean set = false;
	
	public JsonLookup(Runnable lookup, InputComponentListener changeListener) {
		super("Lookup", changeListener);
		this.lookup = lookup;
	}

	public void showLookupDialog() {
		lookup.run();
	}
	
	@Override
	public void setValue(String text) {
		put(VALUE, text);
	}

	@Override
	public String getValue() {
		return (String) get(VALUE);
	}
}
