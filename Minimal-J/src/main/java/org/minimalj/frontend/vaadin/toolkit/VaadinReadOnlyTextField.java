package org.minimalj.frontend.vaadin.toolkit;

import org.minimalj.frontend.toolkit.IFocusListener;
import org.minimalj.frontend.toolkit.TextField;
import org.minimalj.util.StringUtils;

import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

public class VaadinReadOnlyTextField extends VerticalLayout implements TextField {
	private static final long serialVersionUID = 1L;
	private Label label;
	
	public VaadinReadOnlyTextField() {
		setWidth("100%");
	}

	@Override
	public void setText(String text) {
		removeAllComponents();
		Label label = !StringUtils.isEmpty(text) ? new Label(text, Label.CONTENT_TEXT) : new Label("&nbsp;", Label.CONTENT_XHTML);
		label.addStyleName("v-html-readonly");
		addComponent(label);
	}

	@Override
	public String getText() {
		// not possible
		return label != null ? (String) label.getValue() : null;
	}

	@Override
	public void setEditable(boolean editable) {
		// read only field cannot be enabled
	}

	@Override
	public void setFocusListener(IFocusListener focusListener) {
		// read only field cannot be focused
	}

	@Override
	public void setCommitListener(Runnable listener) {
		// read only field cannot get commit command
	}
	
}
