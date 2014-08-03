package org.minimalj.frontend.swing.toolkit;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;

import org.minimalj.frontend.toolkit.ITable;
import org.minimalj.model.Keys;
import org.minimalj.model.PropertyInterface;
import org.minimalj.util.DateUtils;
import org.minimalj.util.resources.Resources;
import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.LocalTime;
import org.threeten.bp.temporal.TemporalAccessor;

public class SwingTable<T> extends JScrollPane implements ITable<T> {

	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(SwingTable.class.getName());
	
	private final Object[] keys;
	private final List<PropertyInterface> properties;
	private final JTable table;
	private final ItemTableModel tableModel;
	private TableActionListener<T> listener;
	
	public SwingTable(Object[] keys) {
		this.keys = keys;
		this.properties = convert(keys);
		
		tableModel = new ItemTableModel();
		table = new JTable(tableModel);

		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.setRowSelectionAllowed(true);
		table.setFillsViewportHeight(true);
		
//		setDefaultRenderer(BooleanFormat.class, new BooleanTableCellRenderer());
		table.setDefaultRenderer(LocalDate.class, new DateTableCellRenderer());
		table.setDefaultRenderer(LocalTime.class, new TimeTableCellRenderer());
		table.setDefaultRenderer(LocalDateTime.class, new DateTableCellRenderer()); // TODO
		
		table.setAutoCreateRowSorter(true);
		
		setViewportView(table);
		
		bindRowHeightToFont();

		table.addMouseListener(new SwingTableMouseListener());
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
	
	public T getSelectedObject() {
		int leadSelectionIndex = table.getSelectionModel().getLeadSelectionIndex();
		if (leadSelectionIndex >= 0) {
			int leadSelectionIndexInModel = table.convertRowIndexToModel(leadSelectionIndex);
			return tableModel.getObject(leadSelectionIndexInModel);
		} else {
			return null;
		}
	}

	public List<T> getSelectedObjects() {
		List<T> selectedIds = new ArrayList<>(table.getSelectedRowCount());
		for (int row : table.getSelectedRows()) {
			int rowInModel = table.convertRowIndexToModel(row);
			selectedIds.add(tableModel.getObject(rowInModel));
		}
		return selectedIds;
	}
	
	@Override
	public void setClickListener(TableActionListener<T> listener) {
		this.listener = listener;
	}

	private class SwingTableMouseListener extends MouseAdapter {
		
		@Override
		public void mouseClicked(MouseEvent e) {
			if (e.getClickCount() >= 2 && listener != null) {
				try {
					SwingClientToolkit.updateEventTab((Component) e.getSource());
					listener.action(getSelectedObject(), getSelectedObjects());
				} catch (Exception x) {
					x.printStackTrace();
				}
			}
		}
	}

	@Override
	public void setInsertListener(final InsertListener listener) {
		if (listener != null) {
			Action action = new AbstractAction() {
				private static final long serialVersionUID = 1L;

				@Override
				public void actionPerformed(ActionEvent e) {
					SwingClientToolkit.updateEventTab((Component) e.getSource());
					listener.action();
				}
			};
			bindKey(KeyEvent.VK_INSERT, action);
		} else {
			unbindKey(KeyEvent.VK_INSERT);
		}
	}

	@Override
	public void setDeleteListener(final TableActionListener<T> listener) {
		if (listener != null) {
			Action action = new AbstractAction() {
				private static final long serialVersionUID = 1L;

				@Override
				public void actionPerformed(ActionEvent e) {
					SwingClientToolkit.updateEventTab((Component) e.getSource());
					listener.action(getSelectedObject(), getSelectedObjects());
				}
			};
			bindKey(KeyEvent.VK_DELETE, action);
		} else {
			unbindKey(KeyEvent.VK_DELETE);
		}
	}

	@Override
	public void setFunctionListener(final int function, final TableActionListener<T> listener) {
		if (listener != null) {
			Action action = new AbstractAction() {
				private static final long serialVersionUID = 1L;

				@Override
				public void actionPerformed(ActionEvent e) {
					SwingClientToolkit.updateEventTab((Component) e.getSource());
					listener.action(getSelectedObject(), getSelectedObjects());
				}
			};
			bindKey(KeyEvent.VK_F1+function, action);
		} else {
			unbindKey(KeyEvent.VK_F1+function);
		}
	}
	
	private void bindKey(int keyEvent, Action action) {
		getActionMap().put(keyEvent, action);
		getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(keyEvent, 0), keyEvent);
	}

	private void unbindKey(int keyEvent) {
		getActionMap().remove(keyEvent);
		getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).remove(KeyStroke.getKeyStroke(keyEvent, 0));
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
;			return Resources.getObjectFieldName(Resources.getResourceBundle(), property);
		}

		@Override
		public Object getValueAt(int row, int column) {
			try {
				Object object = getObject(row);
				return properties.get(column).getValue(object);
			} catch (Exception x) {
				x.printStackTrace();
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
			return properties.get(columnIndex).getFieldClazz();
		}
	}
	
	private class BooleanTableCellRenderer extends DefaultTableCellRenderer {

		private static final long serialVersionUID = 1L;

		@Override
		public Component getTableCellRendererComponent(JTable table,
				Object value, boolean isSelected, boolean hasFocus, int row,
				int column) {
			
			if ("1".equals(value)) {
				value = "ja";
			} else {
				value = "nein";
			}
			
			return super.getTableCellRendererComponent(table, value, isSelected, hasFocus,
					row, column);
		}
	}
	
	private class DateTableCellRenderer extends DefaultTableCellRenderer {

		private static final long serialVersionUID = 1L;
		
		@Override
		public Component getTableCellRendererComponent(JTable table,
				Object value, boolean isSelected, boolean hasFocus, int row,
				int column) {
			
			if (value != null) {
				value = DateUtils.DATE_FORMATTER.format((TemporalAccessor) value); 
			}

			return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		}
	}

	private class TimeTableCellRenderer extends DefaultTableCellRenderer {

		private static final long serialVersionUID = 1L;
		
		@Override
		public Component getTableCellRendererComponent(JTable table,
				Object value, boolean isSelected, boolean hasFocus, int row,
				int column) {
			
			if (value != null) {
				PropertyInterface property = properties.get(column);
				value = DateUtils.getTimeFormatter(property).format((TemporalAccessor) value); 
			}

			return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		}
	}


}
