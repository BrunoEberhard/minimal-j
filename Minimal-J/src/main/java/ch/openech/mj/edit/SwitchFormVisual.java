package ch.openech.mj.edit;

import java.util.List;

import javax.swing.Action;
import javax.swing.event.ChangeListener;

import ch.openech.mj.edit.form.FormVisual;
import ch.openech.mj.edit.validation.ValidationMessage;
import ch.openech.mj.toolkit.ClientToolkit;
import ch.openech.mj.toolkit.IComponent;
import ch.openech.mj.toolkit.SwitchLayout;

public class SwitchFormVisual<T> implements FormVisual<T> {

	private SwitchLayout switchLayout;
	private FormVisual<?> formVisual;
	private ChangeListener changeListener;
	private Action saveAction;
	
	public SwitchFormVisual() {
		switchLayout = ClientToolkit.getToolkit().createSwitchLayout();
	}

	@Override
	public IComponent getComponent() {
		return switchLayout;
	}

	@Override
	public void setChangeListener(ChangeListener changeListener) {
		this.changeListener = changeListener;
		if (formVisual != null) {
			formVisual.setChangeListener(changeListener);
		}
	}

	@Override
	public void validate(List<ValidationMessage> resultList) {
		if (formVisual != null) {
			formVisual.validate(resultList);
		}
	}

	@Override
	public void setValidationMessages(List<ValidationMessage> validationMessages) {
		if (formVisual != null) {
			formVisual.setValidationMessages(validationMessages);
		}
	}

	@Override
	public boolean isResizable() {
		return true;
	}

	@Override
	public void setSaveAction(Action saveAction) {
		if (formVisual != null) {
			formVisual.setSaveAction(saveAction);
		}
	}
	
	public FormVisual<?> getFormVisual() {
		return formVisual;
	}
	
	public void setFormVisual(FormVisual<?> formVisual) {
		this.formVisual = formVisual;
		formVisual.setChangeListener(changeListener);
		formVisual.setSaveAction(saveAction);
		
		switchLayout.show(formVisual);
	}
	
	@Override
	public T getObject() {
		return null;
	}
	
	@Override
	public void setObject(T object) {
		// not used
	}

}
