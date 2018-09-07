package org.minimalj.frontend.impl.lanterna.toolkit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.minimalj.frontend.Frontend.ITable;
import org.minimalj.frontend.Frontend.TableActionListener;
import org.minimalj.model.Keys;
import org.minimalj.model.Rendering;
import org.minimalj.model.properties.PropertyInterface;
import org.minimalj.util.resources.Resources;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.table.Table;

public class LanternaTable<T> extends Table<String> implements ITable<T> {
	private static final Logger logger = Logger.getLogger(LanternaTable.class.getName());

	private final List<PropertyInterface> properties;
	private List<T> objects = Collections.emptyList();
	private final int[] columnWidthArray;
	
	public LanternaTable(Object[] keys, boolean multiSelect, TableActionListener<T> listener) {
		super(getColumnLabels(keys));
		setCellSelection(false);
		
		this.properties = convert(keys);
		
		columnWidthArray = new int[keys.length];
		updateColumnWidths();

		Runnable r = () -> listener.action(objects.get(super.getSelectedRow()));
		Runnable selectAction = () -> LanternaFrontend.run(LanternaTable.this, r);
		setSelectAction(selectAction);
	}
	
	@Override
	public int getVisibleRows() {
		return getSize().getRows();
	}		
	
	private static String[] getColumnLabels(Object[] keys) {
		List<PropertyInterface> properties = convert(keys);
		
		String[] columnLabels = new String[keys.length];
		for (int i = 0; i<properties.size(); i++) {
			PropertyInterface property = properties.get(i);
			columnLabels[i] = Resources.getPropertyName(property);
		}
		return columnLabels;
	}
	
	private void updateColumnWidths() {
		for (int i = 0; i<columnWidthArray.length; i++) {
			int width = getTableModel().getColumnLabel(i).length();
			for (int row = 0; row<getTableModel().getRowCount(); row++) {
				String value = getTableModel().getCell(i, row);
				width = Math.max(width, value.length());
			}
			columnWidthArray[i] = Math.max(width, 1);
		}
	}

	private static List<PropertyInterface> convert(Object[] keys) {
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
	
	@Override
	public void setObjects(List<T> objects) {
		if (objects != null) {
			this.objects = objects;
		} else {
			this.objects = Collections.emptyList();
		}
		for (int i = getTableModel().getRowCount()-1; i>= 0; i--) {
			getTableModel().removeRow(i);
		}
		for (Object object : objects) {
			List<String> values = new ArrayList<>();
			for (PropertyInterface property : properties) {
				Object value = property.getValue(object);
				values.add(Rendering.toString(value, property));
			}
			getTableModel().addRow(values);
		}
	}

    @Override
    public synchronized Table<String> setSize(TerminalSize size) {
    	setVisibleRows(size.getRows() - 1);
    	return super.setSize(size);
    }
	
}
