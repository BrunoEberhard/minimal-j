package ch.openech.mj.edit.form;

import java.util.Collection;
import java.util.List;

import javax.swing.Action;
import javax.swing.event.ChangeListener;

import ch.openech.mj.db.model.PropertyInterface;
import ch.openech.mj.toolkit.IComponent;

public class SplitPaneForm<T> implements IForm<T> {

	private final IForm<T> formComponent;
	
	public SplitPaneForm(IForm<T> formComponent) {
		this.formComponent = formComponent;
	}

	@Override
	public void setChangeListener(ChangeListener changeListener) {
		formComponent.setChangeListener(changeListener);
	}

	@Override
	public boolean isResizable() {
		return formComponent.isResizable();
	}

	@Override
	public IComponent getComponent() {
		return formComponent.getComponent();
	}

	@Override
	public void setSaveAction(Action saveAction) {
		formComponent.setSaveAction(saveAction);
	}

	public Collection<PropertyInterface> getProperties() {
		return formComponent.getProperties();
	}

	public void setObject(T value) {
		formComponent.setObject(value);
	}

	public void setValidationMessage(PropertyInterface property, List<String> validationMessages) {
		formComponent.setValidationMessage(property, validationMessages);
	}
	
}
