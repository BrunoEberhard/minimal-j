package org.minimalj.frontend.swing.toolkit;

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

import org.minimalj.frontend.toolkit.FormContent;
import org.minimalj.frontend.toolkit.ClientToolkit.IComponent;

public class SwingGridFormLayout extends JPanel implements FormContent {
	private static final long serialVersionUID = 1L;
	
	private final int columnWidth;
	
	public SwingGridFormLayout(int columns, int columnWidthPercentage) {
		columnWidth = getColumnWidth() * columnWidthPercentage / 100;
		setLayout(new GridFormLayoutManager(columns, columnWidth, 5));
		setBorder(null);
	}
	
	private int getColumnWidth() {
		FontMetrics fm = getFontMetrics(getFont());
		return (int)fm.getStringBounds("The quick brown fox jumps over the lazy dog", getGraphics()).getWidth();
	}

	@Override
	public void add(IComponent c, int span) {
		Component component = (Component) c;
		add(component, new GridFormLayoutConstraint(span, SwingClientToolkit.verticallyGrowing(component)));
	}

	private static class GridFormLayoutConstraint {
	
		private final int span;
		private final boolean verticallyGrowing;
		
		public GridFormLayoutConstraint(int span, boolean verticallyGrowing) {
			super();
			this.span = span;
			this.verticallyGrowing = verticallyGrowing;
		}

		protected int getSpan() {
			return span;
		}

		protected boolean isVerticallyGrowing() {
			return verticallyGrowing;
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
			
			int fixHeight = calcFixHeight();
			int y = ins;
			int width = parent.getWidth();
			int widthWithoutIns = width - 2 * ins;
			for (List<Component> row : rows) {
				int height = isRowVerticallyGrowing(row) ? Math.max(getHeight(row), fixHeight) : fixHeight;
				layoutRow(widthWithoutIns, row, y, height);
				y += height;
			}
			y+= ins;
			size = new Dimension(Math.max(minColumnWidth * columns, width), y);
		}

		private void layoutRow(int width, List<Component> row, int y, int height) {
			int x = ins;
			for (Component component : row) {
				component.setLocation(x, y);
				GridFormLayoutConstraint constraint = constraints.get(component);
				int componentWidth = constraint.getSpan() * width / columns;
				x += componentWidth; 
				component.setSize(componentWidth, height);
			}
		}
		
		private int getHeight(List<Component> row) {
			int height = 0;
			for (Component component : row) {
				height = Math.max(height, component.getPreferredSize().height);
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
		
		private int calcFixHeight() {
			int height = 0;
			for (List<Component> row : rows) {
				for (Component component : row) {
					if (!SwingClientToolkit.verticallyGrowing(component)) {
						height = Math.max(height, component.getPreferredSize().height);
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
			column += formConstraint.getSpan();
			lastParentBounds = null;
		}

		@Override
		public Dimension maximumLayoutSize(Container target) {
			layoutContainer(target);
			return size;

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
