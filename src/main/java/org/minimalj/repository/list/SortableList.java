package org.minimalj.repository.list;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;

import org.minimalj.model.Keys;
import org.minimalj.model.properties.PropertyInterface;
import org.minimalj.util.Sortable;

/**
 * @param <T>
 *            Class of the Elements
 */
public class SortableList<T> extends ArrayList<T> implements Sortable, Serializable {
	private static final long serialVersionUID = 1L;

	public SortableList() {
		super();
	}
	
	public SortableList(Collection<T> objects) {
		super(objects);
	}
	
	@Override
	public void sort(Object[] sortKeys, boolean[] sortDirections) {
		sort(new KeyComparator(sortKeys, sortDirections));
	}
	
	@Override
	public boolean canSortBy(Object sortKey) {
		PropertyInterface propertyInterface = Keys.getProperty(sortKey);
		return Comparable.class.isAssignableFrom(propertyInterface.getClazz());
	}

	private class KeyComparator implements Comparator<T> {
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
