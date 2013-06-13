package ch.openech.mj.edit.form;

import ch.openech.mj.toolkit.ClientToolkit;
import ch.openech.mj.toolkit.IComponent;
import ch.openech.mj.toolkit.SwitchLayout;

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

	@Override
	public boolean isResizable() {
		return true;
	}
	
	public void setForm(IForm<?> form) {
		switchLayout.show(form.getComponent());
	}

	@Override
	public void setObject(T object) {
		//
	}

}
