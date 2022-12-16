package org.minimalj.frontend.impl.json;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
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
import org.minimalj.frontend.impl.util.ColumnFilter;
import org.minimalj.frontend.util.ListUtil;
import org.minimalj.model.Column;
import org.minimalj.model.Column.ColumnAlignment;
import org.minimalj.model.Keys;
import org.minimalj.model.Rendering;
import org.minimalj.model.Rendering.ColorName;
import org.minimalj.model.properties.Property;
import org.minimalj.model.validation.ValidationMessage;
import org.minimalj.util.EqualsHelper;
import org.minimalj.util.IdUtils;

public class JsonTable<T> extends JsonComponent implements ITable<T> {
	private static final Logger logger = Logger.getLogger(JsonTable.class.getName());

	private static final int PAGE_SIZE = Integer.parseInt(Configuration.get("MjJsonTablePageSize", "1000"));
	private static final int FILTER_LINES = Integer.parseInt(Configuration.get("MjTableFilterLines", "12"));

	private final JsonPageManager pageManager;
	private final TableActionListener<T> listener;
	private List<T> objects, visibleObjects;
	private final List<T> selectedObjects = new ArrayList<>();
	private int page;
	
	private JsonTableModel tableModel;
	
	private final List<Object> sortColumns = new ArrayList<>();
	private final List<Boolean> sortDirections = new ArrayList<>();
	
	public JsonTable(JsonPageManager pageManager, Object[] keys, boolean multiSelect, TableActionListener<T> listener) {
		super("Table");
		this.pageManager = pageManager;
		this.listener = listener;

		put("tableModel", tableModel = new JsonTableModel(keys));
		
		put("multiSelect", multiSelect);
		put("tableContent", Collections.emptyList());
	}

	public class JsonTableModel extends HashMap<String, Object> {
		private final Object[] keys;
		private final List<Property> properties;
		private final ColumnFilter[] filters;
		private final IComponent[] headerFilters;
		private final String[] alignments;
		
		public JsonTableModel(Object[] keys) {
			this.keys = keys;
			this.properties = convert(keys);
			List<String> headers = new ArrayList<>();
			List<String> headerPathes = new ArrayList<>();
			filters = new ColumnFilter[keys.length];
			headerFilters = new IComponent[keys.length];
			alignments = new String[keys.length];
			Integer[] widths = new Integer[keys.length];
			Integer[] maxWidths = new Integer[keys.length];

			for (Property property : properties) {
				String header = Column.evalHeader(property);
				headers.add(header);
				headerPathes.add(property.getPath());
				int column = headers.size() - 1;
				filters[column] = ColumnFilter.createFilter(property);
				headerFilters[column] = filters[column].getComponent(new ColumnFilterChangeListener(column));
				alignments[column] = alignment(property);
				widths[column] = ListUtil.width(property);
				maxWidths[column] = ListUtil.maxWidth(property);
			}

			put("headers", headers);
			put("headerPathes", headerPathes);
			put("headerFilters", headerFilters);
			put("alignments", alignments);
			put("widths", widths);
			put("maxWidths", maxWidths);
		}
	}
	
	private static String alignment(Property property) {
		if (property instanceof Column) {
			ColumnAlignment alignment = ((Column<?, ?>) property).getAlignment();
			if (alignment != null) {
				return alignment.name();
			}
		} 
		if (Number.class.isAssignableFrom(property.getClazz())) {
			return ColumnAlignment.end.name();
		}
		return null;
	}
	
