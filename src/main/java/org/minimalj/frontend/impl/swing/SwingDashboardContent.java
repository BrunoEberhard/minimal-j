package org.minimalj.frontend.impl.swing;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

import org.minimalj.frontend.Frontend.IContent;
import org.minimalj.frontend.impl.swing.component.WrapLayout;
import org.minimalj.frontend.impl.swing.toolkit.SwingTable;
import org.minimalj.frontend.page.Page;

import com.formdev.flatlaf.extras.components.FlatScrollPane;

public class SwingDashboardContent extends FlatScrollPane implements IContent {
	private static final long serialVersionUID = 1L;

	public SwingDashboardContent(List<Page> dashes) {
		JPanel panel = new JPanel(new WrapLayout(FlowLayout.LEFT));
		for (Page dash : dashes) {
			JPanel container = new DashContainer();
			container.setPreferredSize(new Dimension(550, 350));
			JLabel label = new JLabel(dash.getTitle());
			label.setBorder(new EmptyBorder(5, 0, 10, 0));
			container.add(label, BorderLayout.NORTH);
			JComponent content = (JComponent) dash.getContent();
			if (content instanceof SwingTable) {
				((SwingTable) content).setUseGroupInset(false);
			}
			container.add(content, BorderLayout.CENTER);
			panel.add(container);
		}
		setViewportView(panel);
		getVerticalScrollBar().setUnitIncrement(20);
	}

	private static class DashContainer extends JPanel {
		private static final long serialVersionUID = 1L;
		private static int arcSize = UIManager.getInt("Group.ArcSize");

		public DashContainer() {
			super(new BorderLayout());
			setBorder(new EmptyBorder(10, 10, 10, 10));
		}

		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			int width = getWidth();
			int height = getHeight();
			Graphics2D graphics = (Graphics2D) g;
			graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

			graphics.setColor(UIManager.getColor("Group.Background"));
			graphics.fillRoundRect(0, 0, width - 1, height - 1, arcSize, arcSize);
			graphics.setColor(UIManager.getColor("Group.BorderColor"));
			graphics.drawRoundRect(0, 0, width - 1, height - 1, arcSize, arcSize);
		}
	}

}
