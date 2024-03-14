package org.minimalj.frontend.impl.swing.toolkit;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.RowSorter.SortKey;
import javax.swing.SortOrder;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.RowSorterEvent;
import javax.swing.event.RowSorterEvent.Type;
import javax.swing.event.RowSorterListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;

import org.minimalj.application.Configuration;
import org.minimalj.frontend.Frontend.ITable;
import org.minimalj.frontend.Frontend.TableActionListener;
import org.minimalj.frontend.impl.json.JsonTable;
import org.minimalj.frontend.impl.swing.component.SwingDecoration;
import org.minimalj.frontend.impl.util.ColumnFilter;
import org.minimalj.frontend.util.ListUtil;
import org.minimalj.model.Column;
import org.minimalj.model.Column.ColumnAlignment;
import org.minimalj.model.Keys;
import org.minimalj.model.Rendering;
import org.minimalj.model.Rendering.ColorName;
import org.minimalj.model.annotation.Width;
import org.minimalj.model.properties.Property;

import com.formdev.flatlaf.extras.components.FlatScrollPane;
import com.formdev.flatlaf.util.UIScale;

public class SwingTable<T> extends FlatScrollPane implements ITable<T> {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(SwingTable.class.getName());
	private static final int PAGE_SIZE = Integer.parseInt(Configuration.get("MjSwingTablePageSize", "1000"));
	
	private RenderingTableCellRenderer renderer;
	
	private final JTable table;
	private final ItemTableModel tableModel;
	private final TableActionListener<T> listener;
	private final JButton nextButton, prevButton;
	
	private List<T> list;
	private int page;
	private Object[] sortColumns = new Object[0];
	private boolean[] sortDirections = new boolean[0];
	
	public SwingTable(Object[] keys, boolean multiSelect, TableActionListener<T> listener) {
		this.listener = listener;
		
		tableModel = new ItemTableModel(keys);
		table = new JTable(tableModel);

		table.setSelectionMode(multiSelect ? ListSelectionModel.MULTIPLE_INTERVAL_SELECTION : ListSelectionModel.SINGLE_SELECTION);
		table.setRowSelectionAllowed(true);
		table.setFillsViewportHeight(true);
		
		updateBorder();

		table.setShowHorizontalLines(true);
		table.setShowVerticalLines(true);
		
		renderer = new RenderingTableCellRenderer();
		table.setDefaultRenderer(Boolean.class, renderer);
		table.setDefaultRenderer(Object.class, renderer);
		table.setDefaultRenderer(Number.class, renderer);
		
		table.setAutoCreateRowSorter(true);
		
		setViewportView(table);
		
		bindRowHeightToFont();

		table.addMouseListener(new SwingTableMouseListener());
		table.getSelectionModel().addListSelectionListener(new SwingTableSelectionListener());
        table.getRowSorter().addRowSorterListener(new SwingTableRowSortingListener());
        table.addMouseListener(new TableMouseListener());
        
        table.getTableHeader().setLayout(new BorderLayout());
        
        JPanel panel = new JPanel(new FlowLayout());
        panel.setOpaque(false);
        prevButton = SwingDecoration.createDecorationButton(SwingDecoration.Part.PREV);
        prevButton.addActionListener(e -> setPage(page - 1));
        prevButton.setVisible(false);
		panel.add(prevButton);
		nextButton = SwingDecoration.createDecorationButton(SwingDecoration.Part.NEXT);
		nextButton.addActionListener(e -> setPage(page + 1));
        nextButton.setVisible(false);
		panel.add(nextButton);
        table.getTableHeader().add(panel, BorderLayout.LINE_END);
        
        List<Property> properties = getProperties();
        for (int i = 0; i<properties.size(); i++) {
        	Width widthAnnotation = properties.get(i).getAnnotation(Width.class);
        	int width = widthAnnotation != null ? widthAnnotation.value() : Width.DEFAULT;
        	table.getColumnModel().getColumn(i).setPreferredWidth(width);
        	if (width < Width.DEFAULT) {
        		table.getColumnModel().getColumn(i).setMaxWidth(width * 2);
        	}
        }
	}

	@Override
	public void setColumns(Object[] keys) {
		tableModel.setColumns(keys);
	}

	private Object[] getKeys() {
		return tableModel.keys;
	}
	
	private List<Property> getProperties() {
		return tableModel.properties;
	}
	
	protected void updateBorder() {
		int inset = UIManager.getInt("Group.Inset");
		Border emptyBorder = BorderFactory.createEmptyBorder(inset, inset, inset, inset);
		Border lineBorder = BorderFactory.createLineBorder(UIManager.getColor("Group.BorderColor"));
		setBorder(BorderFactory.createCompoundBorder(emptyBorder, lineBorder));
	}
	
	private List<Property> convert(Object[] keys) {
		List<Property> properties = new ArrayList<>(keys.length);
		for (Object key : keys) {
			Property property = Keys.getProperty(key);
			if (property != null) {
				properties.add(property);
			} else {
				logger.log(Level.WARNING, "Key not a property: " + key);
			}
		}
		if (properties.size() == 0) {
			logger.log(Level.SEVERE, "table without valid keys");
		}
		return properties;
	}
	
