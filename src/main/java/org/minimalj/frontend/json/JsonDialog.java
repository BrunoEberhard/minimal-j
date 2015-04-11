package org.minimalj.frontend.json;

import java.util.UUID;

import org.minimalj.frontend.toolkit.Action;
import org.minimalj.frontend.toolkit.ClientToolkit.IContent;
import org.minimalj.frontend.toolkit.IDialog;

public class JsonDialog extends JsonComponent implements IDialog {

	private CloseListener closeListener;
	
	public JsonDialog(String title, IContent content, Action[] actions) {
		super("Dialog");
		String id = UUID.randomUUID().toString();
		put("id", id);
		put("title", title);
		put("content", ((JsonComponent) content).getValues());
		put("actions", JsonClientToolkit.getSession().createActions(actions));
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

}
