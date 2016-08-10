package org.minimalj.frontend.impl.json;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.minimalj.frontend.Frontend.ITable;
import org.minimalj.frontend.Frontend.TableActionListener;
import org.minimalj.model.Keys;
import org.minimalj.model.properties.PropertyInterface;
import org.minimalj.util.DateUtils;
import org.minimalj.util.resources.Resources;

public class JsonTable<T> extends JsonComponent implements ITable<T> {
	private static final Logger logger = Logger.getLogger(JsonTable.class.getName());

	private final List<PropertyInterface> properties;
	private final TableActionListener<T> listener;
	private List<T> objects;
	
	public JsonTable(Object[] keys, TableActionListener<T> listener) {
		super("Table");
		this.properties = convert(keys);
		this.listener = listener;

		List<String> headers = new ArrayList<>();
		for (PropertyInterface property : properties) {
			String header = Resources.getPropertyName(property);
			headers.add(header);
		}
		put("headers", headers);
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

	protected String convert(PropertyInterface property, Object value) {
		if (value instanceof LocalTime) {
			return DateUtils.getTimeFormatter(property).format((LocalTime) value); 
		} else if (value instanceof LocalDate) {
			return DateUtils.format((LocalDate) value); 
		} else if (value instanceof LocalDateTime) {
			String date = DateUtils.getDateTimeFormatter().format((TemporalAccessor) value);
			String time = DateUtils.getTimeFormatter(property).format((TemporalAccessor) value);
			return date + " " + time; 
		}
		return value != null ? value.toString() : null;
	}
	
	@Override
	public void setObjects(List<T> objects) {
		this.objects = objects;
		
		List<List<String>> tableContent = new ArrayList<>();
		for (T object : objects) {
			List<String> rowContent = new ArrayList<>();
			for (PropertyInterface property : properties) {
				Object value = property.getValue(object);
				String stringValue = convert(property, value);
				rowContent.add(stringValue);
			}
			tableContent.add(rowContent);
		}
		
		put("tableContent", tableContent);
	}
	
	public void action(int row) {
		T object = objects.get(row);
		listener.action(object);
	}
	
	public void selection(int selectedRow, List<Number> selectedRows) {
		T selectedObject = objects.get(selectedRow);
		List<T> selectedObjects = new ArrayList<>(selectedRows.size());
		for (Number r : selectedRows) {
			selectedObjects.add(objects.get(r.intValue()));
		}
		listener.selectionChanged(selectedObject, selectedObjects);
	}

}
