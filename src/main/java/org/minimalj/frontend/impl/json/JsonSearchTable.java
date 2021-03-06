package org.minimalj.frontend.impl.json;

import org.minimalj.frontend.Frontend.IComponent;
import org.minimalj.frontend.Frontend.IContent;
import org.minimalj.frontend.Frontend.InputComponentListener;
import org.minimalj.frontend.Frontend.Search;
import org.minimalj.frontend.Frontend.TableActionListener;

public class JsonSearchTable<T> extends JsonComponent implements IContent {

	private final Search<T> search;
	private final JsonTable<T> table;
	
	public JsonSearchTable(Search<T> search, Object[] keys, boolean multiSelect, TableActionListener<T> listener) {
		super("List");
		this.search = search;
		addComponent(new JsonTextField("SearchTextField", new JsonSearchInputListener()));
		table = new JsonTable<>(keys, multiSelect, listener);
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

	public void addComponent(JsonComponent c) {
		JsonFrontend.getClientSession().addContent(getId(), c);
	}

}
