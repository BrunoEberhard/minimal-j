package ch.openech.mj.vaadin.toolkit;

import java.util.Arrays;
import java.util.List;

import ch.openech.mj.db.model.Constants;
import ch.openech.mj.resources.Resources;
import ch.openech.mj.toolkit.VisualTable;
import ch.openech.mj.vaadin.PropertyVaadinContainer;

import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.ui.Table;

public class VaadinVisualTable<T> extends Table implements VisualTable<T> {

	private final Class<T> clazz;
	private final String[] fieldNames;
	private List<T> objects;
	private ClickListener clickListener;
	private VaadinVisualTableItemClickListener tableClickListener;

	public VaadinVisualTable(Class<T> clazz, Object[] fields) {
		this.clazz = clazz;
		this.fieldNames = Constants.getConstants(fields);
		setSelectable(true);
		setMultiSelect(false);
		setSizeFull();
		
		for (String fieldName : fieldNames) {
			String header = Resources.getObjectFieldName(Resources.getResourceBundle(), clazz, fieldName);
			setColumnHeader(fieldName, header);
		}
	}
	
	@Override
	public void requestFocus() {
		focus();
	}

	@Override
	public void setObjects(List<T> list) {
		this.objects = list;
		setContainerDataSource(new PropertyVaadinContainer(clazz, list, (List<String>)Arrays.asList(fieldNames)));
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
	
	private class VaadinVisualTableItemClickListener implements ItemClickListener {
		@Override
		public void itemClick(ItemClickEvent arg0) {
			clickListener.clicked();
		}
	}
}
