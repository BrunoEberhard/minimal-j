package org.minimalj.frontend.impl.vaadin.toolkit;

import java.util.ArrayList;
import java.util.List;

import org.minimalj.frontend.Frontend.IComponent;
import org.minimalj.frontend.Frontend.IList;
import org.minimalj.frontend.action.Action;
import org.minimalj.frontend.impl.vaadin.toolkit.VaadinFrontend.VaadinActionLabel;

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
	public void add(IComponent component, Action... actions) {
		boolean enabled = isEnabled();
		setEnabled(true);
		
		int existingComponents = getComponentCount();
		super.addComponent((Component) component, getComponentCount() - actionCount); // empty string need otherwise LayoutManager doesn't get the component
		for (Action action : actions) {
			super.addComponent(new VaadinActionLabel(action), getComponentCount() - actionCount);
		}
//		if (actionCount > 0) {
//			// if global actions exist: create border at the end of this component+actions
//			JComponent lastLabel = (JComponent) super.getComponent(getComponentCount() - actionCount - 1);
//			lastLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));
//		} else if (existingComponents > 0) {
//			// no global actions, but not the first add: create border at the end of the previous component+actions 
//			JComponent lastLabel = (JComponent) super.getComponent(existingComponents - 1);
//			lastLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));
//		}
		
		setEnabled(enabled);
	}
	
}
