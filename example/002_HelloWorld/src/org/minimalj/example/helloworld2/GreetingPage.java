package org.minimalj.example.helloworld2;

import java.util.Collections;
import java.util.List;

import org.minimalj.frontend.page.Page;
import org.minimalj.frontend.toolkit.Action;
import org.minimalj.frontend.toolkit.ClientToolkit;
import org.minimalj.frontend.toolkit.ClientToolkit.IContent;
import org.minimalj.frontend.toolkit.FormContent;

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
		FormContent form = ClientToolkit.getToolkit().createFormContent(1, 100);
		form.add(ClientToolkit.getToolkit().createLabel("Hello " + user.name));
		return form;
	}
	
	@Override
	public List<Action> getActions() {
		return Collections.singletonList(new UserNameEditor());
	}

}
