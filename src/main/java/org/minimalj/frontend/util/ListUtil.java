package org.minimalj.frontend.util;

import java.time.temporal.Temporal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.minimalj.frontend.impl.util.ColumnFilter;
import org.minimalj.model.Column;
import org.minimalj.model.Keys;
import org.minimalj.model.Rendering;
import org.minimalj.model.annotation.Width;
import org.minimalj.model.properties.Property;

public class ListUtil {

	public static <T> List<T> get(List<T> list, ColumnFilter[] filters, Object[] sortKeys, boolean[] sortDirections, int offset, int pageSize) {
		if (list instanceof LazyLoadingList) {
			return ((LazyLoadingList<T>) list).get(filters, sortKeys, sortDirections, offset, pageSize);
		} else if (list != null) {
			List<T> filteredList;
			boolean hasActiveFilters = Arrays.stream(filters).anyMatch(ColumnFilter::active);
			if (hasActiveFilters) {
				filteredList = new ArrayList<T>(list);
				for (ColumnFilter filter : filters) {
					if (filter != null) {
						filteredList.removeIf(item -> !filter.test(item));
					}
				}
			} else {
				filteredList = list;
			}

			filteredList.sort(new KeyComparator<>(sortKeys, sortDirections));

			int fromIndex = Math.min(offset, filteredList.size());
			int toIndex = Math.min(offset + pageSize, filteredList.size());
			return filteredList.subList(fromIndex, toIndex);
		} else {
			return Collections.emptyList();
		}
	}

	public static <T> int count(List<T> list, ColumnFilter[] filters) {
		if (list instanceof LazyLoadingList) {
			return ((LazyLoadingList<T>) list).count(filters);
		} else if (list != null) {
			boolean hasFilters = Arrays.stream(filters).anyMatch(f -> f != null);
			if (hasFilters) {
				List<T> filteredList = new ArrayList<T>(list);
				for (ColumnFilter filter : filters) {
					if (filter != null) {
						filteredList.removeIf(item -> !filter.test(item));
					}
				}
				return filteredList.size();
			} else {
				return list.size();
			}
		} else {
			return 0;
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static class KeyComparator<T> implements Comparator<T> {
		private final Property[] sortProperties;
		private final boolean[] sortDirections;

		public KeyComparator(Object[] sortKeys, boolean[] sortDirections) {
			this.sortProperties = Keys.getProperties(sortKeys);
			this.sortDirections = sortDirections;
		}

		@Override
		public int compare(T a, T b) {
			int index = 0;
			for (Property property : sortProperties) {
				int factor = sortDirections[index++] ? 1 : -1;
				Object value1 = property.getValue(a);
				Object value2 = property.getValue(b);
				if (value1 == null) {
					return value2 == null ? 0 : -factor;
				} else if (value2 == null) {
					return factor;
				} else {
					value1 = convert(value1);
					value2 = convert(value2);

					try {
						return ((Comparable) value1).compareTo(value2) * factor;
					} catch (Exception x) {
						// fallback to string comparation
						String s1 = Rendering.render(value1).toString();
						String s2 = Rendering.render(value2).toString();
						return s1.compareTo(s2);
					}
				}
			}
			return 0;
		}

		private Comparable convert(Object value) {
			if (value instanceof Comparable) {
				return (Comparable) value;
			} else {
				return Rendering.render(value).toString();
			}
		}
	}

	public static Width getWidthAnnotation(Property property) {
		Width width = property.getAnnotation(Width.class);
		if (width != null) {
			return width;
		}
		width = property.getClazz().getAnnotation(Width.class);
		if (width != null) {
			return width;
		}
		return null;
	}

	public static int width(Property property) {
		if (property instanceof Column) {
			Integer width = ((Column<?, ?>) property).getWidth();
			if (width != null) {
				return width;
			}
		}
		Width width = getWidthAnnotation(property);
		return width != null ? width.value() : Width.DEFAULT;
	}

	public static int maxWidth(Property property) {
		if (property instanceof Column) {
			Integer width = ((Column<?, ?>) property).getMaxWidth();
			if (width != null) {
				return width;
			}
		}
		Width width = getWidthAnnotation(property);
		if (width != null && width.maxWidth() > 0) {
			return width.maxWidth();
		} else {
			int w = width(property);
			Class<?> clazz = property.getClazz();
			if (Number.class.isAssignableFrom(clazz) || Temporal.class.isAssignableFrom(clazz) || clazz == Boolean.class) {
				return w;
			} else {
				return w * 3 / 2;
			}
		}
	}

}
