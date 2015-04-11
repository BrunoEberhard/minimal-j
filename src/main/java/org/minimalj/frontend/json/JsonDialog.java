package org.minimalj.frontend.json;

import java.util.UUID;

import org.minimalj.frontend.toolkit.Action;
import org.minimalj.frontend.toolkit.ClientToolkit.IContent;
import org.minimalj.frontend.toolkit.IDialog;

public class JsonDialog extends JsonComponent implements IDialog {

	private final JsonClientSession session;
	private CloseListener closeListener;
	
	public JsonDialog(JsonClientSession session, String title, IContent content, Action[] actions) {
		super("Dialog");
		this.session = session;
		String id = UUID.randomUUID().toString();
		put("id", id);
		put("title", title);
		put("content", ((JsonComponent) content).getValues());
		put("actions", session.createActions(actions));
	}

	@Override
	public void setCloseListener(CloseListener closeListener) {
		this.closeListener = closeListener;
	}

	@Override
	public void openDialog() {
		session.openDialog(this);
	}

	@Override
	public void closeDialog() {
		session.closeDialog((String) get("id"));
	}

}
