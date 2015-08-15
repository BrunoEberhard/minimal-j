package org.minimalj.frontend.json;

import org.minimalj.frontend.page.PageBrowser.ConfirmDialogResult;
import org.minimalj.frontend.page.PageBrowser.DialogListener;

public class JsonConfirmDialog extends JsonComponent {

	private final DialogListener dialogListener;
	
	public JsonConfirmDialog(String title, String message, DialogListener dialogListener) {
		super("Dialog");
		this.dialogListener = dialogListener;
		put("title", title);
		put("message", message);
		JsonFrontend.getClientSession().openConfirmDialog(this);
	}

	public void confirmed(boolean confirmed) {
		if (confirmed) {
			dialogListener.close(ConfirmDialogResult.YES);
		} else {
			dialogListener.close(ConfirmDialogResult.NO);
		}
	}

}
