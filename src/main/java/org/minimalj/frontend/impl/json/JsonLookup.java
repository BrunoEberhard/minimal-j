package org.minimalj.frontend.impl.json;

import java.util.logging.Logger;

import org.minimalj.frontend.Frontend.Input;
import org.minimalj.frontend.Frontend.InputComponentListener;
import org.minimalj.frontend.Frontend.Search;
import org.minimalj.frontend.Frontend.TableActionListener;
import org.minimalj.model.Rendering;
import org.minimalj.model.Rendering.RenderType;

public class JsonLookup<T> extends JsonInputComponent<T> implements Input<T> {
	private static final Logger logger = Logger.getLogger(JsonLookup.class.getName());

	private final Object[] keys;
	private final Search<T> search;

	private T selectedObject;
	
	private JsonDialog dialog;
	
	public JsonLookup(InputComponentListener listener, Search<T> search, Object[] keys) {
		super("Lookup", listener);
		this.search = search;
		this.keys = keys;
	}
	
	public JsonDialog showLookupDialog() {
		dialog = new JsonDialog.JsonSearchDialog(search, keys, new JsonLookupTableListener());
		return dialog;
	}
	
	private class JsonLookupTableListener implements TableActionListener<T> {
		
		@Override
		public void action(T selectedObject) {
			setValue(selectedObject);
			dialog.closeDialog();
		}
	}
	
	@Override
	public void setValue(T value) {
		this.selectedObject = value;
		display();
	}

	protected void display() {
		String text = null;
		if (selectedObject instanceof Rendering) {
			Rendering rendering = (Rendering) selectedObject;
			text = rendering.render(RenderType.PLAIN_TEXT);
		} else if (selectedObject != null) {
			text = selectedObject.toString();
		}
		put("lookupText", text);
	}

	@Override
	public T getValue() {
		return selectedObject;
	}
}
