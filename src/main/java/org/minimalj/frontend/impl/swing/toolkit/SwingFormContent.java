package org.minimalj.frontend.impl.swing.toolkit;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.LayoutManager2;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.JPanel;

import org.minimalj.frontend.Frontend.FormContent;
import org.minimalj.frontend.Frontend.IComponent;
import org.minimalj.frontend.form.element.FormElementConstraint;
import org.minimalj.frontend.impl.swing.component.SwingCaption;

public class SwingFormContent extends JPanel implements FormContent {
	private static final long serialVersionUID = 1L;

	private final Map<IComponent, SwingCaption> captionByComponent = new HashMap<>();
	
	public SwingFormContent(int columns, int columnWidthPercentage) {
		int columnWidth = getColumnWidth() * columnWidthPercentage / 100;
		setLayout(new GridFormLayoutManager(columns, columnWidth, 5));
		setBorder(null);
	}
	
	private int getColumnWidth() {
		FontMetrics fm = getFontMetrics(getFont());
		return (int)fm.getStringBounds("The quick brown fox jumps over the lazy dog", getGraphics()).getWidth();
	}

	@Override
	public void add(String caption, IComponent c, FormElementConstraint constraint, int span) {
		Component component = c != null ? (Component) c : new JPanel();
		if (caption != null) {
			SwingCaption swingCaption = new SwingCaption(component, caption);
			captionByComponent.put(c, swingCaption);
			add(swingCaption, new GridFormLayoutConstraint(span, constraint));
		} else {
			add(component, new GridFormLayoutConstraint(span, constraint));
		}
	}
	
	@Override
	public void setValidationMessages(IComponent component, List<String> validationMessages) {
		SwingCaption swingCaption = captionByComponent.get(component);
		if (swingCaption != null) {
			swingCaption.setValidationMessages(validationMessages);
		}
	}

	private static class GridFormLayoutConstraint {
	
		private final int span;
		private final FormElementConstraint formElementConstraint;
		
		public GridFormLayoutConstraint(int span, FormElementConstraint formElementConstraint) {
			super();
			this.span = span;
			this.formElementConstraint = formElementConstraint;
		}

		protected int getSpan() {
			return span;
		}

		protected boolean isVerticallyGrowing() {
			return formElementConstraint != null && formElementConstraint.max > 1;
		}

		public boolean isCompleteRow() {
			return span < 1;
		}
		
		public int getMax() {
			return formElementConstraint != null ? formElementConstraint.max : 1;
		}
		
	}
	
	private static class GridFormLayoutManager implements LayoutManager2 {

		private final int columns;
		private final int minColumnWidth;
		private final int ins;
		private final List<List<Component>> rows = new LinkedList<>();
		private final Map<Component, GridFormLayoutConstraint> constraints = new HashMap<>();
		
		private Dimension size;
		private Rectangle lastParentBounds = null;
		private int column = Integer.MAX_VALUE;
		
		public GridFormLayoutManager(int columns, int minColumnWidth, int ins) {
			this.columns = columns;
			this.minColumnWidth = minColumnWidth;
			this.ins = ins;
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
			
			int fixHeight = calcFixHeight(true);
			int fixHeightWithoutCaption = calcFixHeight(false);
			
			int y = ins;
			int width = parent.getWidth();
			int widthWithoutIns = width - 2 * ins;
			
			for (List<Component> row : rows) {
				int height;
				boolean hasCaption = hasCaption(row);
				if (isRowVerticallyGrowing(row)) {
					height = Math.max(getHeight(row, fixHeight), fixHeight);
				} else {
					height = hasCaption ? fixHeight : fixHeightWithoutCaption;
				}
				layoutRow(widthWithoutIns, row, y, height, hasCaption ? fixHeight : fixHeightWithoutCaption);
				y += height;
			}
			y+= ins;
			size = new Dimension(Math.max(minColumnWidth * columns, width), y);
		}

		private void layoutRow(int width, List<Component> row, int y, int height, int minimalHeight) {
			int x = ins;
			for (Component component : row) {
				component.setLocation(x, y);
				GridFormLayoutConstraint constraint = constraints.get(component);
				int componentWidth = constraint.isCompleteRow() ? width : constraint.getSpan() * width / columns;
				x += componentWidth;
				if (constraint.isVerticallyGrowing()) {
					component.setSize(componentWidth, height);
				} else {
					// even non growing components are stretched to fixHeight (they should no collapse to 0 height)
					component.setSize(componentWidth, Math.max(component.getPreferredSize().height, minimalHeight));
				}
			}
		}
		
		private int getHeight(List<Component> row, int fixHeight) {
			int height = 0;
			for (Component component : row) {
				GridFormLayoutConstraint constraint = constraints.get(component);
				int componentHeight = Math.min(component.getPreferredSize().height, constraint.getMax() * fixHeight);
				height = Math.max(height, componentHeight);
			}
			return height;
		}
		
		private boolean isRowVerticallyGrowing(List<Component> row) {
			for (Component component : row) {
				GridFormLayoutConstraint constraint = constraints.get(component);
				if (constraint.isVerticallyGrowing()) {
					return true;
				}
			}
			return false;
		}

		private boolean hasCaption(List<Component> row) {
			for (Component component : row) {
				if (component instanceof SwingCaption) {
					return true;
				}
			}
			return false;
		}
		
		private int calcFixHeight(boolean caption) {
			int height = 0;
			for (List<Component> row : rows) {
				for (Component component : row) {
					GridFormLayoutConstraint constraint = constraints.get(component);
					if (!constraint.isVerticallyGrowing()) {
						boolean hasCaption = component instanceof SwingCaption;
						if (hasCaption == caption) {
							height = Math.max(height, component.getPreferredSize().height);
						}
					}
				}
			}
			return height;
		}

		@Override
		public void addLayoutComponent(Component comp, Object constraint) {
			GridFormLayoutConstraint formConstraint = (GridFormLayoutConstraint) constraint;
			constraints.put(comp, formConstraint);
			List<Component> row;
			if (column >= columns) {
				row = new ArrayList<>();
				rows.add(row);
				column = 0;
			} else {
				row = rows.get(rows.size()-1);
			}
			row.add(comp);
			column = formConstraint.isCompleteRow() ? columns : column + formConstraint.getSpan();
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
	
}
