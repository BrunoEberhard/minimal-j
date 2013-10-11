package ch.openech.mj.vaadin.toolkit;

import ch.openech.mj.toolkit.IComponent;
import ch.openech.mj.toolkit.SwitchLayout;

import com.vaadin.ui.Component;
import com.vaadin.ui.GridLayout;

public class VaadinSwitchLayout extends GridLayout implements SwitchLayout {
	private static final long serialVersionUID = 1L;

	private IComponent showComponent;
	
	public VaadinSwitchLayout() {
	}
	
	public void requestFocus() {
		if (showComponent instanceof Focusable) {
			((Focusable) showComponent).focus();
		}
	}

	@Override
	public void show(IComponent c) {
		if (showComponent != null) {
			Component component = (Component) showComponent;
			removeComponent(component);
		}

		if (c != null) {
			Component component = (Component) c;
			component.setWidth("100%");
			addComponent(component);
			VaadinClientToolkit.focusFirstComponent(component);
		}
		this.showComponent = c;
	}

	@Override
	public IComponent getShownComponent() {
		return showComponent;
	}

}
