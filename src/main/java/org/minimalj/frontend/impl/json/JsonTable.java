package org.minimalj.frontend.impl.json;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.minimalj.application.Configuration;
import org.minimalj.frontend.Frontend.ITable;
import org.minimalj.frontend.Frontend.TableActionListener;
import org.minimalj.frontend.action.Action;
import org.minimalj.frontend.impl.util.ColumnFilter;
import org.minimalj.frontend.util.ListUtil;
import org.minimalj.model.Keys;
import org.minimalj.model.Rendering;
import org.minimalj.model.Rendering.Coloring.ColorName;
import org.minimalj.model.properties.PropertyInterface;
import org.minimalj.util.ChangeListener;
import org.minimalj.util.EqualsHelper;
import org.minimalj.util.IdUtils;
import org.minimalj.util.resources.Resources;

public class JsonTable<T> extends JsonComponent implements ITable<T> {
	private static final Logger logger = Logger.getLogger(JsonTable.class.getName());

	private static final int PAGE_SIZE = Integer.parseInt(Configuration.get("MjJsonTablePageSize", "10"));

	private final JsonPageManager pageManager;
	private final Object[] keys;
	private final List<PropertyInterface> properties;
	private final TableActionListener<T> listener;
	private List<T> objects;
	private final List<T> selectedObjects = new ArrayList<>();
	private int page;
	private final List<Object> sortColumns = new ArrayList<>();
	private final List<Boolean> sortDirections = new ArrayList<>();
	private final ColumnFilter[] filters;
	private final boolean[] headerFilterLookups;
	
	public JsonTable(JsonPageManager pageManager, Object[] keys, boolean multiSelect, TableActionListener<T> listener) {
		super("Table");
		this.pageManager = pageManager;
		this.keys = keys;
		this.properties = convert(keys);
		this.listener = listener;

		List<String> headers = new ArrayList<>();
		List<String> headerPathes = new ArrayList<>();
		filters = new ColumnFilter[keys.length];
		headerFilterLookups = new boolean[keys.length];

		for (PropertyInterface property : properties) {
			String header = Resources.getPropertyName(property);
			headers.add(header);
			headerPathes.add(property.getPath());
			int column = headers.size() - 1;
			filters[column] = ColumnFilter.createFilter(property, new ColumnFilterChangeListener(column));
			headerFilterLookups[column] = filters[column].hasLookup();
		}
		put("headers", headers);
		put("headerPathes", headerPathes);
		put("headerFilterLookups", headerFilterLookups);

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
		pageManager.unregister(get("tableContent"));
		this.objects = objects;

		List<T> visibleObjects = ListUtil.get(objects, filters, sortColumns.toArray(), convert(sortDirections), page, PAGE_SIZE);
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
		updatePaging();
		
		selectedObjects.clear();
		selectedObjects.addAll(newSelectedObjects);
		listener.selectionChanged(selectedObjects);
	}
	
	private void updatePaging() {
		put("paging", objects.size() > PAGE_SIZE);
		put("currentPage", (page + 1) + " / " + (Math.max(ListUtil.count(objects, filters) - 1, 0) / PAGE_SIZE + 1));
//		put("prev", page > 0);
//		put("next", objects.size() > (page + 1) * PAGE_SIZE);
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
	
	private boolean[] convert(List<Boolean> booleans) {
		boolean[] result = new boolean[booleans.size()];
		for (int i = 0; i < booleans.size(); i++) {
			result[i] = booleans.get(i);
		}
		return result;
	}

	public void page(String direction) {
		switch (direction) {
		case "prev":
			if (page > 0) {
				page = page - 1;
			} else {
				return;
			}
			break;
		case "next":
			if (page < Math.max(ListUtil.count(objects, filters) - 1, 0) / PAGE_SIZE) {
				page = page + 1;
			} else {
				return;
			}
			break;
		default:
			throw new IllegalArgumentException(direction);
		}
		
		List<T> visibleObjects = ListUtil.get(objects, filters, sortColumns.toArray(), convert(sortDirections), page, PAGE_SIZE);
		List<List> tableContent = createTableContent(visibleObjects);
		put("tableContent", tableContent);

		put("selectedRows", Collections.emptyList());
		
		updatePaging();
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private List<List> createTableContent(List<T> objects) {
		List<List> tableContent = new ArrayList<>();
		for (T object : objects) {
			List rowContent = new ArrayList();
			for (PropertyInterface property : properties) {
				Object value = property.getValue(object);
				if (value instanceof Action) {
					Action action = (Action) value;
					if (action.isEnabled()) {
						rowContent.add(Map.of("action", new JsonAction(action)));
					} else {
						rowContent.add(action.getName());
					}
				} else {
					String stringValue = Rendering.toString(value, property);
					ColorName color = Rendering.getColor(object, value);
					if (color == null) {
						rowContent.add(stringValue);
					} else {
						rowContent.add(Map.of("value", stringValue, "color", color.name().toLowerCase()));
					}
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
		Object key = keys[column];
		int size = sortColumns.size();
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

	public void filterEditor(int column) {
		filters[column].runEditor(s -> setFilter(column, s));
	}

	public void setFilter(int column, String newValue) {
		filters[column].setText(newValue);
	}
	
	public class ColumnFilterChangeListener implements ChangeListener<ColumnFilter> {

		private final int column;
		
		public ColumnFilterChangeListener(int column) {
			this.column = column;
		}
		
		@Override
		public void changed(ColumnFilter columnFilter) {
			page = 0;
			setObjects(objects);
			
			put("columnFilter", Map.of("column", column, "string", columnFilter.getText()));
		}
	}
}
