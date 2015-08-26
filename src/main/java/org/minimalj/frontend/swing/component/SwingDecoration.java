package org.minimalj.frontend.swing.component;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JPanel;

public class SwingDecoration extends JPanel {
	private static final long serialVersionUID = 1L;

	public static final boolean SHOW_MINIMIZE = true;
	public static final boolean HIDE_MINIMIZE = false;
	
	private final Component content;
	private final ActionListener closeListener;
	
	private JLabel titleLabel;
	
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
		
		if (minimize) {
			JButton button = new JButton();
			button.setFocusPainted(false);
			button.setMargin(new Insets(0,0,0,0));
			button.setIcon(new FrameButtonIcon(Part.WP_MINBUTTON));
			button.setMaximumSize(new Dimension(button.getIcon().getIconWidth(), button.getIcon().getIconHeight()));
			button.setPreferredSize(new Dimension(button.getIcon().getIconWidth(), button.getIcon().getIconHeight()));
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
			JButton button = new JButton();
			button.setFocusPainted(false);
			button.setMargin(new Insets(0, 0, 0, 0));
			button.setIcon(new FrameButtonIcon(Part.WP_CLOSEBUTTON));
			button.setMaximumSize(new Dimension(button.getIcon().getIconWidth(), button.getIcon().getIconHeight()));
			button.setPreferredSize(new Dimension(button.getIcon().getIconWidth(), button.getIcon().getIconHeight()));
			bar.add(button);
			button.addActionListener(closeListener);
		}
		
		bar.setPreferredSize(bar.getMinimumSize());
		
		return bar;
	}
	
	
	public enum Part { WP_CLOSEBUTTON, WP_MINBUTTON, WP_MAXBUTTON, WP_RESTOREBUTTON };

	private static class FrameButtonIcon implements Icon, Serializable {

        private Part part;

        private FrameButtonIcon(Part part) {
            this.part = part;
        }

        @Override
		public void paintIcon(Component c, Graphics g, int x0, int y0) {
            int width = getIconWidth();
            int height = getIconHeight();


                g.setColor(Color.black);
                int x = width / 12 + 2;
                int y = height / 5;
                int h = height - y * 2 - 1;
                int w = width * 3/4 -3;
                int thickness2 = Math.max(height / 8, 2);
                int thickness  = Math.max(width / 15, 1);
                if (part == Part.WP_CLOSEBUTTON) {
                    int lineWidth;
                    if      (width > 47) lineWidth = 6;
                    else if (width > 37) lineWidth = 5;
                    else if (width > 26) lineWidth = 4;
                    else if (width > 16) lineWidth = 3;
                    else if (width > 12) lineWidth = 2;
                    else                 lineWidth = 1;
                    y = height / 12 + 3;
                    if (lineWidth == 1) {
                        if (w % 2 == 1) { x++; w++; }
                        g.drawLine(x,     y, x+w-2, y+w-2);
                        g.drawLine(x+w-2, y, x,     y+w-2);
                    } else if (lineWidth == 2) {
                        if (w > 6) { x++; w--; }
                        g.drawLine(x,     y, x+w-2, y+w-2);
                        g.drawLine(x+w-2, y, x,     y+w-2);
                        g.drawLine(x+1,   y, x+w-1, y+w-2);
                        g.drawLine(x+w-1, y, x+1,   y+w-2);
                    } else {
                        x += 2; y++; w -= 2;
                        g.drawLine(x,     y,   x+w-1, y+w-1);
                        g.drawLine(x+w-1, y,   x,     y+w-1);
                        g.drawLine(x+1,   y,   x+w-1, y+w-2);
                        g.drawLine(x+w-2, y,   x,     y+w-2);
                        g.drawLine(x,     y+1, x+w-2, y+w-1);
                        g.drawLine(x+w-1, y+1, x+1,   y+w-1);
                        for (int i = 4; i <= lineWidth; i++) {
                            g.drawLine(x+i-2,   y,     x+w-1,   y+w-i+1);
                            g.drawLine(x,       y+i-2, x+w-i+1, y+w-1);
                            g.drawLine(x+w-i+1, y,     x,       y+w-i+1);
                            g.drawLine(x+w-1,   y+i-2, x+i-2,   y+w-1);
                        }
                    }
                } else if (part == Part.WP_MINBUTTON) {
                    g.fillRect(x, y+h-thickness2, w-w/3, thickness2);
                } else if (part == Part.WP_MAXBUTTON) {
                    g.fillRect(x, y, w, thickness2);
                    g.fillRect(x, y, thickness, h);
                    g.fillRect(x+w-thickness, y, thickness, h);
                    g.fillRect(x, y+h-thickness, w, thickness);
                } else if (part == Part.WP_RESTOREBUTTON) {
                    g.fillRect(x+w/3, y, w-w/3, thickness2);
                    g.fillRect(x+w/3, y, thickness, h/3);
                    g.fillRect(x+w-thickness, y, thickness, h-h/3);
                    g.fillRect(x+w-w/3, y+h-h/3-thickness, w/3, thickness);

                    g.fillRect(x, y+h/3, w-w/3, thickness2);
                    g.fillRect(x, y+h/3, thickness, h-h/3);
                    g.fillRect(x+w-w/3-thickness, y+h/3, thickness, h-h/3);
                    g.fillRect(x, y+h-thickness, w-w/3, thickness);
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
