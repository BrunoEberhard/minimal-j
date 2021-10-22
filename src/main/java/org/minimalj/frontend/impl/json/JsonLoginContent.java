package org.minimalj.frontend.impl.json;

import org.minimalj.frontend.Frontend.IContent;
import org.minimalj.frontend.action.Action;

public class JsonLoginContent  extends JsonComponent implements IContent {

	public JsonLoginContent(IContent content, Action loginAction, Action... actions) {
		super("Login");
		put("content", content);
		
		put("loginAction", JsonPageManager.createAction(loginAction));
		put("actions", JsonPageManager.createActions(actions));
	}
}
