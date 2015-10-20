package org.minimalj.frontend.impl.swing.toolkit;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.LayoutManager;
import java.awt.Rectangle;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.UIManager;

import org.minimalj.frontend.Frontend.IComponent;
import org.minimalj.frontend.Frontend.IList;
import org.minimalj.frontend.action.Action;
import org.minimalj.frontend.impl.swing.toolkit.SwingFrontend.SwingActionText;


public class SwingList extends JPanel implements IList {
	private static final long serialVersionUID = 1L;
	
	private final int actionCount;

	private Component[] disabledChildren;
	
	public SwingList(Action... actions) {
		super(null, true);
		setLayout(new VerticalLayoutManager());
		if (actions != null) {
			for (Action action : actions) {
				add(new SwingActionText(action), "");
			}
			actionCount = actions.length;
		} else {
			actionCount = 0;
		}
	}
	
	@Override
	public void updateUI() {
		super.updateUI();
		setBackground(UIManager.getColor("TextField.background"));
		setOpaque(true);
	}

	@Override
	public void setEnabled(boolean enabled) {
		if (isEnabled() && !enabled) {
			disabledChildren = getComponents();
			removeAll();
			revalidate();
		} else if (!isEnabled() && enabled) {
			for (Component c: disabledChildren) {
				add(c, "");
			}
			revalidate();
		}
		super.setEnabled(enabled);
	}

	@Override
	public void clear() {
		if (isEnabled()) {
			for (int i = getComponentCount() - actionCount - 1; i>=0; i--) {
				remove(i);
			}
		} else {
			throw new IllegalStateException("Not allowed to clear components while disabled");
		}
		revalidate();
	}

	@Override
	public void add(IComponent component, Action... actions) {
		if (!isEnabled()) {
			throw new IllegalStateException("Not allowed to add component while disabled");
		}
		int existingComponents = getComponentCount();
		super.add((Component) component, "", getComponentCount() - actionCount); // empty string need otherwise LayoutManager doesn't get the component
		for (Action action : actions) {
			super.add(new SwingActionText(action), "", getComponentCount() - actionCount);
		}
		if (actionCount > 0) {
			// if global actions exist: create border at the end of this component+actions
			JComponent lastLabel = (JComponent) super.getComponent(super.getComponentCount() - actionCount - 1);
			lastLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));
		} else if (existingComponents > 0) {
			// no global actions, but not the first add: create border at the end of the previous component+actions 
			JComponent lastLabel = (JComponent) super.getComponent(existingComponents - 1);
			lastLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));
		}
		
		repaint();
		revalidate();
	}

	private class VerticalLayoutManager implements LayoutManager {

		private Dimension preferredSize;
		private Rectangle lastParentBounds = null;
		
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
			if (lastParentBounds != null && lastParentBounds.equals(parent.getBounds())) return;
			lastParentBounds = parent.getBounds();

			int preferredHeight = 0;
			for (Component component : getComponents()) {
				int height = component.getPreferredSize().height;
				preferredHeight += height;
			}
			int verticalRest = parent.getHeight() - preferredHeight;
			int verticalInset = verticalRest > 8 ? 4 : verticalRest / 2;
			int y = verticalInset;
			int x = verticalInset > 0 ? 1 : 0;
			int width = parent.getWidth();
			int widthWithoutIns = width - x;
			for (Component component : getComponents()) {
				int height = component.getPreferredSize().height;
				component.setBounds(x, y, widthWithoutIns, height);
				y += height;
			}
			preferredSize = new Dimension(width, preferredHeight);
		}

		@Override
		public void addLayoutComponent(String name, Component comp) {
			lastParentBounds = null;
		}

		@Override
		public void removeLayoutComponent(Component comp) {
			lastParentBounds = null;
		}
	}
}
