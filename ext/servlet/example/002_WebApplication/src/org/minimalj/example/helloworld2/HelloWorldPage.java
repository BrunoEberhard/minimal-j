package org.minimalj.example.helloworld2;

import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.Frontend.IContent;
import org.minimalj.frontend.page.Page;

public class HelloWorldPage extends Page {

	@Override
	public String getTitle() {
		return "Application";
	}

	@Override
	public IContent getContent() {
		return Frontend.getInstance().createHtmlContent("This is a page of the Minimal-J application");
	}

}
