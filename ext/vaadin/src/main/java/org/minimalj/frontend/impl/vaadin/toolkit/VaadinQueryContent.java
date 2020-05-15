package org.minimalj.frontend.impl.vaadin.toolkit;

import org.minimalj.frontend.Frontend.IContent;
import org.minimalj.util.resources.Resources;

import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.textfield.TextField;

public class VaadinQueryContent extends FlexLayout implements IContent {
	private static final long serialVersionUID = 1L;

	private final Label label;
	private final TextField textField;
	
	public VaadinQueryContent() {
		setWidth("100%");
		
		label = new Label();
		if (Resources.isAvailable("Application.queryCaption")) {
			label.setText(Resources.getString("Application.queryCaption"));
		}
		label.getClassNames().add("queryContentLabel");
		add(label);

		textField = new TextField();
		textField.setWidth("600px");
//		Shortcuts.addShortcutListener(lifecycleOwner, command, key, keyModifiers)
//		textField.addShortcutListener(new ShortcutListener("Query", ShortcutAction.KeyCode.ENTER, null) {
//			private static final long serialVersionUID = 1L;
//			
//			@Override
//			public void handleAction(Object sender, Object target) {
//				if (target == textField) {
//					String query = textField.getValue();
//					Page page = Application.getInstance().createSearchPage(query);
//					Frontend.show(page);
//				}
//			}
//		});
		add(textField);
		// setComponentAlignment(textField, Alignment.MIDDLE_CENTER);
	}
	
}
