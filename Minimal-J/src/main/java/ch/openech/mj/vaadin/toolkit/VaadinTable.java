package ch.openech.mj.vaadin.toolkit;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import org.joda.time.ReadablePartial;

import ch.openech.mj.model.Keys;
import ch.openech.mj.model.PropertyInterface;
import ch.openech.mj.resources.Resources;
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

	private final Class<T> clazz;
	private final List<PropertyInterface> properties = new ArrayList<PropertyInterface>();
	private final JodaFormatter jodaFormatter = new JodaFormatter();
	private List<T> objects;
	private ActionListener listener;
	private VaadinTableItemClickListener tableClickListener;
	private Action action_delete = new ShortcutAction("Delete", ShortcutAction.KeyCode.DELETE, null);
	private Action action_enter = new ShortcutAction("Enter", ShortcutAction.KeyCode.DELETE, null);


	public VaadinTable(Class<T> clazz, Object[] keys) {
		this.clazz = clazz;
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
	public void setObjects(List<T> list) {
		this.objects = list;
		setContainerDataSource(new PropertyVaadinContainer(clazz, list, properties));
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
	public void setClickListener(ActionListener clickListener) {
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
		@Override
		public void itemClick(ItemClickEvent event) {
			if (event.isDoubleClick()) {
				ActionEvent actionEvent = new ActionEvent(VaadinTable.this, 0, "Clicked");
				listener.actionPerformed(actionEvent);
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

	// TODO !
	
	@Override
	public List<T> getSelectedObjects() {
		List<T> selectedObjects = new ArrayList<>();
		for (Object itemId : getItemIds()) {
			if (isSelected(itemId)) {
				selectedObjects.add((T) getItem(itemId));
			}
		}
		return selectedObjects;
	}

	@Override
	public void setDeleteListener(ActionListener listener) {
	}

	@Override
	public void setInsertListener(ActionListener listener) {
		// TODO Auto-generated method stub
	}

	@Override
	public void setFunctionListener(int function, ActionListener listener) {
		// TODO Auto-generated method stub
	}
	
}
