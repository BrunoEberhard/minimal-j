package org.minimalj.frontend.impl.json;

import java.util.Arrays;

import org.minimalj.frontend.Frontend.IContent;
import org.minimalj.frontend.action.Action;

public class JsonLoginContent  extends JsonComponent implements IContent {

	public JsonLoginContent(IContent content, Action loginAction, Action... actions) {
		super("Login");
		put("content", content);
		
		put("actions", JsonPageManager.createActions(actions));
		
		int loginActionIndex = Arrays.asList(actions).indexOf(loginAction);
		put("loginActionIndex", loginActionIndex);
	}
}
