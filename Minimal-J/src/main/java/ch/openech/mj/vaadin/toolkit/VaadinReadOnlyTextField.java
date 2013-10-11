package ch.openech.mj.vaadin.toolkit;

import java.awt.event.FocusListener;

import ch.openech.mj.toolkit.TextField;
import ch.openech.mj.util.StringUtils;

import com.vaadin.ui.Label;

public class VaadinReadOnlyTextField extends Label implements TextField {
	private static final long serialVersionUID = 1L;

	public VaadinReadOnlyTextField() {
		addStyleName("v-html-readonly");
		setWidth("100%");
	}

	@Override
	public void setText(String text) {
		if (!StringUtils.isEmpty(getText())) {
			setReadOnly(false);
		}
		if (!StringUtils.isEmpty(text)) {
			super.setValue(text);
			setContentMode(CONTENT_TEXT);
		} else {
			super.setValue("&nbsp;");
			setContentMode(CONTENT_XHTML);
		}
		setReadOnly(true);
	}

	@Override
	public String getText() {
		// not possible
		return (String) super.getValue();
	}

	@Override
	public void setEditable(boolean editable) {
		// read only field cannot be enabled
	}

	@Override
	public void setFocusListener(FocusListener focusListener) {
		// read only field cannot be focused
	}

	@Override
	public void setCommitListener(Runnable listener) {
		// read only field cannot get commit command
	}
	
}
