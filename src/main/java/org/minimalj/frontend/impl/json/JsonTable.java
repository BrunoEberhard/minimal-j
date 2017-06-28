package org.minimalj.frontend.impl.json;

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
import org.minimalj.util.Sortable;
import org.minimalj.util.resources.Resources;

public class JsonTable<T> extends JsonComponent implements ITable<T> {
	private static final Logger logger = Logger.getLogger(JsonTable.class.getName());

	private static final int PAGE_SIZE = 50;
	
	private final Object[] keys;
	private final List<PropertyInterface> properties;
	private final TableActionListener<T> listener;
	private List<T> objects;
	private int visibleRows = PAGE_SIZE;
	private Integer sortColumn = null;
	
	public JsonTable(Object[] keys, boolean multiSelect, TableActionListener<T> listener) {
		super("Table");
		this.keys = keys;
		this.properties = convert(keys);
		this.listener = listener;

		List<String> headers = new ArrayList<>();
		for (PropertyInterface property : properties) {
			String header = Resources.getPropertyName(property);
			headers.add(header);
		}
		put("headers", headers);
		put("multiSelect", multiSelect);
		put("tableContent", Collections.emptyList());
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
		this.objects = objects;

		visibleRows = 0;
		List<List<String>> tableContent = extendContent();
		
		put("tableContent", tableContent);
		put("size", objects.size());
		put("extendable", isExtendable());
	}

	public List<List<String>> extendContent() {
		int newVisibleRows = Math.min(objects.size(), visibleRows + PAGE_SIZE);
		List<T> newVisibleObjects = objects.subList(visibleRows, newVisibleRows);
		visibleRows = newVisibleRows;
		return createTableContent(newVisibleObjects);
	}
	
	public boolean isExtendable() {
		return visibleRows < objects.size();
	}
	
	private List<List<String>> createTableContent(List<T> objects) {
		List<List<String>> tableContent = new ArrayList<>();
		for (T object : objects) {
			List<String> rowContent = new ArrayList<>();
			for (PropertyInterface property : properties) {
				Object value = property.getValue(object);
				String stringValue = Rendering.render(value, Rendering.RenderType.PLAIN_TEXT, property);
				rowContent.add(stringValue);
			}
			tableContent.add(rowContent);
		}
		return tableContent;
	}
	
	public void action(int row) {
		T object = objects.get(row);
		listener.action(object);
	}
	
	public void selection(List<Number> selectedRows) {
		List<T> selectedObjects = new ArrayList<>(selectedRows.size());
		for (Number r : selectedRows) {
			selectedObjects.add(objects.get(r.intValue()));
		}
		listener.selectionChanged(selectedObjects);
	}

	public void sort(int column) {
		column = column + 1;
		if (sortColumn == null) {
			sortColumn = column;
		} else if (sortColumn == column) {
			sortColumn = -sortColumn;
		} else {
			sortColumn = column;
		}
		if (objects instanceof Sortable) {
			Object[] keys = new Object[] { this.keys[Math.abs(sortColumn) - 1] };
			((Sortable) objects).sort(keys, new boolean[] { sortColumn > 0 });
		}
		setObjects(objects);
	}
}
