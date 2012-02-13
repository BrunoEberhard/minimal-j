package ch.openech.mj.swing.component;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.util.ArrayList;
import java.util.List;

/**
 * A copy of the GridLayout but respecting the visible property of the
 * components
 */
public class VisibleGridLayout implements LayoutManager, java.io.Serializable {

	int hgap, vgap, rows, cols;

	/**
	 * Creates a grid layout with a default of one column per component, in a
	 * single row.
	 * 
	 */
	public VisibleGridLayout() {
		this(1, 0, 0, 0);
	}

	/**
	 * Creates a grid layout with the specified number of rows and columns. All
	 * components in the layout are given equal size.
	 * <p>
	 * One, but not both, of <code>rows</code> and <code>cols</code> can be
	 * zero, which means that any number of objects can be placed in a row or in
	 * a column.
	 * 
	 * @param rows
	 *            the rows, with the value zero meaning any number of rows.
	 * @param cols
	 *            the columns, with the value zero meaning any number of
	 *            columns.
	 */
	public VisibleGridLayout(int rows, int cols) {
		this(rows, cols, 0, 0);
	}

	/**
	 * Creates a grid layout with the specified number of rows and columns. All
	 * components in the layout are given equal size.
	 * <p>
	 * In addition, the horizontal and vertical gaps are set to the specified
	 * values. Horizontal gaps are placed between each of the columns. Vertical
	 * gaps are placed between each of the rows.
	 * <p>
	 * One, but not both, of <code>rows</code> and <code>cols</code> can be
	 * zero, which means that any number of objects can be placed in a row or in
	 * a column.
	 * <p>
	 * All <code>GridLayout</code> constructors defer to this one.
	 * 
	 * @param rows
	 *            the rows, with the value zero meaning any number of rows
	 * @param cols
	 *            the columns, with the value zero meaning any number of columns
	 * @param hgap
	 *            the horizontal gap
	 * @param vgap
	 *            the vertical gap
	 * @exception IllegalArgumentException
	 *                if the value of both <code>rows</code> and
	 *                <code>cols</code> is set to zero
	 */
	public VisibleGridLayout(int rows, int cols, int hgap, int vgap) {
		if (rows == 0 && this.cols == 0) {
			throw new IllegalArgumentException("rows and cols cannot both be zero");
		}
		this.rows = rows;
		this.cols = cols;
		this.hgap = hgap;
		this.vgap = vgap;
	}

	/**
	 * Gets the number of rows in this layout.
	 * 
	 * @return the number of rows in this layout
	 */
	public int getRows() {
		return rows;
	}

	/**
	 * Sets the number of rows in this layout to the specified value.
	 * 
	 * @param rows
	 *            the number of rows in this layout
	 * @exception IllegalArgumentException
	 *                if the value of both <code>rows</code> and
	 *                <code>cols</code> is set to zero
	 */
	public void setRows(int rows) {
		if (rows == 0 && this.cols == 0) {
			throw new IllegalArgumentException("rows and cols cannot both be zero");
		}
		this.rows = rows;
	}

	/**
	 * Gets the number of columns in this layout.
	 * 
	 * @return the number of columns in this layout
	 */
	public int getColumns() {
		return cols;
	}

	/**
	 * Sets the number of columns in this layout to the specified value. Setting
	 * the number of columns has no affect on the layout if the number of rows
	 * specified by a constructor or by the <tt>setRows</tt> method is non-zero.
	 * In that case, the number of columns displayed in the layout is determined
	 * by the total number of components and the number of rows specified.
	 * 
	 * @param cols
	 *            the number of columns in this layout
	 * @exception IllegalArgumentException
	 *                if the value of both <code>rows</code> and
	 *                <code>cols</code> is set to zero
	 */
	public void setColumns(int cols) {
		if ((cols == 0) && (this.rows == 0)) {
			throw new IllegalArgumentException("rows and cols cannot both be zero");
		}
		this.cols = cols;
	}

