package org.minimalj.frontend.impl.json;

import org.minimalj.frontend.Frontend.IComponent;
import org.minimalj.frontend.Frontend.IContent;
import org.minimalj.frontend.Frontend.InputComponentListener;
import org.minimalj.frontend.Frontend.Search;
import org.minimalj.frontend.Frontend.TableActionListener;

public class JsonSearchTable<T> extends JsonTable<T> implements IContent {

	private final Search<T> search;
	
	public JsonSearchTable(JsonPageManager pageManager, Search<T> search, Object[] keys, boolean multiSelect, TableActionListener<T> listener) {
		super(pageManager, keys, multiSelect, listener);
		this.search = search;
		put("overview", JsonTextField.createSearchTextField(new JsonSearchInputListener()));
	}

	private class JsonSearchInputListener implements InputComponentListener {

		@Override
		public void changed(IComponent source) {
			JsonTextField textField = (JsonTextField) source;
			String query = textField.getValue();
			setObjects(search.search(query));
		}
	}

	public void addComponent(JsonComponent c) {
		JsonFrontend.getClientSession().addContent(getId(), c);
	}

}
