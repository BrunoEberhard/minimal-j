package ch.openech.mj.vaadin.toolkit;

import java.util.List;

import ch.openech.mj.edit.validation.ValidationMessage;
import ch.openech.mj.toolkit.MultiLineTextField;

import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.CustomLayout;
import com.vaadin.ui.Panel;

public class VaadinMultiLineTextField extends Panel implements MultiLineTextField {

	private final CustomLayout customLayout;
	
	public VaadinMultiLineTextField() {
		customLayout = new CustomLayout("");
		customLayout.addStyleName("v-html-readonly");
		customLayout.setReadOnly(true);
		customLayout.setImmediate(true);
		
		setContent(customLayout);
		setScrollable(true);
	}
	
	@Override
	public void setText(String text) {
		customLayout.setTemplateContents(text != null ? text : "");
	}

	@Override
	public void requestFocus() {
		super.focus();
	}

	@Override
	public void setValidationMessages(List<ValidationMessage> validationMessages) {
		VaadinIndication.setValidationMessages(validationMessages, customLayout);
	}
	
}
