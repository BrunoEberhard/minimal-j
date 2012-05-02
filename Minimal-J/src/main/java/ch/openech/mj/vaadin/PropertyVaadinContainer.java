package ch.openech.mj.vaadin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import ch.openech.mj.db.model.AccessorInterface;
import ch.openech.mj.db.model.Format;
import ch.openech.mj.db.model.Formats;
import ch.openech.mj.edit.value.PropertyAccessor;

import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.Property;

public class PropertyVaadinContainer implements Container.Sortable {

	private final Class<?> clazz;
	private final List<String> propertyIds;
	private final List<?> list;
	private final List<Integer> ids;

	public PropertyVaadinContainer(Class<?> clazz, List<?> list, List<String> propertyIds) {
		this.propertyIds = propertyIds;
		this.list = list;
		this.clazz = clazz;
		
		ids = new ArrayList<Integer>();
		for (int i = 0; i<list.size(); i++) {
			ids.add(i);
		}
	}
	
	@Override
	public Item getItem(Object itemId) {
		return new PropertyVaadinContainerItem(list.get((Integer)itemId));
	}

	@Override
	public Collection<?> getContainerPropertyIds() {
		return propertyIds;
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
		AccessorInterface accessor = PropertyAccessor.getAccessor(clazz, (String)propertyId);
		Format format = Formats.getInstance().getFormat(accessor);
		if (format != null) {
			return format.getClazz();
		} else {
			return String.class;
		}
	}

	@Override
	public int size() {
		return list.size();
	}

	@Override
	public boolean containsId(Object itemId) {
		Integer id = (Integer) itemId;
		return id >= 0 && id < list.size();
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
		private final Object object;
		
		public PropertyVaadinContainerItem(Object object) {
			this.object = object;
		}
		
		@Override
		public Property getItemProperty(Object id) {
			AccessorInterface accessor = PropertyAccessor.getAccessor(object.getClass(), (String) id);
			Object value = PropertyAccessor.get(object, (String) id);
			Format format = Formats.getInstance().getFormat(accessor);
			if (format != null) {
				value = format.display((String) value);
			}
			return new PropertyVaadinContainerProperty(accessor.getClazz(), value);
		}

		@Override
		public Collection<?> getItemPropertyIds() {
			return propertyIds;
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
			return "" + object;
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
		return ids.get(list.size() - 1);
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
			Collections.sort(ids, new PropertyVaadinContainerComparator((String) propertyId[0], ascending[0]));
		}
	}

	@Override
	public Collection<?> getSortableContainerPropertyIds() {
		return propertyIds;
	}
	
	private class PropertyVaadinContainerComparator implements Comparator<Integer> {
		private final String propertyId;
		private final boolean asc;
		
		public PropertyVaadinContainerComparator(String property, boolean asc) {
			this.propertyId = property;
			this.asc = asc;
		}
		
		@Override
		public int compare(Integer o1, Integer o2) {
			Object item1 = list.get(o1);
			Object item2 = list.get(o2);
			
			Object value1 = PropertyAccessor.get(item1, propertyId);
			Object value2 = PropertyAccessor.get(item2, propertyId);
			
			if (value1 instanceof Comparable && value2 instanceof Comparable) {
				int result = ((Comparable) value1).compareTo(value2);
				return asc ? result : -result;
			}
			return 0;
		}
	}
}
