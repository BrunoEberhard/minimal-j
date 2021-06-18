package org.minimalj.frontend.impl.json;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.minimalj.frontend.Frontend.ITable;
import org.minimalj.frontend.Frontend.TableActionListener;
import org.minimalj.model.Keys;
import org.minimalj.model.Rendering;
import org.minimalj.model.Rendering.Coloring.ColorName;
import org.minimalj.model.properties.PropertyInterface;
import org.minimalj.util.EqualsHelper;
import org.minimalj.util.IdUtils;
import org.minimalj.util.Sortable;
import org.minimalj.util.resources.Resources;

public class JsonTable<T> extends JsonComponent implements ITable<T> {
	private static final Logger logger = Logger.getLogger(JsonTable.class.getName());

	private static final int PAGE_SIZE = 50;

	private final Object[] keys;
	private final List<PropertyInterface> properties;
	private final TableActionListener<T> listener;
	private List<T> objects;
	private final List<T> selectedObjects = new ArrayList<>();
	private int visibleRows = PAGE_SIZE;
	private final List<Object> sortColumns = new ArrayList<>();
	private final List<Boolean> sortDirections = new ArrayList<>();

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
		List<PropertyInterface> properties = new ArrayList<>(keys.length);
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
		checkSortDirections();
		if (!sortColumns.isEmpty()) {
			((Sortable) objects).sort(sortColumns.toArray(), convert(sortDirections));
		}

		visibleRows = Math.min(objects.size(), Math.max(visibleRows, PAGE_SIZE));
		List<T> visibleObjects = objects.subList(0, visibleRows);
		List<List> tableContent = createTableContent(visibleObjects);

		List<String> selectedRows = new ArrayList<>();
		List<T> newSelectedObjects = new ArrayList<>();
		for (T object : visibleObjects) {
			for (T selectedObject : selectedObjects) {
				if (equalsByIdOrContent(selectedObject, object)) {
					selectedRows.add("selected");
					newSelectedObjects.add(object);
				} else {
					selectedRows.add("unselected");
				}
			}
		}
		
		put("tableContent", tableContent);
		putSilent("selectedRows", null); // allway fire this property change
		put("selectedRows", selectedRows);
		put("size", objects.size());
		put("extendable", isExtendable());
		
		selectedObjects.clear();
		selectedObjects.addAll(newSelectedObjects);
		listener.selectionChanged(selectedObjects);
	}
	
	public static <T> boolean equalsByIdOrContent(T a, T b) {
		if (IdUtils.hasId(a.getClass())) {
			Object idA = IdUtils.getId(a);
			Object idB = IdUtils.getId(b);
			return Objects.equals(idA, idB);
		} else {
			return EqualsHelper.equals(a, b);
		}
	}
	
	private void checkSortDirections() {
		boolean sortable = ((objects) instanceof Sortable) && //
				sortColumns.stream().allMatch(key -> ((Sortable) objects).canSortBy(key));
		if (!sortable) {
			sortColumns.clear();
			sortDirections.clear();
		}
	}

	private boolean[] convert(List<Boolean> booleans) {
		boolean[] result = new boolean[booleans.size()];
		for (int i = 0; i < booleans.size(); i++) {
			result[i] = booleans.get(i);
		}
		return result;
	}

	@SuppressWarnings("rawtypes")
	public List<List> extendContent() {
		int newVisibleRows = Math.min(objects.size(), visibleRows + PAGE_SIZE);
		List<T> newVisibleObjects = objects.subList(visibleRows, newVisibleRows);
		visibleRows = newVisibleRows;
		return createTableContent(newVisibleObjects);
	}

	public boolean isExtendable() {
		return visibleRows < objects.size();
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private List<List> createTableContent(List<T> objects) {
		List<List> tableContent = new ArrayList<>();
		for (T object : objects) {
			List rowContent = new ArrayList();
			for (PropertyInterface property : properties) {
				Object value = property.getValue(object);
				String stringValue = Rendering.toString(value, property);
				ColorName color = Rendering.getColor(object, value);
				if (color == null) {
					rowContent.add(stringValue);
				} else {
					rowContent.add(Map.of("value", stringValue, "color", color.name().toLowerCase()));
				}
			}
			tableContent.add(rowContent);
		}
		return tableContent;
	}
	
//	private String toString(Color color) {
//		return "#" + StringUtils.padLeft(Integer.toHexString(color.getRed()), 2, '0') + 
//				StringUtils.padLeft(Integer.toHexString(color.getGreen()), 2, '0') + 
//				StringUtils.padLeft(Integer.toHexString(color.getBlue()), 2, '0');
//	}
	
	public void action(int row) {
		T object = objects.get(row);
		listener.action(object);
	}
	
	public void selection(List<Number> selectedRows) {
		selectedObjects.clear();
		for (Number r : selectedRows) {
			selectedObjects.add(objects.get(r.intValue()));
		}
		listener.selectionChanged(selectedObjects);
	}

	public void sort(int column) {
		if (objects instanceof Sortable) {
			Sortable sortable = (Sortable) objects;
			Object key = keys[column];
			int size = sortColumns.size();
			if (sortable.canSortBy(key)) {
				int pos = sortColumns.indexOf(key);
				if (size > 0 && pos == 0) {
					sortDirections.set(0, !sortDirections.get(0));
				} else {
					if (pos >= 0) {
						sortDirections.remove(pos);
						sortColumns.remove(pos);
					}
					sortColumns.add(0, key);
					sortDirections.add(0, true);
				}

				setObjects(objects);
			}
		}
	}
}
