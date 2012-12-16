package ch.openech.mj.edit.form;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.swing.Action;
import javax.swing.event.ChangeListener;

import ch.openech.mj.db.model.PropertyInterface;
import ch.openech.mj.toolkit.ClientToolkit;
import ch.openech.mj.toolkit.IComponent;
import ch.openech.mj.toolkit.SwitchLayout;

public class SwitchForm<T> implements IForm<T> {

	private SwitchLayout switchLayout;
	private IForm form;
	
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
		switchLayout.show(form.getComponent());
	}

	@Override
	public Collection<PropertyInterface> getProperties() {
		return Collections.emptyList();
	}

	@Override
	public void setObject(T object) {
		//
	}

	@Override
	public void setValidationMessage(PropertyInterface property, List<String> validationMessages) {
//		form.setValidationMessage(property, validationMessages);
	}

}
