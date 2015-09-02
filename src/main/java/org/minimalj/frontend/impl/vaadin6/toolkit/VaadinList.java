package org.minimalj.frontend.impl.vaadin6.toolkit;

import org.minimalj.frontend.Frontend.IList;
import org.minimalj.frontend.action.Action;
import org.minimalj.frontend.impl.vaadin6.toolkit.VaadinFrontend.VaadinActionLabel;

import com.vaadin.ui.Component;
import com.vaadin.ui.VerticalLayout;

public class VaadinList extends VerticalLayout implements IList {
	private static final long serialVersionUID = 1L;

	private final int actionCount;
	
	public VaadinList(Action... actions) {
		addStyleName("whiteBackground");
		
		if (actions != null) {
			for (Action action : actions) {
				addComponent(new VaadinActionLabel(action));
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
		for (int i = getComponentCount() - actionCount - 1; i>=0; i--) {
			removeComponent(getComponent(i));
		}
	}

	@Override
	public void add(Object object, Action... actions) {
		Component label = (object instanceof Action) ? new VaadinActionLabel((Action) object) : new VaadinLabel(object);
		addComponent(label, getComponentCount() - actionCount);
		for (Action action : actions) {
			addComponent(new VaadinActionLabel(action), getComponentCount() - actionCount);
		}
		addComponent(new VaadinLabel(""), getComponentCount() - actionCount);
	}
	
}
