package org.minimalj.frontend.impl.vaadin.toolkit;

import org.minimalj.frontend.Frontend.IComponent;
import org.minimalj.frontend.Frontend.IContent;
import org.minimalj.frontend.Frontend.SwitchComponent;
import org.minimalj.frontend.Frontend.SwitchContent;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

public class VaadinSwitch extends VerticalLayout implements SwitchContent, SwitchComponent {
	private static final long serialVersionUID = 1L;

	private Component current;
	
	public VaadinSwitch() {
	}
	
//	public void requestFocus() {
//		if (current instanceof Focusable) {
//			((Focusable) current).focus();
//		}
//	}

	@Override
	public void show(IContent content) {
		show((Component) content);
	}
	
	@Override
	public void show(IComponent component) {
		show((Component) component);
	}

	private void show(Component component) {
		if (component == current) {
			return;
		}
		
		if (current != null) {
			remove(current);
		}

		if (component != null) {
			if (component instanceof HasSize) {
				((HasSize) component).setWidthFull();
			}
			// VaadinFrontend.focusFirstComponent(component);
		}
		this.current = component;
	}

}
