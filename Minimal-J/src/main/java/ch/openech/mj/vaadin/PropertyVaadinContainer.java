package ch.openech.mj.vaadin;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import ch.openech.mj.model.PropertyInterface;
import ch.openech.mj.search.Lookup;

import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.Property;

public class PropertyVaadinContainer<T> implements Container.Sortable {
	private static final long serialVersionUID = 1L;

	private final List<PropertyInterface> properties;
	private final List<Integer> ids;
	private final Lookup<T> lookup;

	public PropertyVaadinContainer(List<Integer> ids, Lookup<T> lookup, List<PropertyInterface> properties) {
		this.ids = ids;
		this.lookup = lookup;
		this.properties = properties;
	}
	
	@Override
	public Item getItem(Object itemId) {
		return new PropertyVaadinContainerItem(lookup.lookup((Integer)itemId));
	}

	@Override
	public Collection<?> getContainerPropertyIds() {
		return properties;
	}

	@Override
	public Collection<?> getItemIds() {
		return ids;
	}

	@Override
	public Property getContainerProperty(Object itemId, Object propertyId) {
		Item item = getItem(itemId);
        return item.getItemProperty(propertyId);
	}

	@Override
	public Class<?> getType(Object propertyId) {
		PropertyInterface property = (PropertyInterface) propertyId;
		return property.getFieldClazz();
	}

	@Override
	public int size() {
		return ids.size();
	}

	@Override
	public boolean containsId(Object itemId) {
		return ids.contains(itemId);
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
			return new PropertyVaadinContainerProperty(property.getFieldClazz(), value);
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
			int index = ids.indexOf(itemId);
			return ids.get(index+1);
		} else {
			return null;
		}
	}

	@Override
	public Object prevItemId(Object itemId) {
		if (!isFirstId(itemId)) {
			int index = ids.indexOf(itemId);
			return ids.get(index-1);
		} else {
			return null;
		}
	}

	@Override
	public Object firstItemId() {
		return ids.get(0);
	}

	@Override
	public Object lastItemId() {
		return ids.get(ids.size() - 1);
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
			Collections.sort(ids, new PropertyVaadinContainerComparator((PropertyInterface) propertyId[0], ascending[0]));
		}
	}

	@Override
	public Collection<?> getSortableContainerPropertyIds() {
		return properties;
	}
	
	private class PropertyVaadinContainerComparator implements Comparator<Integer> {
		private final PropertyInterface property;
		private final boolean asc;
		
		public PropertyVaadinContainerComparator(PropertyInterface property, boolean asc) {
			this.property = property;
			this.asc = asc;
		}
		
		@Override
		public int compare(Integer o1, Integer o2) {
			Object item1 = lookup.lookup(o1);
			Object item2 = lookup.lookup(o2);
			
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
