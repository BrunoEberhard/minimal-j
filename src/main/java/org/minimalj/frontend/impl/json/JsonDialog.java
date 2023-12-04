package org.minimalj.frontend.impl.json;

import java.util.List;

import org.minimalj.frontend.Frontend.IContent;
import org.minimalj.frontend.action.Action;

public class JsonDialog extends JsonComponent {

	public JsonDialog(String title, IContent content, Action saveAction, Action closeAction, List<Action> actions) {
		super("Dialog");
		put("title", title);
		put("content", content);

		setActions(saveAction, closeAction, actions);
	}

	protected void setActions(Action saveAction, Action closeAction, List<Action> actions) {
		List<Object> jsonActions = JsonPageManager.createActions(actions);
		put("actions", jsonActions);
		
		if (saveAction != null) {
			// if saveAction is one of the 'normal' actions the json adapter has to be reused
			// (there can be only one listener on the Minimal-J action, if there were two
			// one would not get the notifications for enable / disable)
			int index = actions.indexOf(saveAction);
			Object jsonSaveAction = index > -1 ? jsonActions.get(index) : JsonPageManager.createAction(saveAction);
			put("saveAction", jsonSaveAction);
		}
		if (closeAction != null) {
			int index = actions.indexOf(closeAction);
			Object jsonCloseAction = index > -1 ? jsonActions.get(index) : JsonPageManager.createAction(closeAction);
			put("closeAction", jsonCloseAction);
		}
	}

}