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
	private T object;
	
	public SwitchFormVisual() {
		switchLayout = ClientToolkit.getToolkit().createSwitchLayout();
	}

	@Override
	public IComponent getComponent() {
		return switchLayout;
	}

	@Override
	public void setChangeListener(ChangeListener changeListener) {
		// a SwitchFormVisual doesnt change (only the contained forms)
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
	
	public FormVisual<?> getFormVisual() {
		return formVisual;
	}
	
	public void setFormVisual(FormVisual<?> formVisual) {
		this.formVisual = formVisual;
		switchLayout.show(formVisual);
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
