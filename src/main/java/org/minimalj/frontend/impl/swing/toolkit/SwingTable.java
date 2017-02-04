package org.minimalj.frontend.impl.swing.toolkit;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;

import org.minimalj.frontend.Frontend.ITable;
import org.minimalj.frontend.Frontend.TableActionListener;
import org.minimalj.model.Keys;
import org.minimalj.model.Rendering;
import org.minimalj.model.Rendering.RenderType;
import org.minimalj.model.properties.PropertyInterface;
import org.minimalj.util.resources.Resources;

public class SwingTable<T> extends JScrollPane implements ITable<T> {

	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(SwingTable.class.getName());
	
	private final Object[] keys;
	private final List<PropertyInterface> properties;
	private final JTable table;
	private final ItemTableModel tableModel;
	private final TableActionListener<T> listener;
	
	public SwingTable(Object[] keys, boolean multiSelect, TableActionListener<T> listener) {
		this.keys = keys;
		this.listener = listener;
		
		this.properties = convert(keys);
		
		tableModel = new ItemTableModel();
		table = new JTable(tableModel);

		table.setSelectionMode(multiSelect ? ListSelectionModel.MULTIPLE_INTERVAL_SELECTION : ListSelectionModel.SINGLE_SELECTION);
		table.setRowSelectionAllowed(true);
		table.setFillsViewportHeight(true);
		
		setBorder(BorderFactory.createEmptyBorder());
		
//		setDefaultRenderer(BooleanFormat.class, new BooleanTableCellRenderer());
		table.setDefaultRenderer(Object.class, new RenderingTableCellRenderer());
		
		table.setAutoCreateRowSorter(true);
		
		setViewportView(table);
		
		bindRowHeightToFont();

		table.addMouseListener(new SwingTableMouseListener());
		table.getSelectionModel().addListSelectionListener(new SwingTableSelectionListener());
	}

	private List<PropertyInterface> convert(Object[] keys) {
		List<PropertyInterface> properties = new ArrayList<PropertyInterface>(keys.length);
		for (Object key : keys) {
			PropertyInterface property = Keys.getProperty(key);
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
		table.addPropertyChangeListener("UI", new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				table.setRowHeight(table.getFont().getSize() * 5 / 3 + 2);
			}
		});
	}

	@Override
	public void setObjects(List<T> objects) {
		tableModel.setObjects(objects);
	}

	public List<T> getSelectedObjects() {
		List<T> selectedIds = new ArrayList<>(table.getSelectedRowCount());
		for (int row : table.getSelectedRows()) {
			int rowInModel = table.convertRowIndexToModel(row);
			selectedIds.add(tableModel.getObject(rowInModel));
		}
		return selectedIds;
	}

	private class SwingTableMouseListener extends MouseAdapter {
		
		@Override
		public void mouseClicked(MouseEvent e) {
			if (e.getClickCount() >= 2 && listener != null) {
		        int row = table.rowAtPoint(e.getPoint());
		        if (e.getClickCount() == 2 && row >= 0) {
		        	int rowInModel = table.convertRowIndexToModel(row);
		        	SwingFrontend.runWithContext(() -> listener.action(tableModel.getObject(rowInModel)));
		        }
			}
		}
	}

	private class SwingTableSelectionListener implements ListSelectionListener {
		@Override
		public void valueChanged(ListSelectionEvent e) {
			if (!e.getValueIsAdjusting() && table.isShowing()) {
				SwingFrontend.runWithContext(() -> listener.selectionChanged(getSelectedObjects()));
			}
		}
	}
	
	public class ItemTableModel extends AbstractTableModel {

		private static final long serialVersionUID = 1L;
		private List<T> objects = Collections.emptyList();
		
		public ItemTableModel() {
		}

		public void setObjects(List<T> objects) {
			this.objects = objects;
			fireTableDataChanged();
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
			PropertyInterface property = properties.get(column)
;			return Resources.getPropertyName(property);
		}

		@Override
		public Object getValueAt(int row, int column) {
			try {
				Object object = getObject(row);
				PropertyInterface property = properties.get(column);
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
	
	private class RenderingTableCellRenderer extends DefaultTableCellRenderer {

		private static final long serialVersionUID = 1L;
		
		@Override
		public Component getTableCellRendererComponent(JTable table,
				Object value, boolean isSelected, boolean hasFocus, int row,
				int column) {
			
			PropertyInterface property = properties.get(column);
			value = Rendering.render(value, RenderType.PLAIN_TEXT, property);

			return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		}
	}

	@Override
	public Dimension getMinimumSize() {
		Dimension header = table.getTableHeader().getMinimumSize();
		return new Dimension(0, header.height + 10 * table.getRowHeight());
	};

	@Override
	public Dimension getPreferredSize() {
		Dimension minimum = getMinimumSize();
		return new Dimension(30000, minimum.height);
	}
	
	@Override
	public Dimension getMaximumSize() {
		return new Dimension(30000, 30000);
	};

}
