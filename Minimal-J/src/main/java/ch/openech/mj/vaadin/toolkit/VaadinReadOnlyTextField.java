package ch.openech.mj.vaadin.toolkit;

import java.awt.event.ActionListener;
import java.awt.event.FocusListener;

import ch.openech.mj.toolkit.TextField;
import ch.openech.mj.util.StringUtils;

import com.vaadin.ui.Label;

public class VaadinReadOnlyTextField extends Label implements TextField {

	public VaadinReadOnlyTextField() {
//		addStyleName("v-html-readonly");
	}

	@Override
	public void setText(String text) {
		if (!StringUtils.isEmpty(text)) {
			super.setValue(text);
		} else {
			super.setValue(" ");
		}
	}

	@Override
	public String getText() {
		// not possible
		return (String) super.getValue();
	}

	@Override
	public void setEnabled(boolean editable) {
		// read only field cannot be enabled
	}

	@Override
	public void setFocusListener(FocusListener focusListener) {
		// read only field cannot be focused
	}

	@Override
	public void setCommitListener(ActionListener listener) {
		// read only field cannot get commit command
	}
	
}
