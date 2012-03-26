package ch.openech.mj.vaadin.toolkit;

import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.Action;

import ch.openech.mj.edit.validation.ValidationMessage;
import ch.openech.mj.toolkit.MultiLineTextField;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.BaseTheme;

public class VaadinMultiLineTextField extends VerticalLayout implements MultiLineTextField {

	public VaadinMultiLineTextField() {
	}
	
	@Override
	public void requestFocus() {
		super.focus();
	}

	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
	}

	@Override
	public void setValidationMessages(List<ValidationMessage> validationMessages) {
		VaadinIndication.setValidationMessages(validationMessages, this);
	}

	@Override
	public void clear() {
		removeAllComponents();
	}

	@Override
	public void addObject(Object object) {
		if (object != null) {
			Label label = new Label(object.toString());
			label.setContentMode(Label.CONTENT_TEXT);
			addComponent(label);
		}
	}
	
    @Override
	public void addAction(final Action action) {
		addComponent(createActionLink(action));
	}

	@Override
	public void addGap() {
		addComponent(new Label("."));
	}

	private static Component createActionLink(final Action action) {
		final Button button = new Button((String) action.getValue(Action.NAME));
		button.setDescription((String) action.getValue(Action.LONG_DESCRIPTION));
		button.setStyleName(BaseTheme.BUTTON_LINK);
		button.addListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				action.actionPerformed(new ActionEvent(button, 0, null));
			}
		});
		return button;
	}
	
}
