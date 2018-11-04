package org.minimalj.frontend.impl.lanterna.toolkit;

import java.util.List;

import org.minimalj.frontend.Frontend.IComponent;
import org.minimalj.frontend.Frontend.IList;
import org.minimalj.frontend.action.Action;
import org.minimalj.frontend.impl.lanterna.toolkit.LanternaFrontend.LanternaActionText;

import com.googlecode.lanterna.gui2.Component;
import com.googlecode.lanterna.gui2.Direction;
import com.googlecode.lanterna.gui2.LinearLayout;
import com.googlecode.lanterna.gui2.Panel;

public class LanternaList extends Panel implements IList {
	
	private final int actionCount;
	
	public LanternaList(Action... actions) {
		setLayoutManager(new LinearLayout(Direction.VERTICAL));
		if (actions != null) {
			for (Action action : actions) {
				addComponent(new LanternaActionText(action));
			}
			actionCount = actions.length;
		} else {
			actionCount = 0;
		}
	}
	
	@Override
	public void clear() {
		List<Component> children = (List<Component>) getChildren();
		for (int i = children.size() - actionCount - 1; i>=0; i--) {
			removeComponent(children.get(i));
		}
	}

	@Override
	public void setEnabled(boolean enabled) {	
		// TODO
		
	}

	@Override
	public void add(IComponent component, Action... actions) {
		super.addComponent((Component) component);
		// TODO actions
	}

}
