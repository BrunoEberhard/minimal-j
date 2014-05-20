package org.minimalj.frontend.vaadin.toolkit;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.ReadablePartial;
import org.minimalj.frontend.toolkit.ITable;
import org.minimalj.frontend.vaadin.PropertyVaadinContainer;
import org.minimalj.model.Keys;
import org.minimalj.model.PropertyInterface;
import org.minimalj.util.JodaFormatter;
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
	private final JodaFormatter jodaFormatter = new JodaFormatter();
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
	public void setClickListener(TableActionListener clickListener) {
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
		Object v = property.getValue();
		if (v instanceof ReadablePartial) {
			return jodaFormatter.format(v, (PropertyInterface) colId);
		}
		return super.formatPropertyValue(rowId, colId, property);
	}
     
	private class VaadinTableItemClickListener implements ItemClickListener {
		private static final long serialVersionUID = 1L;

		@Override
		public void itemClick(ItemClickEvent event) {
			if (event.isDoubleClick()) {
				T id = (T) event.getItemId();
				listener.action(id, getSelectedObjects());
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

	public List<T> getSelectedObjects() {
		List<T> selectedIds = new ArrayList<>();
		for (Object itemId : getItemIds()) {
			if (isSelected(itemId)) {
				selectedIds.add((T) itemId);
			}
		}
		return selectedIds;
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
