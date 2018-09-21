package org.minimalj.frontend.impl.json;

import org.minimalj.frontend.Frontend.Input;
import org.minimalj.frontend.Frontend.InputComponentListener;
import org.minimalj.model.Rendering;
import org.minimalj.repository.sql.EmptyObjects;
import org.minimalj.util.EqualsHelper;

public class JsonLookup<T> extends JsonInputComponent<T> implements Input<T> {
	private final Runnable lookup;

	private boolean set = false;
	private T selectedObject;
	
	public JsonLookup(Runnable lookup, InputComponentListener changeListener) {
		super("Lookup", changeListener);
		this.lookup = lookup;
	}

	public void showLookupDialog() {
		lookup.run();
	}
	
	@Override
	public void setValue(T object) {
		if (!set || !EqualsHelper.equals(selectedObject, object)) {
			this.selectedObject = object;
			if (EmptyObjects.isEmpty(object)) {
				put(VALUE, null);
			} else {
				put(VALUE, Rendering.toString(selectedObject));
			}
			fireChange();
		}
	}

	@Override
	public T getValue() {
		return selectedObject;
	}
}
