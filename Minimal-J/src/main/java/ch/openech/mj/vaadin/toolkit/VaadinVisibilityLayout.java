package ch.openech.mj.vaadin.toolkit;

import ch.openech.mj.toolkit.IComponent;
import ch.openech.mj.toolkit.VisibilityLayout;

import com.vaadin.ui.Component;
import com.vaadin.ui.GridLayout;

public class VaadinVisibilityLayout extends GridLayout implements VisibilityLayout {

	private final Component component;
	
	public VaadinVisibilityLayout(IComponent content) {
		component = VaadinClientToolkit.getComponent(content);
		component.setWidth("100%");
		addComponent(component);
	}

	@Override
	public void requestFocus() {
		if (component instanceof Focusable) {
			((Focusable) component).focus();
		}
	}

	@Override
	public void setVisible(boolean visible) {
		super.setVisible(visible);
	}

	@Override
	public boolean isVisible() {
		return super.isVisible();
	}

}
