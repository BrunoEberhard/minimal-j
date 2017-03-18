package org.minimalj.frontend.impl.swing.component;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Line2D;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JPanel;

import org.minimalj.frontend.page.Parts;

public class SwingDecoration extends JPanel {
	private static final long serialVersionUID = 1L;

	public static final boolean SHOW_MINIMIZE = true;
	public static final boolean HIDE_MINIMIZE = false;
	
	private final Component content;
	private final ActionListener closeListener;
	
	private JLabel titleLabel;
	private JPanel partNumbersComponent;
	
	public SwingDecoration(String title, Component content, boolean minimize, ActionListener closeListener) {
		super(new BorderLayout());
		this.content = content;
		this.closeListener = closeListener;
		
		add(createBar(title, minimize), BorderLayout.NORTH);
		
		content.setVisible(true);
		add(content, BorderLayout.CENTER);
	}
	
	public void setContentVisible() {
		content.setVisible(true);
		content.getParent().revalidate();
		content.getParent().repaint();
	}
	
	public void setTitle(String title) {
		titleLabel.setText(title);
	}
	
	private Component createBar(String title, boolean minimize) {
		JMenuBar bar = new JMenuBar();
		
		titleLabel = new JLabel(title);
		titleLabel.setBorder(BorderFactory.createEmptyBorder(2, 3, 2, 0));
		bar.add(titleLabel);

			
		bar.add(Box.createHorizontalGlue());

		partNumbersComponent = new JPanel();
		partNumbersComponent.setOpaque(false);
		partNumbersComponent.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));
		partNumbersComponent.setLayout(new FlowLayout(FlowLayout.RIGHT));
		bar.add(partNumbersComponent);
		
		if (minimize) {
			JButton button = createDecorationButton(Part.WP_MINBUTTON);
			bar.add(button);
			
			button.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					content.setVisible(!content.isShowing());
					content.getParent().revalidate();
					content.getParent().repaint();
				}
			});
		}
		
		if (closeListener != null) {
			JButton button = createDecorationButton(Part.WP_CLOSEBUTTON);
			bar.add(button);
			button.addActionListener(closeListener);
		}
		
		bar.setPreferredSize(bar.getMinimumSize());
		
		return bar;
	}
	
	public static JButton createDecorationButton(Part part) {
		JButton button = new JButton(new DecorationButtonIcon(part));
		button.setFocusPainted(false);
		button.setMargin(new Insets(0, 0, 0, 0));
		button.setMaximumSize(new Dimension(button.getIcon().getIconWidth(), button.getIcon().getIconHeight()));
		button.setPreferredSize(new Dimension(button.getIcon().getIconWidth(), button.getIcon().getIconHeight()));
		return button;
	}
	
	public void minimize() {
		if (content.isVisible()) {
			content.setVisible(false);
			content.getParent().revalidate();
			content.getParent().repaint();
		}
	}
	
	public void setParts(Parts parts) {
		if (parts != null) {
			partNumbersComponent.setVisible(true);
			updatePartNumbers(parts);
		} else {
			partNumbersComponent.setVisible(false);
		}
	}
	
	private void updatePartNumbers(Parts parts) {
		partNumbersComponent.removeAll();
		int start = Math.max(0, Math.min(parts.getCurrentPart() - 1, parts.getPartCount() - 3));
		int end = Math.min(parts.getPartCount() - 1, parts.getCurrentPart() + 3);
		for (int i = start; i < end; i++) {
			JLabel label = new JLabel(String.valueOf(i + 1));
			if (parts.getCurrentPart() == i) {
				label.setFont(label.getFont().deriveFont(Font.BOLD));
			}
			final int j = i;
			label.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					parts.setCurrentPart(j);
					updatePartNumbers(parts);
				}
			});
			partNumbersComponent.add(label);
		}
		partNumbersComponent.revalidate();
	}

	public enum Part { WP_CLOSEBUTTON, WP_MINBUTTON, PREV, NEXT };

	public static class DecorationButtonIcon implements Icon {

		private Part part;

		public DecorationButtonIcon(Part part) {
			this.part = part;
		}

		@Override
		public void paintIcon(Component c, Graphics g, int x0, int y0) {
			Graphics2D g2 = (Graphics2D) g;
			int width = getIconWidth();
			int height = getIconHeight();

			g.setColor(Color.black);
			g2.setStroke(new BasicStroke(Math.max(((float) height) / 12f, 1.4f)));
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

			float inset = width / 20f + ((part == Part.WP_CLOSEBUTTON || part == Part.WP_MINBUTTON) ? 4 : 5);
			float x = inset;
			float y = inset;
			float w = width - 2 * inset;

			if (part == Part.WP_CLOSEBUTTON) {
				g2.draw(new Line2D.Float(x, y, x + w, y + w));
				g2.draw(new Line2D.Float(x + w, y, x, y + w));
			} else if (part == Part.NEXT) {
				g2.draw(new Line2D.Float(x, y, x + w, y + w / 2));
				g2.draw(new Line2D.Float(x + w, y + w / 2, x, y + w));
			} else if (part == Part.PREV) {
				g2.draw(new Line2D.Float(x, y + w / 2, x + w, y + w));
				g2.draw(new Line2D.Float(x + w, y, x, y + w / 2));

			} else if (part == Part.WP_MINBUTTON) {
				g2.draw(new Line2D.Float(x + w / 3, y + w, x + w, y + w));
			}
		}

        @Override
		public int getIconWidth() {
            return 16;
        }

        @Override
		public int getIconHeight() {
            return 16;
        }
    }
}
