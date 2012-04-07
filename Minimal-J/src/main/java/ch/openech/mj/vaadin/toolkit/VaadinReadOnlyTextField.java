package ch.openech.mj.vaadin.toolkit;

import java.awt.event.FocusListener;

import ch.openech.mj.toolkit.TextField;
import ch.openech.mj.util.StringUtils;

import com.vaadin.ui.Label;

public class VaadinReadOnlyTextField extends Label implements TextField {

	public VaadinReadOnlyTextField() {
//		addStyleName("v-html-readonly");
	}

	@Override
	public void requestFocus() {
		// not possible
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
	public void setFocusListener(FocusListener focusListener) {
		// not possible
	}
	
}
