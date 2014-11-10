package org.minimalj.frontend.vaadin.toolkit;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import org.minimalj.frontend.toolkit.ITable;
import org.minimalj.frontend.vaadin.PropertyVaadinContainer;
import org.minimalj.model.Keys;
import org.minimalj.model.PropertyInterface;
import org.minimalj.util.DateUtils;
import org.minimalj.util.resources.Resources;

import com.vaadin.data.Property;
import com.vaadin.event.Action;
import com.vaadin.event.Action.Handler;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.event.ShortcutAction;
import com.vaadin.ui.Table;

public class VaadinTable<T> extends Table implements ITable<T> {
	private static final long serialVersionUID = 1L;

	private final List<PropertyInterface> properties = new ArrayList<PropertyInterface>();
	private TableActionListener<T> listener;
	private VaadinTableItemClickListener tableClickListener;
	private Action action_delete = new ShortcutAction("Delete", ShortcutAction.KeyCode.DELETE, null);
	private Action action_enter = new ShortcutAction("Enter", ShortcutAction.KeyCode.DELETE, null);


	public VaadinTable(Object[] keys) {
		setSelectable(true);
		setMultiSelect(false);
		setSizeFull();
		
		for (Object key : keys) {
			PropertyInterface property = Keys.getProperty(key);
			properties.add(property);
			String header = Resources.getObjectFieldName(Resources.getResourceBundle(), property);
			setColumnHeader(property, header);
		}
		
		addActionHandler(new VaadinTableActionHandler());
	}
	
	@Override
	public void setObjects(List<T> objects) {
		setContainerDataSource(new PropertyVaadinContainer<T>(objects, properties));
	}

	@Override
	public void setClickListener(TableActionListener<T> clickListener) {
		if (clickListener == null) {
			if (tableClickListener != null) {
				removeListener(tableClickListener);
				tableClickListener = null;
			}
		}
		this.listener = clickListener;
		if (clickListener != null) {
			if (tableClickListener == null) {
				tableClickListener = new VaadinTableItemClickListener();
				addListener(tableClickListener);
			}
		}
	}
	
	@Override
	protected String formatPropertyValue(Object rowId, Object colId,
			Property property) {
		Object value = property.getValue();
		if (value instanceof LocalTime) {
			return DateUtils.getTimeFormatter((PropertyInterface) colId).format((LocalTime) value); 
		} else if (value instanceof LocalDate) {
			return DateUtils.DATE_FORMATTER.format((LocalDate) value); 
		}
		return super.formatPropertyValue(rowId, colId, property);
	}
     
	private class VaadinTableItemClickListener implements ItemClickListener {
		private static final long serialVersionUID = 1L;

		@Override
		public void itemClick(ItemClickEvent event) {
			if (event.isDoubleClick()) {
				T id = (T) event.getItemId();
				VaadinClientToolkit.setWindow(event.getComponent().getWindow());
				listener.action(id, getObjects());
				VaadinClientToolkit.setWindow(null);
			}
		}
	}
	
	private class VaadinTableActionHandler implements Handler {
		private static final long serialVersionUID = 1L;

		@Override
		public Action[] getActions(Object target, Object sender) {
			if (sender == VaadinTable.this) {
				return new Action[]{action_delete, action_enter};
			} else {
				return null;
			}
		}

		@Override
		public void handleAction(Action action, Object sender, Object target) {
			System.out.println(action);
		}
		
	}

	public List<T> getObjects() {
		List<T> objects = new ArrayList<>();
		for (Object itemId : getItemIds()) {
			objects.add((T) itemId);
		}
		return objects;
	}

	@Override
	public void setDeleteListener(TableActionListener<T> listener) {
		// TODO Delete Action on Vaadin Table
	}

	@Override
	public void setInsertListener(InsertListener listener) {
		// TODO Insert Action on Vaadin Table
	}

	@Override
	public void setFunctionListener(int function, TableActionListener<T> listener) {
		// TODO Function Action on Vaadin Table
	}
	
}