	private void bindRowHeightToFont() {
		PropertyChangeListener listener = event -> table.setRowHeight(table.getFont().getSize() * 5 / 3 + 2);
		listener.propertyChange(null);
		table.addPropertyChangeListener("UI", listener);
	}

	@Override
	public void setObjects(List<T> list) {
		this.list = list;
		setPage(0);
	}
	
	private void setPage(int page) {
		List<T> selectedObjects = getSelectedObjects();
		
		this.page = page;
		
		List<T> objects = ListUtil.get(list, new ColumnFilter[0], sortColumns, sortDirections, page * PAGE_SIZE, PAGE_SIZE);
		tableModel.setObjects(objects);
		nextButton.setVisible(objects.size() > (page + 1) * PAGE_SIZE);
		prevButton.setVisible(page > 0);
		
		// redo selection
		for (int i = 0; i < table.getRowCount(); i++) {
			T object = tableModel.getObject(table.convertRowIndexToModel(i));
			for (T selectedObject : selectedObjects) {
				if (JsonTable.equalsByIdOrContent(object, selectedObject)) {
					table.setRowSelectionInterval(i, i);
				}
			}
		}
	}

	private List<T> getSelectedObjects() {
		List<T> selectedObjects = new ArrayList<>(table.getSelectedRowCount());
		for (int row : table.getSelectedRows()) {
			int rowInModel = table.convertRowIndexToModel(row);
			selectedObjects.add(tableModel.getObject(rowInModel));
		}
		return selectedObjects;
	}

	private class SwingTableMouseListener extends MouseAdapter {
		
		@Override
		public void mouseClicked(MouseEvent e) {
			if (e.getClickCount() >= 2 && listener != null) {
		        int row = table.rowAtPoint(e.getPoint());
		        if (e.getClickCount() == 2 && row >= 0) {
		        	int rowInModel = table.convertRowIndexToModel(row);
					SwingFrontend.run(SwingTable.this, () -> listener.action(tableModel.getObject(rowInModel)));
		        }
			}
		}
	}

	private class SwingTableSelectionListener implements ListSelectionListener {
		@Override
		public void valueChanged(ListSelectionEvent e) {
			if (!e.getValueIsAdjusting() && table.isShowing()) {
				SwingFrontend.run(SwingTable.this, () -> listener.selectionChanged(getSelectedObjects()));
			}
		}
	}
	
	private class SwingTableRowSortingListener implements RowSorterListener {
        @Override
        public void sorterChanged(RowSorterEvent e) {
        	if (e.getType() == Type.SORT_ORDER_CHANGED) {
				List<? extends SortKey> sortKeys = e.getSource().getSortKeys();
        		sortColumns = new Object[sortKeys.size()];
        		sortDirections = new boolean[sortKeys.size()];
        		int index = 0;
        		for (SortKey s : sortKeys) {
        			sortColumns[index] = getKeys()[s.getColumn()];
        			sortDirections[index] = s.getSortOrder() == SortOrder.ASCENDING;
        			index++;
        		}
        		setPage(0);
        	}
        }
    }
    
	public class ItemTableModel extends AbstractTableModel {

		private static final long serialVersionUID = 1L;
		private Object[] keys;
		private List<Property> properties;
		private int width;

		private List<T> objects = Collections.emptyList();
		
		public ItemTableModel(Object[] keys) {
			this.keys = keys;
			this.properties = convert(keys);
			updateWidth();
		}

		public void setObjects(List<T> objects) {
			this.objects = objects;
			fireTableDataChanged();
		}

		public void setColumns(Object[] keys) {
			this.keys = keys;
			this.properties = convert(keys);
			updateWidth();
			fireTableStructureChanged();
		}
		
		private void updateWidth() {
			width = 0;
			for (Property property : properties) {
				Width widthAnnotation = property.getAnnotation(Width.class);
				if (widthAnnotation != null) {
					width += widthAnnotation.value();
				} else {
					width += Width.DEFAULT;
				}
			}
		}
		
		public List<T> getObjects() {
			return objects;
		}
		
		public T getObject(int index) {
			return objects.get(index);
		}

		@Override
		public boolean isCellEditable(int row, int column) {
			return false;
		}

		@Override
		public String getColumnName(int column) {
			Property property = properties.get(column)
;			return Column.evalHeader(property);
		}

		@Override
		public Object getValueAt(int row, int column) {
			try {
				Object object = getObject(row);
				Property property = properties.get(column);
				return property.getValue(object);
			} catch (Exception x) {
				logger.severe("Couldn't get value for " + row + "/" + column + ": " + x.getMessage());
				return row + "/" + column + ": " + x.getMessage();
			}
		}

		@Override
		public int getRowCount() {
			return objects.size();
		}

		@Override
		public int getColumnCount() {
			return keys.length;
		}

