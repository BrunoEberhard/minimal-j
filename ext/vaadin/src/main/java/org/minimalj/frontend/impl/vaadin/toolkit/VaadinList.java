package org.minimalj.frontend.impl.vaadin.toolkit;

import java.util.ArrayList;
import java.util.List;

import org.minimalj.frontend.Frontend.IList;
import org.minimalj.frontend.action.Action;
import org.minimalj.frontend.impl.vaadin.toolkit.VaadinFrontend.VaadinActionLabel;
import org.minimalj.model.Rendering;

import com.vaadin.ui.Component;
import com.vaadin.ui.VerticalLayout;

public class VaadinList extends VerticalLayout implements IList {
	private static final long serialVersionUID = 1L;

	private final int actionCount;

	private List<Component> disabledChildren;
	
	public VaadinList(Action... actions) {
		setMargin(false);
		setSpacing(false);
		addStyleName("whiteBackground");
		if (actions != null) {
			for (Action action : actions) {
				addComponent(new VaadinFrontend.VaadinActionLabel(action));
			}
			actionCount = actions.length;
		} else {
			actionCount = 0;
		}
	}
	
	@Override
	public void setEnabled(boolean enabled) {
		if (isEnabled() && !enabled) {
			disabledChildren = new ArrayList<>(components);
			removeAllComponents();
		} else if (!isEnabled() && enabled) {
			for (Component c: disabledChildren) {
				addComponent(c);
			}
		} else {
			return; // avoid fireChange in super
		}
		super.setEnabled(enabled);
	}

	@Override
	public void clear() {
		for (int i = getComponentCount() - actionCount - 1; i >= 0; i--) {
			removeComponent(getComponent(i));
		}
		if (disabledChildren != null) {
			disabledChildren = disabledChildren.subList(disabledChildren.size() - actionCount, disabledChildren.size());
		}
	}

	@Override
	public void add(String title, Object object, Action... actions) {
		add(new VaadinText(title));
		add(object, actions);
	}

	@Override
	public void add(Object object, Action... actions) {
		boolean enabled = isEnabled();
		setEnabled(true);
		
		if (object != null) {
			Component component = object instanceof Rendering ? new VaadinText((Rendering) object) : new VaadinText(object.toString());
			super.addComponent((Component) component, getComponentCount() - actionCount); // empty string need otherwise LayoutManager doesn't get the component
		}
		for (Action action : actions) {
			super.addComponent(new VaadinActionLabel(action), getComponentCount() - actionCount);
		}
		
		setEnabled(enabled);
	}
	
}
