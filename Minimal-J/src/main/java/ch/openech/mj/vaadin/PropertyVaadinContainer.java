package ch.openech.mj.vaadin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import ch.openech.mj.db.model.AccessorInterface;
import ch.openech.mj.db.model.Format;
import ch.openech.mj.db.model.Formats;
import ch.openech.mj.edit.value.PropertyAccessor;

import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.Property;

public class PropertyVaadinContainer implements Container {

	private final Class<?> clazz;
	private final List<String> propertyIds;
	private final List<?> list;

	public PropertyVaadinContainer(Class<?> clazz, List<?> list, List<String> propertyIds) {
		this.propertyIds = propertyIds;
		this.list = list;
		this.clazz = clazz;
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
		List<Integer> ids = new ArrayList<Integer>();
		for (int i = 0; i<list.size(); i++) {
			ids.add(i);
		}
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
}
