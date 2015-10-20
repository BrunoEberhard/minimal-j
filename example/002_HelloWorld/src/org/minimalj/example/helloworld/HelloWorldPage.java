package org.minimalj.example.helloworld;

import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.Frontend.FormContent;
import org.minimalj.frontend.Frontend.IContent;
import org.minimalj.frontend.page.Page;

public class HelloWorldPage extends Page {

	@Override
	public String getTitle() {
		return "Hello World";
	}

	@Override
	public IContent getContent() {
		FormContent form = Frontend.getInstance().createFormContent(1, 100);
		form.add(Frontend.getInstance().createText("Hello World"));
		return form;
	}

}