	/**
	 * Gets the horizontal gap between components.
	 * 
	 * @return the horizontal gap between components
	 */
	public int getHgap() {
		return hgap;
	}

	/**
	 * Sets the horizontal gap between components to the specified value.
	 * 
	 * @param hgap
	 *            the horizontal gap between components
	 */
	public void setHgap(int hgap) {
		this.hgap = hgap;
	}

	/**
	 * Gets the vertical gap between components.
	 * 
	 * @return the vertical gap between components
	 */
	public int getVgap() {
		return vgap;
	}

	/**
	 * Sets the vertical gap between components to the specified value.
	 * 
	 * @param vgap
	 *            the vertical gap between components
	 */
	public void setVgap(int vgap) {
		this.vgap = vgap;
	}

	/**
	 * Adds the specified component with the specified name to the layout.
	 * 
	 * @param name
	 *            the name of the component
	 * @param comp
	 *            the component to be added
	 */
	@Override
	public void addLayoutComponent(String name, Component comp) {
	}

	/**
	 * Removes the specified component from the layout.
	 * 
	 * @param comp
	 *            the component to be removed
	 */
	@Override
	public void removeLayoutComponent(Component comp) {
	}

	public int getVisibleComponentCount(Container parent) {
		int count = 0;
		for (Component component : parent.getComponents()) {
			if (component.isVisible()) {
				count++;
			}
		}
		return count;
	}

	private List<Component> getVisibleComponent(Container parent) {
		List<Component> visibleComponents = new ArrayList<Component>();
		for (Component component : parent.getComponents()) {
			if (component.isVisible()) {
				visibleComponents.add(component);
			}
		}
		return visibleComponents;
	}

	/**
	 * Determines the preferred size of the container argument using this grid
	 * layout.
	 * <p>
	 * The preferred width of a grid layout is the largest preferred width of
	 * all of the components in the container times the number of columns, plus
	 * the horizontal padding times the number of columns minus one, plus the
	 * left and right insets of the target container.
	 * <p>
	 * The preferred height of a grid layout is the largest preferred height of
	 * all of the components in the container times the number of rows, plus the
	 * vertical padding times the number of rows minus one, plus the top and
	 * bottom insets of the target container.
	 * 
	 * @param parent
	 *            the container in which to do the layout
	 * @return the preferred dimensions to lay out the subcomponents of the
	 *         specified container
	 * @see java.awt.GridLayout#minimumLayoutSize
	 * @see java.awt.Container#getPreferredSize()
	 */
	@Override
	public Dimension preferredLayoutSize(Container parent) {
		synchronized (parent.getTreeLock()) {
			Insets insets = parent.getInsets();
			List<Component> visibleComponents = getVisibleComponent(parent);
			int ncomponents = visibleComponents.size();
			int nrows = rows;
			int ncols = cols;

			if (nrows > 0) {
				ncols = (ncomponents + nrows - 1) / nrows;
			} else {
				nrows = (ncomponents + ncols - 1) / ncols;
			}
			int w = 0;
			int h = 0;
			for (Component comp : visibleComponents) {
				Dimension d = comp.getPreferredSize();
				if (w < d.width) {
					w = d.width;
				}
				if (h < d.height) {
					h = d.height;
				}
			}
			return new Dimension(insets.left + insets.right + ncols * w + (ncols - 1) * hgap, insets.top + insets.bottom + nrows * h + (nrows - 1) * vgap);
		}
	}

