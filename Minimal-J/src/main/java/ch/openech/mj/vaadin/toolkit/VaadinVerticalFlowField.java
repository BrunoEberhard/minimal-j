package ch.openech.mj.vaadin.toolkit;

import ch.openech.mj.toolkit.FlowField;
import ch.openech.mj.toolkit.IComponent;

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
		field.setText(null);
		addComponent(field);
	}

	
}
