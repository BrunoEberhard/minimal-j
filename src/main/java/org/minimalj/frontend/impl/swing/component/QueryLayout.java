package org.minimalj.frontend.impl.swing.component;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.LayoutManager2;
import java.awt.Rectangle;

public class QueryLayout implements LayoutManager2 {

	public enum QueryLayoutConstraint { CAPTION, TEXTFIELD };
	
	private Dimension size;
	private Rectangle lastParentBounds = null;
	private final int width;
	private final int margin = 10;
	private final int marginTop = 200;
	private Component caption;
	private Component textfield;

	public QueryLayout() {
		this(600);
	}
	
	public QueryLayout(int width) {
		this.width = width;
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
		if (lastParentBounds != null && lastParentBounds.equals(parent.getBounds())) {
			return;
		}
		lastParentBounds = parent.getBounds();

		if (caption != null) {
			layoutCaption(parent);
		}
		
		if (textfield != null) {
			layoutTextField(parent);
		}
	}

	private void layoutTextField(Container parent) {
		int height = textfield.getPreferredSize().height * 5 / 4;
		int width;
		int x;
		if (parent.getWidth() < this.width + 2 * margin) {
			width = parent.getWidth() - 2 * margin;
			x = margin;
		} else {
			width = this.width;
			x = (parent.getWidth() - width) / 2;
		}
		
		size = new Dimension(this.width, marginTop + textfield.getHeight());
		textfield.setBounds(x, marginTop, width, height);
	}
	
	private void layoutCaption(Container parent) {
		int height = caption.getPreferredSize().height;
		int width = caption.getPreferredSize().width;
		int x, y;
		if (parent.getWidth() < width + 2 * margin) {
			width = parent.getWidth() - 2 * margin;
			x = margin;
		} else {
			x = (parent.getWidth() - width) / 2;
		}
		if (marginTop < height) {
			y = 0;
		} else {
			y = marginTop - height - margin;
		}
		
		caption.setBounds(x, y, width, height);
	}
	
	
	
	@Override
	public void addLayoutComponent(Component comp, Object constraint) {
		if (constraint == QueryLayoutConstraint.CAPTION) {
			this.caption = comp;
		} else if (constraint == QueryLayoutConstraint.TEXTFIELD) {
			this.textfield = comp;
		} else {
			throw new IllegalArgumentException();
		}
		lastParentBounds = null;
	}

	@Override
	public Dimension maximumLayoutSize(Container target) {
		return new Dimension(30000, 30000);
	}

	@Override
	public void invalidateLayout(Container target) {
		lastParentBounds = null;
	}

	
	@Override
	public float getLayoutAlignmentX(Container target) {
		return 0;
	}

	@Override
	public float getLayoutAlignmentY(Container target) {
		return 0;
	}

	@Override
	public void addLayoutComponent(String name, Component comp) {
		// not used
	}

	@Override
	public void removeLayoutComponent(Component comp) {
		// not used
	}
}
