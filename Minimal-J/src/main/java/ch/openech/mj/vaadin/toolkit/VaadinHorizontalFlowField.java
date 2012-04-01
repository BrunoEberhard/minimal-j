package ch.openech.mj.vaadin.toolkit;

import java.util.List;

import javax.swing.Action;

import ch.openech.mj.edit.validation.ValidationMessage;
import ch.openech.mj.toolkit.FlowField;
import ch.openech.mj.util.StringUtils;

import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;

public class VaadinHorizontalFlowField extends HorizontalLayout implements FlowField {

	public VaadinHorizontalFlowField() {
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
	public void addHtml(String html) {
		if (!StringUtils.isBlank(html)) {
			Label label = new Label(html);
			label.setContentMode(Label.CONTENT_XHTML);
			addComponent(label);
		}
	}
	
    @Override
	public void addAction(final Action action) {
		addComponent(VaadinVerticalFlowField.createActionLink(action));
	}

	@Override
	public void addGap() {
		addComponent(new Label(" "));
	}
	
}
