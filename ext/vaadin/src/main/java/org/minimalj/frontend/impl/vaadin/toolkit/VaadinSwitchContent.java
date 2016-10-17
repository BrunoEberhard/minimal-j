package org.minimalj.frontend.impl.vaadin.toolkit;

import org.minimalj.frontend.Frontend.IContent;
import org.minimalj.frontend.Frontend.SwitchContent;

import com.vaadin.ui.Component;
import com.vaadin.ui.GridLayout;

public class VaadinSwitchContent extends GridLayout implements SwitchContent {
	private static final long serialVersionUID = 1L;

	private IContent showContent;
	
	public VaadinSwitchContent() {
	}
	
	public void requestFocus() {
		if (showContent instanceof Focusable) {
			((Focusable) showContent).focus();
		}
	}

	@Override
	public void show(IContent c) {
		if (showContent != null) {
			Component component = (Component) showContent;
			removeComponent(component);
		}

		if (c != null) {
			Component component = (Component) c;
			component.setWidth("100%");
			addComponent(component);
			VaadinFrontend.focusFirstComponent(component);
		}
		this.showContent = c;
	}

}
