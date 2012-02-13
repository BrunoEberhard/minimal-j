package ch.openech.mj.vaadin.toolkit;

import java.util.List;

import ch.openech.mj.edit.validation.ValidationMessage;
import ch.openech.mj.toolkit.MultiLineTextField;

import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.CustomLayout;

public class VaadinMultiLineTextField extends CustomLayout implements MultiLineTextField {

	public VaadinMultiLineTextField() {
		addStyleName("v-html-readonly");
		setReadOnly(true);
		setText("Lorem<p>Ipsum<br>");
		setHeight(100, Sizeable.UNITS_PERCENTAGE);
	}
	
	@Override
	public void setText(String text) {
		super.setTemplateContents(text);
	}

	@Override
	public void requestFocus() {
		super.focus();
	}

	@Override
	public void setValidationMessages(List<ValidationMessage> validationMessages) {
		VaadinIndication.setValidationMessages(validationMessages, this);
	}

}
