package ch.openech.mj.util;

import java.awt.Component;
import java.text.Collator;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableRowSorter;

import ch.openech.mj.db.model.AccessorInterface;
import ch.openech.mj.db.model.BooleanFormat;
import ch.openech.mj.db.model.Constants;
import ch.openech.mj.db.model.DateFormat;
import ch.openech.mj.db.model.Format;
import ch.openech.mj.db.model.Formats;
import ch.openech.mj.db.model.IntegerFormat;
import ch.openech.mj.edit.value.PropertyAccessor;
import ch.openech.mj.resources.Resources;

public class PropertyTable<T> extends JTable {

	private final Class<T> clazz;
	private final String[] fieldNames;
	private final PropertyTableModel tableModel;

	public PropertyTable(Class<T> clazz, Object[] fields) {
		this.clazz = clazz;
		this.fieldNames = Constants.getConstants(fields);
		
		tableModel = new PropertyTableModel();
		setModel(tableModel);
		
		setDefaultRenderer(BooleanFormat.class, new BooleanTableCellRenderer());

		setAutoCreateRowSorter(true);
		for (int i = 0; i<tableModel.getColumnCount(); i++) {
			AccessorInterface accessor = PropertyAccessor.getAccessor(clazz, fieldNames[i]);
			Format format = Formats.getInstance().getFormat(accessor);
			if (format != null) {
				setDefaultRenderer(format.getClass(), new FormatTableCellRenderer(format));
				if (format instanceof IntegerFormat) {
					((TableRowSorter<?>) getRowSorter()).setComparator(i, new IntegerComparator());
				}
			}
		}
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
			return Resources.getObjectFieldName(Resources.getResourceBundle(), clazz, fieldNames[column]);
		}

		@Override
		public Object getValueAt(int row, int column) {
			T object = list.get(row);
			Object value = PropertyAccessor.get(object, fieldNames[column]);
			return value;
		}

		@Override
		public int getRowCount() {
			return list.size();
		}

		@Override
		public int getColumnCount() {
			return fieldNames.length;
		}

		@Override
		public Class<?> getColumnClass(int columnIndex) {
			AccessorInterface accessor = PropertyAccessor.getAccessor(clazz, fieldNames[columnIndex]);
			Format format = Formats.getInstance().getFormat(accessor);
			if (format != null) {
				return format.getClass();
			}
			return super.getColumnClass(columnIndex);
		}

		public T getRow(int row) {
			return list.get(row);
		}
	}
	
	private class FormatTableCellRenderer extends DefaultTableCellRenderer {
		private final Format format;
		
		public FormatTableCellRenderer(Format format) {
			this.format = format;
		}
		
		@Override
		public Component getTableCellRendererComponent(JTable table,
				Object value, boolean isSelected, boolean hasFocus, int row,
				int column) {
			
			value = format.display((String) value);
			return super.getTableCellRendererComponent(table, value, isSelected, hasFocus,
					row, column);
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
	
	private static class IntegerComparator implements Comparator<String> {

		@Override
		public int compare(String o1, String o2) {
			if (o1 != null && o2 != null) {
				while (o1.length() > o2.length()) {
					o1 = "0" + o1;
				}
				while (o2.length() > o1.length()) {
					o2 = "0" + o2;
				}
			}	

			return Collator.getInstance().compare(o1, o2);
		}
	}

	public T getObject(int row) {
		row = convertRowIndexToModel(row);
		return tableModel.getRow(row);
	}
}
