package org.minimalj.frontend.impl.json;

import org.minimalj.frontend.Frontend.IComponent;
import org.minimalj.frontend.Frontend.InputComponentListener;
import org.minimalj.frontend.Frontend.Search;
import org.minimalj.frontend.Frontend.TableActionListener;

public class JsonSearchPanel<T> extends JsonList {

	private final Search<T> search;
	private final JsonTable<T> table;
	
	public JsonSearchPanel(Search<T> search, Object[] keys, TableActionListener<T> listener) {
		super();
		this.search = search;
		addComponent(new JsonTextField("SearchTextField", new JsonSearchInputListener()));
		table = new JsonTable<T>(keys, false, listener);
		addComponent(table);
	}

	private class JsonSearchInputListener implements InputComponentListener {

		@Override
		public void changed(IComponent source) {
			JsonTextField textField = (JsonTextField) source;
			String query = textField.getValue();
			table.setObjects(search.search(query));
		}
	}
}
