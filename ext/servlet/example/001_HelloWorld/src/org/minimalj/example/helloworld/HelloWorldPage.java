package org.minimalj.example.helloworld;

import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.Frontend.IContent;
import org.minimalj.frontend.page.Page;

public class HelloWorldPage extends Page {

	@Override
	public String getTitle() {
		return "Hello World";
	}

	@Override
	public IContent getContent() {
		return Frontend.getInstance().createHtmlContent("Hello World");
	}

}
