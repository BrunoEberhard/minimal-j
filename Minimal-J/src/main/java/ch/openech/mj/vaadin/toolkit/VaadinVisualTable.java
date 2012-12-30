package ch.openech.mj.vaadin.toolkit;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.base.BaseLocal;

import ch.openech.mj.db.model.Constants;
import ch.openech.mj.db.model.PropertyInterface;
import ch.openech.mj.resources.Resources;
import ch.openech.mj.toolkit.VisualTable;
import ch.openech.mj.util.JodaFormatter;
import ch.openech.mj.vaadin.PropertyVaadinContainer;

import com.vaadin.data.Property;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.ui.Table;

public class VaadinVisualTable<T> extends Table implements VisualTable<T> {

	private final Class<T> clazz;
	private final List<PropertyInterface> properties = new ArrayList<PropertyInterface>();
	private final JodaFormatter jodaFormatter = new JodaFormatter();
	private List<T> objects;
	private ClickListener clickListener;
	private VaadinVisualTableItemClickListener tableClickListener;

	public VaadinVisualTable(Class<T> clazz, Object[] keys) {
		this.clazz = clazz;
		setSelectable(true);
		setMultiSelect(false);
		setSizeFull();
		
		for (Object key : keys) {
			PropertyInterface property = Constants.getProperty(key);
			properties.add(property);
			String header = Resources.getObjectFieldName(Resources.getResourceBundle(), property);
			setColumnHeader(property, header);
		}
	}
	
	@Override
	public void requestFocus() {
		focus();
	}

	@Override
	public void setObjects(List<T> list) {
		this.objects = list;
		setContainerDataSource(new PropertyVaadinContainer(clazz, list, properties));
	}

	@Override
	public void setSelectedObject(T object) {
		int id = objects.indexOf(object);
		if (id >= 0) {
			select(id);
		} else {
			select(null);
		}
	}

	@Override
	public T getSelectedObject() {
		if (getValue() != null) {
			return objects.get((Integer)getValue());
		} else {
			return null;
		}
	}

	@Override
	public int getSelectedIndex() {
		Object value = getValue();
		if (value != null) {
			return (Integer) objects.indexOf(getSelectedObject());
		} else {
			return -1;
		}
	}

	@Override
	public void setClickListener(ClickListener clickListener) {
		if (clickListener == null) {
			if (tableClickListener != null) {
				removeListener(tableClickListener);
				tableClickListener = null;
			}
		}
		this.clickListener = clickListener;
		if (clickListener != null) {
			if (tableClickListener == null) {
				tableClickListener = new VaadinVisualTableItemClickListener();
				addListener(tableClickListener);
			}
		}
	}
	
	@Override
	protected String formatPropertyValue(Object rowId, Object colId,
			Property property) {
		Object v = property.getValue();
		if (v instanceof BaseLocal) {
			return jodaFormatter.format(v);
		}
		return super.formatPropertyValue(rowId, colId, property);
	}
     
	private class VaadinVisualTableItemClickListener implements ItemClickListener {
		@Override
		public void itemClick(ItemClickEvent arg0) {
			clickListener.clicked();
		}
	}
}
