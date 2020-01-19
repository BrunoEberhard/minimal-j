package org.minimalj.frontend.impl.json;

import java.util.Arrays;
import java.util.List;

import org.minimalj.frontend.Frontend.IContent;
import org.minimalj.frontend.action.Action;
import org.minimalj.frontend.page.IDialog;

public class JsonDialog extends JsonComponent implements IDialog {

	public JsonDialog(String title, IContent content, Action saveAction, Action closeAction, Action[] actions) {
		super(content instanceof JsonSearchTable ? "SearchDialog" : "Dialog");
		put("title", title);
		put("content", content);

		setActions(saveAction, closeAction, actions);
	}

	protected void setActions(Action saveAction, Action closeAction, Action[] actions) {
		List<Object> jsonActions = JsonFrontend.getClientSession().createActions(actions);
		put("actions", jsonActions);
		
		if (saveAction != null) {
			// if saveAction is one of the 'normal' actions the json adapter has to be reused
			// (there can be only one listener on the Minimal-J action, if there were two
			// one would not get the notifications for enable / disable)
			int index = Arrays.asList(actions).indexOf(saveAction);
			Object jsonSaveAction = index > -1 ? jsonActions.get(index) : JsonFrontend.getClientSession().createAction(saveAction);
			put("saveAction", jsonSaveAction);
		}
		if (closeAction != null) {
			int index = Arrays.asList(actions).indexOf(closeAction);
			Object jsonCloseAction = index > -1 ? jsonActions.get(index) : JsonFrontend.getClientSession().createAction(closeAction);
			put("closeAction", jsonCloseAction);
		}
	}

	@Override
	public void closeDialog() {
		JsonFrontend.getClientSession().closeDialog(this);
	}

	protected class CancelAction extends Action {
		@Override
		public void action() {
			closeDialog();
		}
	}

}