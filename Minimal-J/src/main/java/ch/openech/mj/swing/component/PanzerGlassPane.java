package ch.openech.mj.swing.component;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class PanzerGlassPane extends JPanel {
	private Component content;
	private boolean blocked;
	private JLabel imageLabel = new JLabel();
	private Cursor waitCursor;
	private Cursor originalCursor;
	
	public PanzerGlassPane() {
		this(null);
	}
	
	public PanzerGlassPane(Component content) {
		super(new BorderLayout());
		setContent(content);
	}

	public Component getContent() {
		return content;
	}
	
	public void setContent(Component content) {
		this.content = content;
		updateContent();
	}
	
	private void updateContent() {
		removeAll();
		if (content != null) {
			if (blocked) {
				updateLabelImage();
				add(imageLabel, BorderLayout.CENTER);
			} else {
				add(content, BorderLayout.CENTER);
			}
		}
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public void layout() {
		super.layout();

		boolean changed = content != null && !content.getSize().equals(getSize());
		if (changed && blocked) {
			content.setSize(getSize());
			layoutTree(content);
			updateLabelImage();
		} 
	}
	
	private static void layoutTree(Component component) {
		component.doLayout();
		if (component instanceof Container) {
			Container container = (Container) component;
			for (Component child : container.getComponents()) {
				layoutTree(child);
			}
		}
	}
    
	public void setWaitCursor(Cursor cursor) {
		this.waitCursor = cursor;
	}

	public boolean isBlocked() {
		return blocked;
	}
	
	public void setBlocked(boolean blocked) {
		if (this.blocked == blocked) return;
		this.blocked = blocked;

		updateContent();
		if (blocked) {
			if (waitCursor != null) {
				originalCursor = getCursor();
				setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			}
		} else {
			if (originalCursor != null) {
				setCursor(originalCursor);
			}
		}
	}
	
	public void updateLabelImage() {
	    if (content != null && getWidth() != 0 && getHeight() != 0) {
	    	BufferedImage bi = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);
	    	Graphics2D g2 = bi.createGraphics();
		    content.paint(g2);
		    imageLabel.setIcon(new ImageIcon(bi));
		    validate();
	    }
	}
	
}