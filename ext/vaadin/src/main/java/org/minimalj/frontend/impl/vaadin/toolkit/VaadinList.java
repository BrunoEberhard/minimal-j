package org.minimalj.frontend.impl.vaadin.toolkit;

import org.minimalj.frontend.Frontend.IComponent;
import org.minimalj.frontend.Frontend.IList;
import org.minimalj.frontend.action.Action;

import com.vaadin.ui.Component;
import com.vaadin.ui.VerticalLayout;

public class VaadinList extends VerticalLayout implements IList {
	private static final long serialVersionUID = 1L;

	private final int actionCount;
	
	public VaadinList(Action... actions) {
		addStyleName("whiteBackground");
		if (actions != null) {
			for (Action action : actions) {
				// add(new SwingActionText(action), "");
				// TODO add actions
			}
			actionCount = actions.length;
		} else {
			actionCount = 0;
		}
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
	public void add(IComponent component, Action... actions) {
		addComponent((Component) component);
		
		// TODO add actions
	}
	
}
