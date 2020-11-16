package org.minimalj.frontend.impl.json;

import java.util.Objects;

import org.minimalj.frontend.Frontend.IContent;
import org.minimalj.frontend.action.Action;

public class JsonLoginContent  extends JsonComponent implements IContent {

	public JsonLoginContent(IContent content, Action loginAction, Action forgetPasswordAction) {
		super("Login");
		put("content", content);
		
		Object jsonLoginAction = JsonPageManager.createAction(Objects.requireNonNull(loginAction));
		put("loginAction", jsonLoginAction);
		
		if (forgetPasswordAction != null) {
			Object jsonForgetPasswordAction = JsonPageManager.createAction(forgetPasswordAction);
			put("forgetPasswordAction", jsonLoginAction);
		}
	}
}
