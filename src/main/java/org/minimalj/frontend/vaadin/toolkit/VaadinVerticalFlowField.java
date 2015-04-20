package org.minimalj.frontend.vaadin.toolkit;

import org.minimalj.frontend.toolkit.FlowField;
import org.minimalj.frontend.toolkit.ClientToolkit.IComponent;

import com.vaadin.ui.Component;
import com.vaadin.ui.VerticalLayout;

public class VaadinVerticalFlowField extends VerticalLayout implements FlowField {
	private static final long serialVersionUID = 1L;

	public VaadinVerticalFlowField() {
		addStyleName("whiteBackground");
	}
	
	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
	}

	@Override
	public void clear() {
		removeAllComponents();
	}

	@Override
	public void add(IComponent component) {
		addComponent((Component) component);
	}
	
	@Override
	public void addGap() {
		VaadinReadOnlyTextField field = new VaadinReadOnlyTextField();
		field.setValue(null);
		addComponent(field);
	}

	
}
