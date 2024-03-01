package org.minimalj.frontend.impl.swing.toolkit;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.LayoutManager;

import javax.swing.JComponent;
import javax.swing.JPanel;

import org.minimalj.frontend.Frontend.IComponent;


public class SwingVerticalGroup extends JPanel implements IComponent {
	private static final long serialVersionUID = 1L;
	
	public SwingVerticalGroup(IComponent... components) {
		setLayout(new VerticalLayoutManager());

		for (IComponent c : components) {
			JComponent component = (JComponent)c;
			component.setOpaque(false);
			add(component);
		}
	}

	private static class VerticalLayoutManager implements LayoutManager {

		private Dimension preferredSize;
		
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
			int preferredHeight = 0;
			for (Component component : parent.getComponents()) {
				int height = component.getPreferredSize().height;
				preferredHeight += height;
			}
			int y = 0;
			int width = parent.getWidth();
			for (Component component : parent.getComponents()) {
				int height = component.getPreferredSize().height;
				component.setBounds(0, y, width, height);
				y += height;
			}
			preferredSize = new Dimension(width, preferredHeight);
		}

		@Override
		public void addLayoutComponent(String name, Component comp) {
		}

		@Override
		public void removeLayoutComponent(Component comp) {
		}
	}
}
