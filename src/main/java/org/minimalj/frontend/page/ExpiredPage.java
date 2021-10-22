package org.minimalj.frontend.page;

import org.minimalj.util.resources.Resources;

public class ExpiredPage extends HtmlPage {

	public ExpiredPage() {
		super(null);
	}
	
	@Override
	protected String getHtml() {
		return Resources.getString("ExpiredPage.html");
	}
	
	@Override
	public String getTitle() {
		return Resources.getString("ExpiredPage");
	}

}
