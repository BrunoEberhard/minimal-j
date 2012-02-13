package ch.openech.mj.vaadin.toolkit;

import ch.openech.mj.toolkit.SwitchLayout;

import com.vaadin.ui.Component;
import com.vaadin.ui.GridLayout;

public class VaadinSwitchLayout extends GridLayout implements SwitchLayout {

	private Component showComponent;
	
	public VaadinSwitchLayout() {
	}
	
	public void requestFocus() {
		if (showComponent instanceof Focusable) {
			((Focusable) showComponent).focus();
		}
	}

	@Override
	public void show(Object component) {
		if (component != null) {
			Component c = (Component) component;
			c.setWidth("100%");
			if (showComponent != null) {
				replaceComponent(showComponent, c);
			} else {
				addComponent(c);
			}
		} else {
			if (showComponent != null) {
				removeComponent(showComponent);
			}
		}
		this.showComponent = (Component)component;
	}

	@Override
	public Object getShownComponent() {
		return showComponent;
	}

}
