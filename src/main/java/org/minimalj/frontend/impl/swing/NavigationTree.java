package org.minimalj.frontend.impl.swing;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.LayoutManager;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

import org.minimalj.frontend.action.Action;
import org.minimalj.frontend.action.ActionGroup;
import org.minimalj.frontend.impl.swing.toolkit.SwingFrontend.SwingActionText;
import org.minimalj.frontend.impl.swing.toolkit.SwingText;


public class NavigationTree extends JPanel {
	private static final long serialVersionUID = 1L;

	public NavigationTree(List<Action> actions) {
		this(actions, true);
	}
	
	public NavigationTree(List<Action> actions, boolean root) {
		super(new VerticalLayoutManager());
		if (root) {
			setBorder(BorderFactory.createEmptyBorder(15, 20, 0, 0));
		} else {
			setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 0));
		}
		
		for (Action action : actions) {
			if (action instanceof ActionGroup) {
				add(new SwingText(action.getName()));
				ActionGroup actionGroup = (ActionGroup) action;
				add(new NavigationTree(actionGroup.getItems(), false));
			} else {
				add(new SwingActionText(action));
			}
		}
	}		

	public static class VerticalLayoutManager implements LayoutManager {

		private Dimension preferredSize = new Dimension(100, 0);
		
		public VerticalLayoutManager() {
		}

		@Override
		public Dimension preferredLayoutSize(Container parent) {
			layoutContainer(parent);
			return preferredSize;
		}

		@Override
		public Dimension minimumLayoutSize(Container parent) {
			layoutContainer(parent);
			return preferredSize;
		}

		@Override
		public void layoutContainer(Container parent) {
			int y = parent.getInsets().top;
			int x = parent.getInsets().left;
			int width = parent.getWidth();
			for (Component component : parent.getComponents()) {
				int height = component.getPreferredSize().height;
				component.setBounds(x, y, width, height);
				y += 4 + height;
			}
			preferredSize = new Dimension(100, y);
		}

		@Override
		public void addLayoutComponent(String name, Component comp) {
		}

		@Override
		public void removeLayoutComponent(Component comp) {
		}
	}
}
