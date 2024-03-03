package org.minimalj.frontend.impl.swing.toolkit;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.LayoutManager2;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.UIManager;

import org.minimalj.frontend.Frontend.FormContent;
import org.minimalj.frontend.Frontend.IComponent;
import org.minimalj.frontend.form.element.FormElementConstraint;
import org.minimalj.frontend.impl.swing.component.SwingCaption;

public class SwingFormContent extends JPanel implements FormContent {
	private static final long serialVersionUID = 1L;

	private final Map<IComponent, SwingCaption> captionByComponent = new HashMap<>();
	private final GridFormLayoutManager layoutManager;
	private boolean nested = false;
	private boolean ignoreCaption = false;

	public SwingFormContent(int columns, int columnWidthPercentage) {
		int columnWidth = getColumnWidth() * columnWidthPercentage / 100;
		setLayout(layoutManager = new GridFormLayoutManager(columns, columnWidth));
		setOpaque(true);
	}
	
	@Override
	public void addNotify() {
		super.addNotify();
		nested = false;
		Component parent = getParent();
		while (parent != null && !nested) {
			nested |= parent instanceof SwingFormContent;
			parent = parent.getParent();
		}
		int border = nested ? 0 : UIManager.getInt("Group.BorderSize");
		layoutManager.setBorder(border);
		setBorder(BorderFactory.createEmptyBorder(border, border, border, border));
	}

	private int getColumnWidth() {
		FontMetrics fm = getFontMetrics(getFont());
		return (int) fm.getStringBounds("The quick brown fox jumps over the lazy dog", getGraphics()).getWidth();
	}
	
	@Override
	public void setIgnoreCaption(boolean ignoreCaption) {
		this.ignoreCaption = ignoreCaption;
	}

	@Override
	public void add(String caption, IComponent c, FormElementConstraint constraint, int span) {
		if (layoutManager.getGroupedRows().isEmpty()) {
			layoutManager.group(null);
		}
		JComponent component = c != null ? (JComponent) c : new JPanel();
		component.setOpaque(false);
		if (!ignoreCaption) {
			SwingCaption swingCaption = new SwingCaption(component, caption != null ? caption : " ");
			captionByComponent.put(c, swingCaption);
			add(swingCaption, new GridFormLayoutConstraint(span, constraint));
		} else {
			add(component, new GridFormLayoutConstraint(span, constraint));
		}
	}

	@Override
	public void group(String caption) {
		layoutManager.group(caption);
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
		if (visible != ((Component) component).isVisible()) {
			((Component) component).setVisible(visible);
			revalidate();
			repaint();
		}
	}

	@Override
	protected void paintComponent(Graphics g) {
		if (!nested) {
			super.paintComponent(g);
			
			doLayout();
			
			Graphics2D g2 = (Graphics2D) g.create();
			RenderingHints qualityHints = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			qualityHints.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
			g2.setRenderingHints(qualityHints);

			if (layoutManager.getGroupedRows().isEmpty()) {
				paintGroupBackground(g, 0, 0, layoutManager.getRowCount());
			} else {
				for (int i = 0; i < layoutManager.getGroupedRows().size() - 1; i++) {
					paintGroupBackground(g, i, layoutManager.getGroupedRows().get(i), layoutManager.getGroupedRows().get(i + 1));
				}
				int i = layoutManager.getGroupedRows().size() - 1;
				paintGroupBackground(g, i, layoutManager.getGroupedRows().get(i), layoutManager.getRowCount());
			}
		} else {
			g.setColor(UIManager.getColor("Group.Background"));
			g.fillRect(0, 0, g.getClipBounds().width, g.getClipBounds().height);
		}
	}
	
