package org.minimalj.security;

import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.Frontend.FormContent;
import org.minimalj.frontend.Frontend.IContent;
import org.minimalj.frontend.page.Page;
import org.minimalj.util.resources.Resources;

public class AuthenticationFailedPage extends Page {

	@Override
	public String getTitle() {
		return Resources.getString("AuthenticationFailed");
	}

	@Override
	public IContent getContent() {
		FormContent form = Frontend.getInstance().createFormContent(1, 100);
		form.add(Frontend.getInstance().createText(Resources.getString("AuthenticationFailed")));
		return form;
	}

}
