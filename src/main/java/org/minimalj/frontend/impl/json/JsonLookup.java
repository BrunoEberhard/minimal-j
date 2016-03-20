package org.minimalj.frontend.impl.json;

import java.util.logging.Logger;

import org.minimalj.frontend.Frontend.Input;
import org.minimalj.frontend.Frontend.InputComponentListener;
import org.minimalj.frontend.Frontend.Search;
import org.minimalj.frontend.Frontend.TableActionListener;
import org.minimalj.model.Rendering;
import org.minimalj.model.Rendering.RenderType;
import org.minimalj.util.EqualsHelper;

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
	public void setValue(T object) {
		if (!EqualsHelper.equals(selectedObject, object)) {
			this.selectedObject = object;
			put(VALUE, render(selectedObject));
			fireChange();
		}
	}

	protected String render(T object) {
		if (object instanceof Rendering) {
			Rendering rendering = (Rendering) object;
			return rendering.render(RenderType.PLAIN_TEXT);
		} else if (object != null) {
			return object.toString();
		} else {
			return null;
		}
	}

	@Override
	public T getValue() {
		return selectedObject;
	}
}
