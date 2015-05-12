package org.minimalj.frontend.swing.toolkit;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.LayoutManager;
import java.awt.Rectangle;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.UIManager;

import org.minimalj.frontend.swing.toolkit.SwingClientToolkit.SwingActionLabel;
import org.minimalj.frontend.toolkit.Action;
import org.minimalj.frontend.toolkit.IList;


public class SwingList extends JPanel implements IList {
	private static final long serialVersionUID = 1L;
	
	private final int actionCount;
	
	public SwingList(Action... actions) {
		super(null, true);
		setLayout(new VerticalLayoutManager());
		if (actions != null) {
			for (Action action : actions) {
				add(new SwingActionLabel(action), "");
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
		super.setEnabled(enabled);
	}

	@Override
	public void clear() {
		for (int i = getComponentCount() - actionCount - 1; i>=0; i--) {
			remove(i);
		}
		repaint();
		revalidate();
	}

	@Override
	public void add(Object object, Action... actions) {
		JComponent label = (object instanceof Action) ? new SwingActionLabel((Action) object) : new SwingLabel(object);
		super.add(label, "", getComponentCount() - actionCount); // empty string need otherwise LayoutManager doesn't get the component
		for (Action action : actions) {
			super.add(new SwingActionLabel(action), "", getComponentCount() - actionCount);
		}
		JComponent lastLabel = (JComponent) super.getComponent(super.getComponentCount()-1);
		lastLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));
		
		repaint();
		revalidate();
	}

	private class VerticalLayoutManager implements LayoutManager {

		private Dimension size;
		private Rectangle lastParentBounds = null;
		
		public VerticalLayoutManager() {
		}

		@Override
		public Dimension preferredLayoutSize(Container parent) {
			layoutContainer(parent);
			return size;
		}

		@Override
		public Dimension minimumLayoutSize(Container parent) {
			layoutContainer(parent);
			return size;
		}

		@Override
		public void layoutContainer(Container parent) {
			if (lastParentBounds != null && lastParentBounds.equals(parent.getBounds())) return;
			lastParentBounds = parent.getBounds();
			
			int y = 4;
			int x = 1;
			int width = parent.getWidth();
			int widthWithoutIns = width - x;
			for (Component component : getComponents()) {
				int height = component.getPreferredSize().height;
				component.setBounds(x, y, widthWithoutIns, height);
				y += height;
			}
			size = new Dimension(width, y);
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