	/**
	 * Determines the minimum size of the container argument using this grid
	 * layout.
	 * <p>
	 * The minimum width of a grid layout is the largest minimum width of all of
	 * the components in the container times the number of columns, plus the
	 * horizontal padding times the number of columns minus one, plus the left
	 * and right insets of the target container.
	 * <p>
	 * The minimum height of a grid layout is the largest minimum height of all
	 * of the components in the container times the number of rows, plus the
	 * vertical padding times the number of rows minus one, plus the top and
	 * bottom insets of the target container.
	 * 
	 * @param parent
	 *            the container in which to do the layout
	 * @return the minimum dimensions needed to lay out the subcomponents of the
	 *         specified container
	 * @see java.awt.GridLayout#preferredLayoutSize
	 * @see java.awt.Container#doLayout
	 */
	@Override
	public Dimension minimumLayoutSize(Container parent) {
		synchronized (parent.getTreeLock()) {
			Insets insets = parent.getInsets();
			List<Component> visibleComponents = getVisibleComponent(parent);
			int ncomponents = visibleComponents.size();
			int nrows = rows;
			int ncols = cols;

			if (nrows > 0) {
				ncols = (ncomponents + nrows - 1) / nrows;
			} else {
				nrows = (ncomponents + ncols - 1) / ncols;
			}
			int w = 0;
			int h = 0;
			for (Component comp : visibleComponents) {
				Dimension d = comp.getMinimumSize();
				if (w < d.width) {
					w = d.width;
				}
				if (h < d.height) {
					h = d.height;
				}
			}
			return new Dimension(insets.left + insets.right + ncols * w + (ncols - 1) * hgap, insets.top + insets.bottom + nrows * h + (nrows - 1) * vgap);
		}
	}

	/**
	 * Lays out the specified container using this layout.
	 * <p>
	 * This method reshapes the components in the specified target container in
	 * order to satisfy the constraints of the <code>GridLayout</code> object.
	 * <p>
	 * The grid layout manager determines the size of individual components by
	 * dividing the free space in the container into equal-sized portions
	 * according to the number of rows and columns in the layout. The
	 * container's free space equals the container's size minus any insets and
	 * any specified horizontal or vertical gap. All components in a grid layout
	 * are given the same size.
	 * 
	 * @param parent
	 *            the container in which to do the layout
	 * @see java.awt.Container
	 * @see java.awt.Container#doLayout
	 */
	@Override
	public void layoutContainer(Container parent) {
		synchronized (parent.getTreeLock()) {
			Insets insets = parent.getInsets();
			List<Component> visibleCompoments = getVisibleComponent(parent);
			int ncomponents = visibleCompoments.size();
			int nrows = rows;
			int ncols = cols;
			boolean ltr = parent.getComponentOrientation().isLeftToRight();

			if (ncomponents == 0) {
				return;
			}
			if (nrows > 0) {
				ncols = (ncomponents + nrows - 1) / nrows;
			} else {
				nrows = (ncomponents + ncols - 1) / ncols;
			}
			int w = parent.getWidth() - (insets.left + insets.right);
			int h = parent.getHeight() - (insets.top + insets.bottom);
			w = (w - (ncols - 1) * hgap) / ncols;
			h = (h - (nrows - 1) * vgap) / nrows;

			if (ltr) {
				for (int c = 0, x = insets.left; c < ncols; c++, x += w + hgap) {
					for (int r = 0, y = insets.top; r < nrows; r++, y += h + vgap) {
						int i = r * ncols + c;
						if (i < ncomponents) {
							visibleCompoments.get(i).setBounds(x, y, w, h);
						}
					}
				}
			} else {
				for (int c = 0, x = parent.getWidth() - insets.right - w; c < ncols; c++, x -= w + hgap) {
					for (int r = 0, y = insets.top; r < nrows; r++, y += h + vgap) {
						int i = r * ncols + c;
						if (i < ncomponents) {
							visibleCompoments.get(i).setBounds(x, y, w, h);
						}
					}
				}
			}
		}
	}

	/**
	 * Returns the string representation of this grid layout's values.
	 * 
	 * @return a string representation of this grid layout
	 */
	@Override
	public String toString() {
		return getClass().getName() + "[hgap=" + hgap + ",vgap=" + vgap + ",rows=" + rows + ",cols=" + cols + "]";
	}
}
