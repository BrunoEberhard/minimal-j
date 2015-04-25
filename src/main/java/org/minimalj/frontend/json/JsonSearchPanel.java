package org.minimalj.frontend.json;

import org.minimalj.frontend.toolkit.ClientToolkit.IComponent;
import org.minimalj.frontend.toolkit.ClientToolkit.InputComponentListener;
import org.minimalj.frontend.toolkit.ClientToolkit.Search;
import org.minimalj.frontend.toolkit.ClientToolkit.TableActionListener;

public class JsonSearchPanel<T> extends JsonFlowField {

	private final Search<T> search;
	private final JsonTable<T> table;
	
	public JsonSearchPanel(Search<T> search, Object[] keys, TableActionListener<T> listener) {
		super();
		this.search = search;
		add(new JsonTextField("SearchTextField", new JsonSearchInputListener()));
		table = new JsonTable<T>(keys, listener);
		add(table);
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
