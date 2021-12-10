package org.minimalj.frontend.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.minimalj.frontend.impl.util.ColumnFilter;
import org.minimalj.model.Keys;
import org.minimalj.model.properties.PropertyInterface;

public class ListUtil {

	public static <T> List<T> get(List<T> list, ColumnFilter[] filters, Object[] sortKeys, boolean[] sortDirections, int page, int pageSize) {
		if (list instanceof LazyLoadingList) {
			return ((LazyLoadingList<T>) list).get(filters, sortKeys, sortDirections, page, pageSize);
		} else {
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

			int fromIndex = Math.min(page * pageSize, filteredList.size());
			int toIndex = Math.min((page + 1) * pageSize, filteredList.size());
			return filteredList.subList(fromIndex, toIndex);
		}
	}

	public static <T> int count(List<T> list, ColumnFilter[] filters) {
		if (list instanceof LazyLoadingList) {
			return ((LazyLoadingList<T>) list).count(filters);
		} else {
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
		}
	}

	private static class KeyComparator<T> implements Comparator<T> {
		private final PropertyInterface[] sortProperties;
		private final boolean[] sortDirections;

		public KeyComparator(Object[] sortKeys, boolean[] sortDirections) {
			this.sortProperties = Keys.getProperties(sortKeys);
			this.sortDirections = sortDirections;
		}

		@SuppressWarnings({ "unchecked", "rawtypes" })
		@Override
		public int compare(T a, T b) {
			int index = 0;
			for (PropertyInterface property : sortProperties) {
				int factor = sortDirections[index++] ? 1 : -1;
				Object value1 = property.getValue(a);
				Object value2 = property.getValue(b);
				if (value1 == null) {
					return value2 == null ? 0 : -factor;
				} else if (value2 == null) {
					return factor;
				} else {
					return ((Comparable) value1).compareTo(value2) * factor;
				}
			}
			return 0;
		}

	}
}
