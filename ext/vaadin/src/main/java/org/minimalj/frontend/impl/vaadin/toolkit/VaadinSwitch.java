package org.minimalj.frontend.impl.vaadin.toolkit;

import org.minimalj.frontend.Frontend.IComponent;
import org.minimalj.frontend.Frontend.IContent;
import org.minimalj.frontend.Frontend.SwitchComponent;
import org.minimalj.frontend.Frontend.SwitchContent;
import org.minimalj.frontend.impl.vaadin.toolkit.VaadinFrontend.HasCaption;
import org.minimalj.frontend.impl.vaadin.toolkit.VaadinFrontend.HasComponent;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.customfield.CustomField;

public class VaadinSwitch extends CustomField<Object> implements SwitchContent, SwitchComponent, HasCaption {
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
        if (component instanceof HasComponent) {
            show(((HasComponent) component).getComponent());
        } else {
            show((Component) component);
        }
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
            add(component);
			// VaadinFrontend.focusFirstComponent(component);
		}
		this.current = component;
	}

    @Override
    protected Object generateModelValue() {
        return null;
    }

    @Override
    protected void setPresentationValue(Object newPresentationValue) {
    }

}
