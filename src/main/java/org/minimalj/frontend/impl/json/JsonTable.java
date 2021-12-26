package org.minimalj.frontend.impl.json;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.minimalj.application.Configuration;
import org.minimalj.frontend.Frontend.IComponent;
import org.minimalj.frontend.Frontend.ITable;
import org.minimalj.frontend.Frontend.InputComponentListener;
import org.minimalj.frontend.Frontend.TableActionListener;
import org.minimalj.frontend.action.Action;
import org.minimalj.frontend.impl.util.ColumnFilter;
import org.minimalj.frontend.util.ListUtil;
import org.minimalj.model.Keys;
import org.minimalj.model.Rendering;
import org.minimalj.model.Rendering.ColorName;
import org.minimalj.model.properties.PropertyInterface;
import org.minimalj.model.validation.ValidationMessage;
import org.minimalj.util.EqualsHelper;
import org.minimalj.util.IdUtils;
import org.minimalj.util.resources.Resources;

public class JsonTable<T> extends JsonComponent implements ITable<T> {
	private static final Logger logger = Logger.getLogger(JsonTable.class.getName());

	private static final int PAGE_SIZE = Integer.parseInt(Configuration.get("MjJsonTablePageSize", "1000"));

	private final JsonPageManager pageManager;
	private final Object[] keys;
	private final List<PropertyInterface> properties;
	private final TableActionListener<T> listener;
	private List<T> objects, visibleObjects;
	private final List<T> selectedObjects = new ArrayList<>();
	private int page;
	private final List<Object> sortColumns = new ArrayList<>();
	private final List<Boolean> sortDirections = new ArrayList<>();
	private final ColumnFilter[] filters;
	private final IComponent[] headerFilters;
	
	public JsonTable(JsonPageManager pageManager, Object[] keys, boolean multiSelect, TableActionListener<T> listener) {
		super("Table");
		this.pageManager = pageManager;
		this.keys = keys;
		this.properties = convert(keys);
		this.listener = listener;

		List<String> headers = new ArrayList<>();
		List<String> headerPathes = new ArrayList<>();
		filters = new ColumnFilter[keys.length];
		headerFilters = new IComponent[keys.length];

		for (PropertyInterface property : properties) {
			String header = Resources.getPropertyName(property);
			headers.add(header);
			headerPathes.add(property.getPath());
			int column = headers.size() - 1;
			filters[column] = ColumnFilter.createFilter(property, new ColumnFilterChangeListener(column));
			headerFilters[column] = filters[column].getComponent();
		}
		put("headers", headers);
		put("headerPathes", headerPathes);
		put("headerFilters", headerFilters);

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

		visibleObjects = ListUtil.get(objects, filters, sortColumns.toArray(), convert(sortDirections), page, PAGE_SIZE);
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
		updatePaging();
		
		selectedObjects.clear();
		selectedObjects.addAll(newSelectedObjects);
		listener.selectionChanged(selectedObjects);
	}
	
	private void updatePaging() {
		put("paging", objects.size() > PAGE_SIZE);
		put("currentPage", (page + 1) + " / " + (Math.max(ListUtil.count(objects, filters) - 1, 0) / PAGE_SIZE + 1));
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
		T object = visibleObjects.get(row);
		listener.action(object);
	}
	
	public void selection(List<Number> selectedRows) {
		selectedObjects.clear();
		for (Number r : selectedRows) {
			selectedObjects.add(visibleObjects.get(r.intValue()));
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
	
	public class ColumnFilterChangeListener implements InputComponentListener {

		private final int column;

		public ColumnFilterChangeListener(int column) {
			this.column = column;
		}

		@Override
		public void changed(IComponent source) {
			page = 0;
			setObjects(objects);
			ValidationMessage validationMessage = filters[column].validate();
			((JsonComponent) headerFilters[column]).put(JsonFormContent.VALIDATION_MESSAGE, validationMessage != null ? validationMessage.getFormattedText() : "");
		}
	}

	public void filter(boolean enabled) {
		int column = 0;
		for (ColumnFilter filter : filters) {
			filter.setEnabled(enabled);

			ValidationMessage validationMessage = filters[column].validate();
			((JsonComponent) headerFilters[column]).put(JsonFormContent.VALIDATION_MESSAGE, validationMessage != null ? validationMessage.getFormattedText() : "");
			column++;
		}

		page = 0;
		setObjects(objects);
	}
}
