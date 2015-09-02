package org.minimalj.frontend.impl.vaadin6.toolkit;

import org.minimalj.frontend.Frontend.Input;
import org.minimalj.util.StringUtils;

import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

public class VaadinReadOnlyTextField extends VerticalLayout implements Input<String> {
	private static final long serialVersionUID = 1L;
	private Label label;
	
	public VaadinReadOnlyTextField() {
		setWidth("100%");
	}

	@Override
	public void setValue(String text) {
		removeAllComponents();
		Label label = !StringUtils.isEmpty(text) ? new Label(text, Label.CONTENT_TEXT) : new Label("&nbsp;", Label.CONTENT_XHTML);
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
