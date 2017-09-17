package org.minimalj.frontend.impl.vaadin.toolkit;

import org.minimalj.application.Application;
import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.Frontend.IContent;
import org.minimalj.frontend.page.Page;
import org.minimalj.util.resources.Resources;

import com.vaadin.event.ShortcutAction;
import com.vaadin.event.ShortcutListener;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;

public class VaadinQueryContent extends GridLayout implements IContent {
	private static final long serialVersionUID = 1L;

	private final Label label;
	private final TextField textField;
	
	public VaadinQueryContent() {
		setWidth("100%");
		
		label = new Label();
		if (Resources.isAvailable("Application.queryCaption")) {
			label.setCaption(Resources.getString("Application.queryCaption"));
		}
		label.addStyleName("queryContentLabel");
		addComponent(label);

		textField = new TextField();
		textField.setWidth("600px");
		textField.addShortcutListener(new ShortcutListener("Query", ShortcutAction.KeyCode.ENTER, null) {
			private static final long serialVersionUID = 1L;
			
			@Override
			public void handleAction(Object sender, Object target) {
				if (target == textField) {
					String query = textField.getValue();
					Page page = Application.getInstance().createSearchPage(query);
					Frontend.show(page);
				}
			}
		});
		addComponent(textField);
		setComponentAlignment(textField, Alignment.MIDDLE_CENTER);
	}
	
}