	private void paintGroupBackground(Graphics g, int group, int startRow, int endRow) {
		if (!layoutManager.getGroupVisible().get(group)) {
			return;
		}
		int i = 0;
		int y = getInsets().top;
		for (int gr = 0; gr<group; gr++) {
			if (layoutManager.getGroupVisible().get(gr)) {
				y += 2 * layoutManager.getBorder();
			}
		}
		while (i < startRow) {
			if (layoutManager.getRowVisible().get(i)) {
				y+= layoutManager.getPadding();
			}
			y += layoutManager.getRowHeights().get(i++);
		}
		int height = 2 * layoutManager.getBorder() - layoutManager.getPadding();
		while (i < endRow) {
			if (layoutManager.getRowVisible().get(i)) {
				height+= layoutManager.getPadding();
			}
			height += layoutManager.getRowHeights().get(i++);
		}
		
		int arcSize = UIManager.getInt("Group.ArcSize");
		g.setColor(UIManager.getColor("Group.Background"));
		g.fillRoundRect(getInsets().left, y, getWidth() - getInsets().left - getInsets().right, height, arcSize, arcSize);

		g.setColor(UIManager.getColor("Group.BorderColor"));
		g.drawRoundRect(getInsets().left, y, getWidth() - getInsets().left - getInsets().right, height, arcSize, arcSize);
	}

	public void invalidate() {
		super.invalidate();
		layoutManager.lastParentBounds = null;
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
		private int padding = 2;
		private int border = 8;

		private Dimension size;
		private Rectangle lastParentBounds = null;
		private int column = Integer.MAX_VALUE;
		private Insets insets;
		private List<Integer> rowHeights = new ArrayList<>();

		private List<Integer> groupedRows = new ArrayList<>();
		private List<Boolean> groupVisible = new ArrayList<>();
		private List<Boolean> rowVisible = new ArrayList<>();
		
		public GridFormLayoutManager(int columns, int minColumnWidth) {
			this.columns = columns;
			this.minColumnWidth = minColumnWidth;
		}

		public void group(String caption) {
			if (!groupedRows.contains(getRowCount())) {
				groupedRows.add(getRowCount());
			}
		}

		public List<Integer> getGroupedRows() {
			return groupedRows;
		}

		public List<Boolean> getGroupVisible() {
			return groupVisible;
		}
		
		public List<Boolean> getRowVisible() {
			return rowVisible;
		}
		
		public int getRowCount() {
			return rows.size();
		}

		public List<Integer> getRowHeights() {
			return rowHeights;
		}

		public int getPadding() {
			return padding;
		}

		public int getBorder() {
			return border;
		}

		public void setBorder(int border) {
			this.border = border;
			lastParentBounds = null;
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
			rowHeights.clear();

			insets = parent.getInsets();
			int y = insets.top + border;
			int width = parent.getWidth() - insets.left - insets.right - 2 * border;

			boolean addBorder = false;
			groupVisible.clear();
			rowVisible.clear();
			for (List<Component> row : rows) {
				boolean isRowVisible = isRowVisible(row);
				rowVisible.add(isRowVisible);
				if (isRowVisible) {
					boolean hasCaption = hasCaption(row);
					int height = layoutRow(width, row, y, hasCaption);
					rowHeights.add(height);
					y += height + padding;
					addBorder = true;
				} else {
					rowHeights.add(0);
				}
				if (groupedRows.contains(rowHeights.size())) {
					groupVisible.add(addBorder);
					if (addBorder) {
						y += 2 * border;
					}
				}
			}
			groupVisible.add(addBorder);
			y += padding;
			size = new Dimension(Math.max(minColumnWidth * columns, width), Math.max(25, y));
			
			// TODO this seems wrong place to repaint but background sometimes needs repainting
			parent.repaint();
		}

		private boolean isRowVisible(List<Component> row) {
			return row.stream().anyMatch(c -> c.isVisible());
		}

		private int layoutRow(int width, List<Component> row, int y, boolean hasCaption) {
			int rowHeight = 0;
			int column = 0;
			for (Component component : row) {
				int x = insets.left + border + column * (width + padding) / columns;
				component.setLocation(x, y);
				GridFormLayoutConstraint constraint = constraints.get(component);
				int componentWidth = constraint.isCompleteRow() ? width : (constraint.getSpan() * width + padding) / columns - padding;
				int height = component instanceof SwingCaption ? fixHeight : fixHeightWithoutCaption;
				int minHeight = height + (constraint.getMin() - 1) * fixHeightWithoutCaption;
				int maxHeight = height + (constraint.getMax() - 1) * fixHeightWithoutCaption;

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
				row = rows.get(rows.size() - 1);
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
