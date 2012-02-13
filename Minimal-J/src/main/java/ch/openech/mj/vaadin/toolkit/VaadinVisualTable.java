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
import com.vaadin.ui.VerticalLayout;

public class VaadinVisualTable<T> extends VerticalLayout implements VisualTable<T> {

	private final Class<T> clazz;
	private final String[] fieldNames;
	private final Table table;
	private List<T> objects;
	private ClickListener clickListener;
	private VaadinVisualTableItemClickListener tableClickListener;

	public VaadinVisualTable(Class<T> clazz, Object[] fields) {
		this.clazz = clazz;
		this.fieldNames = Constants.getConstants(fields);
		this.table = new Table();
		table.setSelectable(true);
		table.setMultiSelect(false);
		table.setSizeFull();
		
		for (String fieldName : fieldNames) {
			String header = Resources.getObjectFieldName(Resources.getResourceBundle(), clazz, fieldName);
			table.setColumnHeader(fieldName, header);
		}
		
		addComponent(table);
	}
	
	@Override
	public void requestFocus() {
		focus();
	}

	@Override
	public void setObjects(List<T> list) {
		this.objects = list;
		table.setContainerDataSource(new PropertyVaadinContainer(clazz, list, (List<String>)Arrays.asList(fieldNames)));
	}

	@Override
	public void setSelectedObject(T object) {
		table.setValue(object);
	}

	@Override
	public T getSelectedObject() {
		if (table.getValue() != null) {
			return objects.get((Integer)table.getValue());
		} else {
			return null;
		}
	}

	@Override
	public int getSelectedIndex() {
		return (Integer) table.getValue();
	}

	@Override
	public void setClickListener(ClickListener clickListener) {
		if (clickListener == null) {
			if (tableClickListener != null) {
				table.removeListener(tableClickListener);
				tableClickListener = null;
			}
		}
		this.clickListener = clickListener;
		if (clickListener != null) {
			if (tableClickListener == null) {
				tableClickListener = new VaadinVisualTableItemClickListener();
				table.addListener(tableClickListener);
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