		@Override
		public Class<?> getColumnClass(int columnIndex) {
			Class<?> clazz = properties.get(columnIndex).getClazz();
			if (Rendering.class.isAssignableFrom(clazz)) {
				return Rendering.class;
			} else {
				return clazz;
			}
		}
	}
	
	@Override
	public void updateUI() {
		super.updateUI();
		updateBorder();
		if (renderer != null) {
			renderer.updateUI();
		}
		if (table != null) {
			table.setBackground(UIManager.getColor("Table.background"));
			table.setGridColor(UIManager.getColor("Table.gridColor"));
		}
	}
	
	private class RenderingTableCellRenderer extends DefaultTableCellRenderer {

		private static final long serialVersionUID = 1L;
		private Color actionColor = UIManager.getColor("Action.forground");

		@SuppressWarnings({ "unchecked", "rawtypes" })
		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int columnIndex) {
			Object object = ((ItemTableModel) table.getModel()).getObject(table.convertRowIndexToModel(row));

			Color color = null;
			String stringValue;

			Property property = getProperties().get(columnIndex);

			if (property instanceof Column) {
				Column column = (Column) property;
				stringValue = Rendering.toString(column.render(object, value));
				if (column.isLink(object, value)) {
					color = actionColor;
				} else {
					color = getColor(Column.getColor(column, object, value));
				}
				ColumnAlignment alignment = column.getAlignment();
				if (alignment == ColumnAlignment.center) {
					setHorizontalAlignment(JLabel.CENTER);
				} else if (alignment == ColumnAlignment.end) {
					setHorizontalAlignment(JLabel.TRAILING);
				} else {
					setHorizontalAlignment(JLabel.LEADING);
				}
			} else {
				stringValue = Rendering.toString(value, property);
				color = getColor(Rendering.getColor(object, value));
				if (Number.class.isAssignableFrom(property.getClazz())) {
					setHorizontalAlignment(JLabel.TRAILING);
				} else {
					setHorizontalAlignment(JLabel.LEADING);
				}
			}

			if (!Objects.equals(getForeground(), color)) {
				setForeground(color);
			}
			
			Component c = super.getTableCellRendererComponent(table, stringValue, isSelected, hasFocus, row, columnIndex);
			if (property instanceof Column) {
				Column column = (Column) property;
				ColumnAlignment alignment = column.getAlignment();
				if (alignment == ColumnAlignment.center) {
					setHorizontalAlignment(JLabel.CENTER);
				} else if (alignment == ColumnAlignment.end) {
					setHorizontalAlignment(JLabel.TRAILING);
				} else {
					setHorizontalAlignment(JLabel.LEADING);
				}
			} else {
				if (Number.class.isAssignableFrom(property.getClazz())) {
					setHorizontalAlignment(JLabel.TRAILING);
				} else {
					setHorizontalAlignment(JLabel.LEADING);
				}
			}
			return c;
		}
	
		@Override
		public void updateUI() {
			super.updateUI();
			actionColor = UIManager.getColor("Action.forground");
		}
	}
	
	private static Color getColor(ColorName colorName) {
		if (colorName != null) {
			switch (colorName) {
				case RED: return Color.RED;
				case YELLOW: return Color.YELLOW;
				case GREEN: return Color.GREEN;
				case BLUE: return Color.BLUE;
				case GRAY: return Color.GRAY;
			}
		}
		return null;
	}

	private class TableMouseListener extends MouseAdapter {

		@SuppressWarnings({ "unchecked", "rawtypes" })
		@Override
		public void mouseClicked(java.awt.event.MouseEvent evt) {
			int rowView = table.rowAtPoint(evt.getPoint());
			int colView = table.columnAtPoint(evt.getPoint());
			if (rowView >= 0 && colView >= 0) {
				int row = table.convertRowIndexToModel(rowView);
				int col = table.convertColumnIndexToModel(colView);
				Property property = getProperties().get(col);
				if (property instanceof Column) {
					Column column = (Column) property;
					Object object = ((ItemTableModel) table.getModel()).getObject(row);
					SwingFrontend.run(table, () -> column.run(object));
				}
			}
		}
	}
	
	@Override
	public Dimension getMinimumSize() {
		Dimension header = table.getTableHeader().getMinimumSize();
		return new Dimension(0, header.height + 10 * table.getRowHeight());
	};

	@Override
	public Dimension getPreferredSize() {
		Rectangle bounds = getGraphicsConfiguration().getDevice().getDefaultConfiguration().getBounds();
		Dimension minimum = getMinimumSize();
		return new Dimension(Math.min(tableModel.width, UIScale.scale(bounds.width) - 100), minimum.height);
	}
	
	@Override
	public Dimension getMaximumSize() {
		Rectangle bounds = getGraphicsConfiguration().getDevice().getDefaultConfiguration().getBounds();
		return new Dimension(Math.min(tableModel.width, UIScale.scale(bounds.width) - 100), UIScale.scale(bounds.height) - 100);
	};

}
