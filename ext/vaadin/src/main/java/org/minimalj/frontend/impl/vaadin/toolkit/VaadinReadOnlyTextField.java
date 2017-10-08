package org.minimalj.frontend.impl.vaadin.toolkit;

import org.minimalj.frontend.Frontend.Input;
import org.minimalj.util.StringUtils;

import com.vaadin.shared.ui.ContentMode;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

public class VaadinReadOnlyTextField extends VerticalLayout implements Input<String> {
	private static final long serialVersionUID = 1L;
	private Label label;
	
	public VaadinReadOnlyTextField() {
		setMargin(false);
		setSpacing(false);
		setWidth("100%");
	}

	@Override
	public void setValue(String text) {
		removeAllComponents();
		Label label = !StringUtils.isEmpty(text) ? new Label(text, ContentMode.TEXT) : new Label("&nbsp;", ContentMode.HTML);
		label.addStyleName("v-html-readonly");
		addComponent(label);
	}

	@Override
	public String getValue() {
		// not possible
		return label != null ? (String) label.getValue() : null;
	}

	@Override
	public void setEditable(boolean editable) {
		// read only field cannot be enabled
	}
}
