package ch.openech.mj.vaadin.toolkit;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.ReadablePartial;

import ch.openech.mj.model.Keys;
import ch.openech.mj.model.PropertyInterface;
import ch.openech.mj.resources.Resources;
import ch.openech.mj.search.Lookup;
import ch.openech.mj.toolkit.ITable;
import ch.openech.mj.util.JodaFormatter;
import ch.openech.mj.vaadin.PropertyVaadinContainer;

import com.vaadin.data.Property;
import com.vaadin.event.Action;
import com.vaadin.event.Action.Handler;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.event.ShortcutAction;
import com.vaadin.ui.Table;

public class VaadinTable<T> extends Table implements ITable<T> {
	private static final long serialVersionUID = 1L;

	private final Lookup<T> lookup;
	private final List<PropertyInterface> properties = new ArrayList<PropertyInterface>();
	private final JodaFormatter jodaFormatter = new JodaFormatter();
	private List<Integer> ids;
	private TableActionListener<T> listener;
	private VaadinTableItemClickListener tableClickListener;
	private Action action_delete = new ShortcutAction("Delete", ShortcutAction.KeyCode.DELETE, null);
	private Action action_enter = new ShortcutAction("Enter", ShortcutAction.KeyCode.DELETE, null);


	public VaadinTable(Lookup<T> lookup, Object[] keys) {
		this.lookup = lookup;
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
	public void setIds(List<Integer> ids) {
		this.ids = ids;
		setContainerDataSource(new PropertyVaadinContainer<T>(ids, lookup, properties));
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
				Integer id = (Integer) event.getItemId();
				listener.action(lookup.lookup(id), getSelectedObjects());
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
		List<T> selectedObjects = new ArrayList<>();
		for (Object itemId : getItemIds()) {
			if (isSelected(itemId)) {
				selectedObjects.add(lookup.lookup((Integer) itemId));
			}
		}
		return selectedObjects;
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
