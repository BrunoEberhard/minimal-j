package ch.openech.mj.swing.component;

import java.awt.Component;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;

import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;
import org.joda.time.ReadablePartial;

import ch.openech.mj.model.Keys;
import ch.openech.mj.model.PropertyInterface;
import ch.openech.mj.resources.Resources;
import ch.openech.mj.util.JodaFormatter;

public class PropertyTable<T> extends JTable {
	private static final Logger logger = Logger.getLogger(PropertyTable.class.getName());

	private final List<PropertyInterface> properties;
	private final PropertyTableModel tableModel;

	public PropertyTable(Class<T> clazz, Object[] keys) {
		this.properties = convert(keys);
		
		tableModel = new PropertyTableModel();
		setModel(tableModel);
		
//		setDefaultRenderer(BooleanFormat.class, new BooleanTableCellRenderer());
		setDefaultRenderer(LocalDate.class, new DateTableCellRenderer());
		setDefaultRenderer(LocalTime.class, new DateTableCellRenderer());
		setDefaultRenderer(LocalDateTime.class, new DateTableCellRenderer());
		setDefaultRenderer(ReadablePartial.class, new DateTableCellRenderer());
		
		setAutoCreateRowSorter(true);
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
			logger.log(Level.SEVERE, "PropertyTable without valid keys");
		}
		return properties;
	}

	public void setObjects(List<T> list) {
		tableModel.setObjects(list);
	}

	public class PropertyTableModel extends AbstractTableModel {

		private List<T> list = Collections.emptyList();

		public void setObjects(List<T> bookList) {
			this.list = bookList;
			fireTableDataChanged();
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
			T object = list.get(row);
			PropertyInterface property = properties.get(column);
			return property.getValue(object);
		}

		@Override
		public int getRowCount() {
			return list.size();
		}

		@Override
		public int getColumnCount() {
			return properties.size();
		}

		@Override
		public Class<?> getColumnClass(int columnIndex) {
			return properties.get(columnIndex).getFieldClazz();
		}

		public T getRow(int row) {
			return list.get(row);
		}
	}
	
	private class BooleanTableCellRenderer extends DefaultTableCellRenderer {

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

		private final JodaFormatter formatter = new JodaFormatter();
		
		@Override
		public Component getTableCellRendererComponent(JTable table,
				Object value, boolean isSelected, boolean hasFocus, int row,
				int column) {
			
			PropertyInterface property = properties.get(column);
			value = formatter.format(value, property);
			return super.getTableCellRendererComponent(table, value, isSelected, hasFocus,
					row, column);
		}
	}
	
	public T getObject(int row) {
		row = convertRowIndexToModel(row);
		return tableModel.getRow(row);
	}
}
