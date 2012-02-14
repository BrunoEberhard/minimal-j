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
	
	public SwitchFormVisual() {
		switchLayout = ClientToolkit.getToolkit().createSwitchLayout();
	}

	@Override
	public IComponent getComponent() {
		return switchLayout;
	}

	@Override
	public void setChangeListener(ChangeListener changeListener) {
		// TODO Auto-generated method stub
	}

	@Override
	public void validate(List<ValidationMessage> resultList) {
		// TODO Auto-generated method stub
	}

	@Override
	public void setValidationMessages(List<ValidationMessage> validationMessages) {
		// TODO Auto-generated method stub
	}

	@Override
	public boolean isResizable() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setSaveAction(Action saveAction) {
		// TODO Auto-generated method stub
		
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
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void setObject(T object) {
		// TODO Auto-generated method stub
		
	}


}
