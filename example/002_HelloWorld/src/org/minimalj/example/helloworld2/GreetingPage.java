package org.minimalj.example.helloworld2;

import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.Frontend.IContent;
import org.minimalj.frontend.page.Page;

public class GreetingPage implements Page {

	private final User user;
	
	public GreetingPage(User user) {
		this.user = user;
	}
	
	@Override
	public String getTitle() {
		return "Hello " + user.name;
	}

	@Override
	public IContent getContent() {
		return Frontend.getInstance().createHtmlContent("Hello " + user.name);
	}
	
}
