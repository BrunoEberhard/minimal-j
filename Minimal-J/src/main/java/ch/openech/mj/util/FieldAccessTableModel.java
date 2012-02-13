package ch.openech.mj.util;

import java.util.Collections;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import ch.openech.mj.db.model.AccessorInterface;
import ch.openech.mj.db.model.Constants;
import ch.openech.mj.db.model.Format;
import ch.openech.mj.db.model.Formats;
import ch.openech.mj.edit.value.PropertyAccessor;

public class FieldAccessTableModel<T> extends AbstractTableModel {
	private final Object[] fields;
	private final String[] names;

	public FieldAccessTableModel(Object[] fields, String[] names) {
		this.fields = fields;
		this.names = names;
	}

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
		return names[column];
	}

	@Override
	public Object getValueAt(int row, int column) {
		T object = list.get(row);
		Object value = PropertyAccessor.get(object, Constants.getConstant(fields[column]));
		return value;
	}

	@Override
	public int getRowCount() {
		return list.size();
	}

	@Override
	public int getColumnCount() {
		return fields.length;
	}

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		if (!list.isEmpty()) {
			Class<?> clazz = list.get(0).getClass();
			AccessorInterface accessor = PropertyAccessor.getAccessor(clazz, Constants.getConstant(fields[columnIndex]));
			Format format = Formats.getInstance().getFormat(accessor);
			if (format != null && format.getClazz() != null) {
				return format.getClazz();
			}
		} 
		return super.getColumnClass(columnIndex);
	}

	public T getRow(int row) {
		return list.get(row);
	}
}
