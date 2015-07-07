package org.minimalj.example.helloworld;

import org.minimalj.frontend.page.Page;
import org.minimalj.frontend.toolkit.ClientToolkit;
import org.minimalj.frontend.toolkit.ClientToolkit.IContent;
import org.minimalj.frontend.toolkit.FormContent;

public class HelloWorldPage implements Page {

	@Override
	public String getTitle() {
		return "Hello World";
	}

	@Override
	public IContent getContent() {
		FormContent form = ClientToolkit.getToolkit().createFormContent(1, 100);
		form.add(ClientToolkit.getToolkit().createLabel("Hello World"));
		return form;
	}

}
