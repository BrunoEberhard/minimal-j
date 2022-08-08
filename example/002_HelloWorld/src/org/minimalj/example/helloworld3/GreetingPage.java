package org.minimalj.example.helloworld3;

import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.Frontend.FormContent;
import org.minimalj.frontend.Frontend.IContent;
import org.minimalj.frontend.Frontend.Input;
import org.minimalj.frontend.form.element.FormElementConstraint;
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
		FormContent form = Frontend.getInstance().createFormContent(1, 100);
		form.add(null, false, Frontend.getInstance().createText("Hello " + user.name), null, 1);
		if (user.image != null) {
			Input<byte[]> image = Frontend.getInstance().createImage(null);
			image.setValue(user.image);
			form.add(null, false, image, new FormElementConstraint(3, FormElementConstraint.MAX), 1);
		}
		return form;
	}
	
}
