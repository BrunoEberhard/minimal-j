package org.minimalj.frontend.json;

import org.minimalj.frontend.toolkit.Action;
import org.minimalj.frontend.toolkit.ClientToolkit.IContent;
import org.minimalj.frontend.toolkit.ClientToolkit.Search;
import org.minimalj.frontend.toolkit.ClientToolkit.TableActionListener;
import org.minimalj.frontend.toolkit.IDialog;

public class JsonDialog extends JsonComponent implements IDialog {

	private CloseListener closeListener;
	
	public JsonDialog(String title, IContent content, Action[] actions) {
		super("Dialog");
		put("title", title);
		put("content", ((JsonComponent) content));
		put("actions", JsonClientToolkit.getSession().createActions(actions));
	}

	private JsonDialog(String type, IContent content) {
		super(type);
		put("title", "Search");
		put("content", ((JsonComponent) content));
	}

	
	@Override
	public void setCloseListener(CloseListener closeListener) {
		this.closeListener = closeListener;
	}

	@Override
	public void openDialog() {
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
