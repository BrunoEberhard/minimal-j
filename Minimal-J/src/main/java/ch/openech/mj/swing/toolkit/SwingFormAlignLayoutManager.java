package ch.openech.mj.swing.toolkit;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class SwingFormAlignLayoutManager implements LayoutManager {

	public SwingFormAlignLayoutManager() {
	}

	@Override
	public void addLayoutComponent(String name, Component comp) {
	}

	@Override
	public void removeLayoutComponent(Component comp) {
	}

	@Override
	public Dimension preferredLayoutSize(Container parent) {
		synchronized (parent.getTreeLock()) {
			Component component = parent.getComponent(0);
			Insets insets = parent.getInsets();
			return new Dimension(insets.left + insets.right + component.getPreferredSize().width, insets.top
					+ insets.bottom + component.getPreferredSize().height);
		}
	}

	@Override
	public Dimension minimumLayoutSize(Container parent) {
		synchronized (parent.getTreeLock()) {
			Component component = parent.getComponent(0);
			Insets insets = parent.getInsets();
			return new Dimension(insets.left + insets.right + component.getMinimumSize().width, insets.top
					+ insets.bottom + component.getMinimumSize().height);
		}
	}

	@Override
	public void layoutContainer(Container parent) {
		synchronized (parent.getTreeLock()) {
			if (parent.getComponentCount() < 1) {
				return;
			}
			
			Component component = parent.getComponent(0);
			Insets insets = parent.getInsets();
			int parentWidth = parent.getWidth() - insets.left - insets.right;
			int parentHeight = parent.getHeight() - insets.top - insets.bottom;
			int preferredWidth = component.getPreferredSize().width;
			int preferredHeight = component.getPreferredSize().height;

			int x, y, width, height;
			
			if (parentWidth <= preferredWidth) {
				x = insets.left;
				width = parentWidth;
			} else {
				int delta = parentWidth - preferredWidth;
				int additionalBorderLeft = Math.min(delta * 2 / 3, parentWidth / 10);
				x = insets.left + additionalBorderLeft;
				width = preferredWidth;
			}
			
			if (parentHeight <= preferredHeight) {
				y = insets.top;
				height = parentHeight;
			} else {
				int delta = parentHeight - preferredHeight;
				int additionalBorderTop = Math.min(delta / 4, parentHeight / 12);
				y = insets.top + additionalBorderTop;
				height = preferredHeight;
			}
			
			component.setBounds(x, y, width, height);
		}
	}

	public static void main(String... args) {
		JFrame frame = new JFrame("Test");
		frame.setLocationRelativeTo(null);
		frame.getContentPane().setLayout(new SwingFormAlignLayoutManager());
		JPanel panel = new JPanel();
		panel.setPreferredSize(new Dimension(500, 300));
		panel.setBackground(Color.GREEN);
		panel.setOpaque(true);
		frame.getContentPane().add(panel);
		frame.setVisible(true);
	}
	
}
