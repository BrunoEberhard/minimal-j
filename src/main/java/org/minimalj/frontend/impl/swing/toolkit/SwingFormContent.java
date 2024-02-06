package org.minimalj.frontend.impl.swing.toolkit;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Insets;
import java.awt.LayoutManager2;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.UIManager;

import org.minimalj.frontend.Frontend.FormContent;
import org.minimalj.frontend.Frontend.IComponent;
import org.minimalj.frontend.form.element.FormElementConstraint;
import org.minimalj.frontend.impl.swing.component.SwingCaption;

public class SwingFormContent extends JPanel implements FormContent {
	private static final long serialVersionUID = 1L;

	private final Map<IComponent, SwingCaption> captionByComponent = new HashMap<>();
	
	public SwingFormContent(int columns, int columnWidthPercentage) {
		int columnWidth = getColumnWidth() * columnWidthPercentage / 100;
		setLayout(new GridFormLayoutManager(columns, columnWidth));
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
	
	@Override
	public void setVisible(IComponent component, boolean visible) {
		((Component) component).setVisible(visible);
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
		
		public int getMin() {
			return formElementConstraint != null ? Math.max(formElementConstraint.min, 1) : 1;
		}
		
	}
	
	public static int getFixHeight() {
		return GridFormLayoutManager.fixHeightWithoutCaption;
	}
	
	private static class GridFormLayoutManager implements LayoutManager2 {

		private static int fixHeight, fixHeightWithoutCaption;
		
		static {
			updateFixHeight();
			UIManager.addPropertyChangeListener(evt -> updateFixHeight());
		}
		
		static void updateFixHeight() {
			fixHeight = new SwingCaption(new JComboBox<>(), "X").getPreferredSize().height;
			fixHeightWithoutCaption = new JComboBox<>().getPreferredSize().height;
		}
		
		private final int columns;
		private final int minColumnWidth;
		private final List<List<Component>> rows = new LinkedList<>();
		private final Map<Component, GridFormLayoutConstraint> constraints = new HashMap<>();
		private int padding = 5;
		
		private Dimension size;
		private Rectangle lastParentBounds = null;
		private int column = Integer.MAX_VALUE;
		private Insets insets;
		
		public GridFormLayoutManager(int columns, int minColumnWidth) {
			this.columns = columns;
			this.minColumnWidth = minColumnWidth;
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
			
			insets = parent.getInsets();
			int y = insets.top;
			int width = parent.getWidth() - insets.left - insets.right;
			
			for (List<Component> row : rows) {
				if (isRowVisible(row)) {
					boolean hasCaption = hasCaption(row);
					int height = layoutRow(width, row, y, hasCaption);
					y += height + padding;
				}
			}
			y+= padding;
			size = new Dimension(Math.max(minColumnWidth * columns, width), Math.max(25, y));
		}

		private boolean isRowVisible(List<Component> row) {
			return row.stream().anyMatch(c -> c.isVisible());
		}
		
		private int layoutRow(int width, List<Component> row, int y, boolean hasCaption) {
			int rowHeight = 0;
			int column = 0;
			for (Component component : row) {
				int x = insets.left + column * (width + padding) / columns;
				component.setLocation(x, y);
				GridFormLayoutConstraint constraint = constraints.get(component);
				int componentWidth = constraint.isCompleteRow() ? width : (constraint.getSpan() * width + padding) / columns - padding;
				
				int height = component instanceof SwingCaption ? fixHeight : fixHeightWithoutCaption;
				int minHeight = height + (constraint.getMin() -1) * fixHeightWithoutCaption;
				int maxHeight = height + (constraint.getMax() -1) * fixHeightWithoutCaption;

				Dimension preferredSize = component.getPreferredSize();
				height = Math.min(Math.max(minHeight, preferredSize.height), maxHeight);
				component.setSize(componentWidth, height);
				if (height > rowHeight) {
					rowHeight = height;
				}
				column += constraint.getSpan();
			}
			return rowHeight;
		}

		private boolean hasCaption(List<Component> row) {
			for (Component component : row) {
				if (component instanceof SwingCaption) {
					return true;
				}
			}
			return false;
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
