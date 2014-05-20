package org.minimalj.frontend.edit.form;

import org.minimalj.frontend.toolkit.ClientToolkit;
import org.minimalj.frontend.toolkit.IComponent;
import org.minimalj.frontend.toolkit.SwitchLayout;

public class SwitchForm<T> implements IForm<T> {

	private SwitchLayout switchLayout;
	
	public SwitchForm() {
		switchLayout = ClientToolkit.getToolkit().createSwitchLayout();
	}

	@Override
	public IComponent getComponent() {
		return switchLayout;
	}

	@Override
	public void setChangeListener(IForm.FormChangeListener<T> changeListener) {
		// a SwitchForm doesnt change (only the contained forms)
	}

	public void setForm(IForm<?> form) {
		switchLayout.show(form.getComponent());
	}

	@Override
	public void setObject(T object) {
		//
	}

}
