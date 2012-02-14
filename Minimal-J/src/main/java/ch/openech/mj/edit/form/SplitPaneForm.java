package ch.openech.mj.edit.form;

import java.util.List;

import javax.swing.Action;
import javax.swing.event.ChangeListener;

import ch.openech.mj.edit.validation.ValidationMessage;

public class SplitPaneForm<T> implements FormVisual<T> {

	private final FormVisual<T> formComponent;
	
	public SplitPaneForm(FormVisual<T> formComponent) {
		this.formComponent = formComponent;
	}

	@Override
	public void validate(List<ValidationMessage> resultList) {
		formComponent.validate(resultList);
	}

	@Override
	public void setValidationMessages(List<ValidationMessage> validationMessages) {
		formComponent.setValidationMessages(validationMessages);
	}

	@Override
	public void setChangeListener(ChangeListener changeListener) {
		formComponent.setChangeListener(changeListener);
	}

	@Override
	public void setObject(T object) {
		formComponent.setObject(object);
	}

	@Override
	public T getObject() {
		return formComponent.getObject();
	}

	@Override
	public boolean isResizable() {
		return formComponent.isResizable();
	}

	@Override
	public Object getComponent() {
		return formComponent.getComponent();
	}

	@Override
	public void setSaveAction(Action saveAction) {
		formComponent.setSaveAction(saveAction);
	}
	
}
