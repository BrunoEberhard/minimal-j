package org.minimalj.security;

import org.minimalj.frontend.Frontend;
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
		return Frontend.getInstance().createHtmlContent(Resources.getString("AuthenticationFailed"));
	}

}
