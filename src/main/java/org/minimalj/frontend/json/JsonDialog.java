package org.minimalj.frontend.json;

import org.minimalj.frontend.toolkit.Action;
import org.minimalj.frontend.toolkit.ClientToolkit.IContent;
import org.minimalj.frontend.toolkit.ClientToolkit.Search;
import org.minimalj.frontend.toolkit.ClientToolkit.TableActionListener;
import org.minimalj.frontend.toolkit.IDialog;

public class JsonDialog extends JsonComponent implements IDialog {

	private final Action closeAction;
	
	public JsonDialog(String title, IContent content, Action closeAction, Action[] actions) {
		super("Dialog");
		this.closeAction = closeAction;
		put("title", title);
		put("content", ((JsonComponent) content));
		put("actions", JsonClientToolkit.getSession().createActions(actions));
		JsonClientToolkit.getSession().openDialog(this);
	}

	private JsonDialog(String type, IContent content) {
		super(type);
		this.closeAction = null;
		put("title", "Search");
		put("content", ((JsonComponent) content));
		JsonClientToolkit.getSession().openDialog(this);
	}

	@Override
	public void closeDialog() {
		JsonClientToolkit.getSession().closeDialog((String) get("id"));
	}
	
	public static class JsonSearchDialog<T> extends JsonDialog {

		public JsonSearchDialog(Search<T> search, Object[] keys, TableActionListener<T> listener) {
			super("SearchDialog", new JsonSearchPanel(search, keys, listener));
		}
		
	}

}
