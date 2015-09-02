package org.minimalj.frontend.impl.json;

import org.minimalj.frontend.Frontend.IContent;
import org.minimalj.frontend.Frontend.Search;
import org.minimalj.frontend.Frontend.TableActionListener;
import org.minimalj.frontend.action.Action;
import org.minimalj.frontend.page.IDialog;

public class JsonDialog extends JsonComponent implements IDialog {

	private final Action closeAction;
	
	public JsonDialog(String title, IContent content, Action closeAction, Action[] actions) {
		super("Dialog");
		this.closeAction = closeAction;
		put("title", title);
		put("content", (content));
		put("actions", JsonFrontend.getClientSession().createActions(actions));
		JsonFrontend.getClientSession().openDialog(this);
	}

	private JsonDialog(String type, JsonComponent content) {
		super(type);
		this.closeAction = null;
		put("title", "Search");
		put("content", content);
		JsonFrontend.getClientSession().openDialog(this);
	}

	@Override
	public void closeDialog() {
		JsonFrontend.getClientSession().closeDialog((String) get("id"));
	}
	
	public static class JsonSearchDialog<T> extends JsonDialog {

		public JsonSearchDialog(Search<T> search, Object[] keys, TableActionListener<T> listener) {
			super("SearchDialog", new JsonSearchPanel(search, keys, listener));
		}
		
	}

}