	private static List<Property> convert(Object[] keys) {
		List<Property> properties = new ArrayList<>(keys.length);
		for (Object key : keys) {
			Property property = Keys.getProperty(key);
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
		this.objects = objects != null ? objects : Collections.emptyList();

		visibleObjects = ListUtil.get(this.objects, Boolean.TRUE.equals(get("filterVisible")) ? tableModel.filters : ColumnFilter.NO_FILTER, sortColumns.toArray(), convert(sortDirections), page * PAGE_SIZE, PAGE_SIZE);
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
		putSilent("selectedRows", null); // always fire this property change
		put("selectedRows", selectedRows);
		updatePaging();
		if (!containsKey("filterVisible")) {
			put("filterVisible", this.objects.size() >= FILTER_LINES);
		}
		
		selectedObjects.clear();
		selectedObjects.addAll(newSelectedObjects);
		listener.selectionChanged(selectedObjects);
	}
	
	@Override
	public void setColumns(Object[] keys) {
		tableModel = new JsonTableModel(keys);
		put("tableModel", tableModel);
		setObjects(objects);
	}
	
	private void updatePaging() {
		if (objects.size() > PAGE_SIZE) {
			int count = ListUtil.count(objects, tableModel.filters);
			put("paging", (page * PAGE_SIZE + 1) + " - " + Math.min((page + 1) * PAGE_SIZE, count) + " / " + count);
		}
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
			if (page < Math.max(ListUtil.count(objects, tableModel.filters) - 1, 0) / PAGE_SIZE) {
				page = page + 1;
			} else {
				return;
			}
			break;
		case "first":
			if (page > 0) {
				page = 0;
			} else {
				return;
			}
			break;
		case "last":
			int max = Math.max(ListUtil.count(objects, tableModel.filters) - 1, 0) / PAGE_SIZE;
			if (page < max) {
				page = max;
			} else {
				return;
			}
			break;
		default:
			throw new IllegalArgumentException(direction);
		}
		
		visibleObjects = ListUtil.get(objects, tableModel.filters, sortColumns.toArray(), convert(sortDirections), page * PAGE_SIZE, PAGE_SIZE);
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
			for (Property property : tableModel.properties) {
				Object value = property.getValue(object);
				if (property instanceof Column) {
					Column column = (Column) property;
					String stringValue = Rendering.toString(column.render(object, value));
					if (column.isLink(object, value)) {
						rowContent.add(Collections.singletonMap("action", stringValue));
					} else {
						ColorName color = column.getColor(object, value);
						if (color == null) {
							rowContent.add(stringValue);
						} else {
							Map<String, Object> map = new HashMap<>();
							map.put("value", stringValue);
							map.put("color", color.name().toLowerCase());
							rowContent.add(map);
							// newer Java: rowContent.add(Map.of("value", stringValue, "color", color.name().toLowerCase()));
						}
					}
				} else {
					String stringValue = Rendering.toString(value, property);
					ColorName color = Rendering.getColor(object, value);
					if (color == null) {
						rowContent.add(stringValue);
					} else {
						Map<String, Object> map = new HashMap<>();
						map.put("value", stringValue);
						map.put("color", color.name().toLowerCase());
						rowContent.add(map);
						// newer Java: rowContent.add(Map.of("value", stringValue, "color", color.name().toLowerCase()));
					}
				}
			}
			tableContent.add(rowContent);
		}
		return tableContent;
	}
	
	public void cellAction(int row, int columnIndex) {
		Property property = tableModel.properties.get(columnIndex);
		if (property instanceof Column) {
			Column column = (Column) property;
			Object object = visibleObjects.get(row);
			column.run(object);
		}
	}
	
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
		Object key = tableModel.keys[column];
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
			ValidationMessage validationMessage = tableModel.filters[column].validate();
			((JsonComponent) tableModel.headerFilters[column]).put(JsonFormContent.VALIDATION_MESSAGE, validationMessage != null ? validationMessage.getFormattedText() : "");
		}
	}

	public void setFilterVisible(boolean visible) {
		put("filterVisible", visible);

		if (visible) {
			for (int column = 0; column < tableModel.filters.length; column++) {
				ValidationMessage validationMessage = tableModel.filters[column].validate();
				((JsonComponent) tableModel.headerFilters[column]).put(JsonFormContent.VALIDATION_MESSAGE, validationMessage != null ? validationMessage.getFormattedText() : "");
			}
		}

		page = 0;
		setObjects(objects);
	}

	public String export() {
		StringBuilder s = new StringBuilder(1000);
		List<String> headers = (List<String>) get("headers");
		for (String header : headers) {
			s.append(header).append(";");
		}
		s.delete(s.length() - 1, s.length());
		s.append("\n");
		
		for (T object : objects) {
			for (Property property : tableModel.properties) {
				Object value = property.getValue(object);
				if (value instanceof BigDecimal) {
					s.append(((BigDecimal) value).toPlainString());
				} else if (property instanceof Column) {
					Column column = (Column) property;
					append(s, column.render(object, value));
				} else {
					append(s, Rendering.render(value, property));
				}
				s.append(";");
			}
			s.delete(s.length() - 1, s.length());
			s.append("\n");
		}
		
		return s.toString();
	}
	
	private void append(StringBuilder s, CharSequence sequence) {
		s.append("\"");
		if (sequence != null) {
			for (int i = 0; i<sequence.length(); i++) {
				char c = sequence.charAt(i);
				if (c == '"') {
					s.append('"').append('"');
				} else if (Character.isWhitespace(c)) {
					s.append(' ');
				} else {
					s.append(c);
				}
			}
		}
		s.append("\"");
	}
}
