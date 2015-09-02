package org.minimalj.frontend.impl.vaadin6;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.minimalj.model.properties.PropertyInterface;

import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.Property;

public class PropertyVaadinContainer<T> implements Container.Sortable {
	private static final long serialVersionUID = 1L;

	private final List<PropertyInterface> properties;
	private final List<T> objects;

	public PropertyVaadinContainer(List<T> objects, List<PropertyInterface> properties) {
		this.objects = objects;
		this.properties = properties;
	}
	
	@Override
	public Item getItem(Object itemId) {
		return new PropertyVaadinContainerItem(itemId);
	}

	@Override
	public Collection<?> getContainerPropertyIds() {
		return properties;
	}

	@Override
	public Collection<?> getItemIds() {
		return objects;
	}

	@Override
	public Property getContainerProperty(Object itemId, Object propertyId) {
		Item item = getItem(itemId);
        return item.getItemProperty(propertyId);
	}

	@Override
	public Class<?> getType(Object propertyId) {
		PropertyInterface property = (PropertyInterface) propertyId;
		return property.getClazz();
	}

	@Override
	public int size() {
		return objects.size();
	}

	@Override
	public boolean containsId(Object itemId) {
		return objects.contains(itemId);
	}

	@Override
	public Item addItem(Object itemId) throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object addItem() throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean removeItem(Object itemId) throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean addContainerProperty(Object propertyId, Class<?> type, Object defaultValue)
			throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean removeContainerProperty(Object propertyId) throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean removeAllItems() throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}

	private class PropertyVaadinContainerItem implements Item {
		private static final long serialVersionUID = 1L;
		private final Object object;
		
		public PropertyVaadinContainerItem(Object object) {
			this.object = object;
		}
		
		@Override
		public Property getItemProperty(Object id) {
			PropertyInterface property = (PropertyInterface) id;
			Object value = property.getValue(object);
			return new PropertyVaadinContainerProperty(property.getClazz(), value);
		}

		@Override
		public Collection<?> getItemPropertyIds() {
			return properties;
		}

		@Override
		public boolean addItemProperty(Object id, Property property) throws UnsupportedOperationException {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean removeItemProperty(Object id) throws UnsupportedOperationException {
			throw new UnsupportedOperationException();
		}
	}
	
	private class PropertyVaadinContainerProperty implements Property {

		private static final long serialVersionUID = 1L;
		private final Class<?> clazz;
		private final Object object;
		
		public PropertyVaadinContainerProperty(Class<?> clazz, Object object) {
			this.clazz = clazz;
			this.object = object;
		}
		
		@Override
		public Object getValue() {
			return object;
		}

		@Override
		public void setValue(Object newValue) throws ReadOnlyException, ConversionException {
			throw new ReadOnlyException();
		}

		@Override
		public Class<?> getType() {
			return clazz;
		}

		@Override
		public boolean isReadOnly() {
			return true;
		}

		@Override
		public void setReadOnly(boolean newStatus) {
			// ignored
		}

		@Override
		public String toString() {
			return object != null ? object.toString() : "";
		}
	}
	
	// Sortable

	@Override
	public Object nextItemId(Object itemId) {
		if (!isLastId(itemId)) {
			int index = objects.indexOf(itemId);
			return objects.get(index+1);
		} else {
			return null;
		}
	}

	@Override
	public Object prevItemId(Object itemId) {
		if (!isFirstId(itemId)) {
			int index = objects.indexOf(itemId);
			return objects.get(index-1);
		} else {
			return null;
		}
	}

	@Override
	public Object firstItemId() {
		return objects.get(0);
	}

	@Override
	public Object lastItemId() {
		return objects.get(objects.size() - 1);
	}

	@Override
	public boolean isFirstId(Object itemId) {
		return firstItemId().equals(itemId);
	}

	@Override
	public boolean isLastId(Object itemId) {
		return lastItemId().equals(itemId);
	}

	@Override
	public Object addItemAfter(Object previousItemId) throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}

	@Override
	public Item addItemAfter(Object previousItemId, Object newItemId) throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void sort(Object[] propertyId, boolean[] ascending) {
		if (propertyId.length > 0) {
			Collections.sort(objects, new PropertyVaadinContainerComparator((PropertyInterface) propertyId[0], ascending[0]));
		}
	}

	@Override
	public Collection<?> getSortableContainerPropertyIds() {
		return properties;
	}
	
	private class PropertyVaadinContainerComparator implements Comparator<T> {
		private final PropertyInterface property;
		private final boolean asc;
		
		public PropertyVaadinContainerComparator(PropertyInterface property, boolean asc) {
			this.property = property;
			this.asc = asc;
		}
		
		@Override
		public int compare(T item1, T item2) {
			Object value1 = property.getValue(item1);
			Object value2 = property.getValue(item2);
			
			if (value1 instanceof Comparable && value2 instanceof Comparable) {
				int result = ((Comparable) value1).compareTo(value2);
				return asc ? result : -result;
			}
			return 0;
		}
	}
}
