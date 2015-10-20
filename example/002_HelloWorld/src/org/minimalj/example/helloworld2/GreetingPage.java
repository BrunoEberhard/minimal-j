package org.minimalj.example.helloworld2;

import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.Frontend.FormContent;
import org.minimalj.frontend.Frontend.IContent;
import org.minimalj.frontend.page.Page;

public class GreetingPage extends Page {

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
		FormContent form = Frontend.getInstance().createFormContent(1, 100);
		form.add(Frontend.getInstance().createText("Hello " + user.name));
		return form;
	}
	
}
