package ch.openech.mj.edit.form;

import java.util.List;

import javax.swing.Action;
import javax.swing.event.ChangeListener;

import ch.openech.mj.edit.validation.ValidationMessage;
import ch.openech.mj.toolkit.ClientToolkit;
import ch.openech.mj.toolkit.IComponent;
import ch.openech.mj.toolkit.SwitchLayout;

public class SwitchForm<T> implements IForm<T> {

	private SwitchLayout switchLayout;
	private IForm<?> form;
	private T object;
	
	public SwitchForm() {
		switchLayout = ClientToolkit.getToolkit().createSwitchLayout();
	}

	@Override
	public IComponent getComponent() {
		return switchLayout;
	}

	@Override
	public void setChangeListener(ChangeListener changeListener) {
		// a SwitchForm doesnt change (only the contained forms)
	}

	@Override
	public void validate(List<ValidationMessage> resultList) {
	}

	@Override
	public void setValidationMessages(List<ValidationMessage> validationMessages) {
	}

	@Override
	public boolean isResizable() {
		return true;
	}

	@Override
	public void setSaveAction(Action saveAction) {
		// no save
	}
	
	public IForm<?> getFormVisual() {
		return form;
	}
	
	public void setForm(IForm<?> form) {
		this.form = form;
		switchLayout.show(form);
	}
	
	@Override
	public T getObject() {
		return object;
	}
	
	@Override
	public void setObject(T object) {
		this.object = object;
	}

}
