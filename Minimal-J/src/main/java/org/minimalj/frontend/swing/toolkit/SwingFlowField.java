package org.minimalj.frontend.swing.toolkit;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.LayoutManager;
import java.awt.Rectangle;
import java.util.LinkedList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;

import org.minimalj.frontend.toolkit.FlowField;
import org.minimalj.frontend.toolkit.IComponent;


public class SwingFlowField extends JPanel implements FlowField {
	private static final long serialVersionUID = 1L;
	
	private JLabel lastLabel;
	
	public SwingFlowField() {
		super(new FlowLayoutManager());
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
		removeAll();
		repaint();
		revalidate();
	}

	@Override
	public void addGap() {
		if (lastLabel != null) {
			lastLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));
		}
	}

	@Override
	public void add(IComponent component) {
		super.add((Component) component, ""); // empty string need otherwise LayoutManager doesn't get the component
		repaint();
		revalidate();
	}
	
	private static class FlowLayoutManager implements LayoutManager {

		private final List<Component> components = new LinkedList<>();
		private Dimension size;
		private Rectangle lastParentBounds = null;
		
		public FlowLayoutManager() {
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
			for (Component component : components) {
				int height = component.getPreferredSize().height;
				component.setBounds(x, y, widthWithoutIns, height);
				y += height;
			}
			size = new Dimension(width, y);
		}

		@Override
		public void addLayoutComponent(String name, Component comp) {
			components.add(comp);
			lastParentBounds = null;
		}

		@Override
		public void removeLayoutComponent(Component comp) {
			components.remove(comp);
			lastParentBounds = null;
		}
	}
}
