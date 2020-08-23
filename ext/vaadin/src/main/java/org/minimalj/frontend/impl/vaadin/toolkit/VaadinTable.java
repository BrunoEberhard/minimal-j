package org.minimalj.frontend.impl.vaadin.toolkit;

import java.util.ArrayList;
import java.util.List;

import org.minimalj.frontend.Frontend.ITable;
import org.minimalj.frontend.Frontend.TableActionListener;
import org.minimalj.model.Keys;
import org.minimalj.model.properties.PropertyInterface;
import org.minimalj.util.Sortable;
import org.minimalj.util.resources.Resources;

import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.ItemClickEvent;
import com.vaadin.flow.data.renderer.BasicRenderer;
import com.vaadin.flow.data.selection.SelectionEvent;
import com.vaadin.flow.data.selection.SelectionListener;

public class VaadinTable<T> extends Grid<T> implements ITable<T> {
	private static final long serialVersionUID = 1L;

	private final Object[] keys;
	private final TableActionListener<T> listener;
	// private Action action_delete = new ShortcutAction("Delete",
	// ShortcutAction.KeyCode.DELETE, null);
	// private Action action_enter = new ShortcutAction("Enter",
	// ShortcutAction.KeyCode.DELETE, null);

	public VaadinTable(Object[] keys, boolean multiSelect, TableActionListener<T> listener) {
		this.keys = keys;
		for (Object key : keys) {
			PropertyInterface p = Keys.getProperty(key);
			addColumn(new MinimalRenderer(p)).
				setHeader(Resources.getPropertyName(p)).
				setComparator((a, b) -> compareMaybeComparables(p.getValue(a), p.getValue(b)));
		}

		addClassName("table");
		this.listener = listener;

		setSelectionMode(multiSelect ? SelectionMode.MULTI : SelectionMode.SINGLE);
		setSizeFull();

		VaadinTableListener tableListener = new VaadinTableListener();
		addItemClickListener(tableListener);
		addSelectionListener(tableListener);
	}
	
	private class MinimalRenderer extends BasicRenderer<T, Object> {
		private static final long serialVersionUID = 1L;

		private final PropertyInterface property;
		
		protected MinimalRenderer(PropertyInterface property) {
			super(T -> property.getValue(T));
			this.property = property;
		}
		
	    @Override
	    protected String getFormattedValue(Object object) {
	        return org.minimalj.model.Rendering.toString(object, property);
	    }
	}

	@Override
	public void setObjects(List<T> objects) {
		setItems(objects);
		setSortableColumns(objects);
	}

	private void setSortableColumns(List<T> list) {
		Sortable sortable = null;
		if (list instanceof Sortable) {
			sortable = (Sortable) list;
		}
		for (int i = 0; i < keys.length; i++) {
			getColumns().get(i).setSortable(sortable != null && sortable.canSortBy(keys[i]));
		}
	}

	private class VaadinTableListener implements ComponentEventListener<ItemClickEvent<T>>, SelectionListener<Grid<T>, T> {
		private static final long serialVersionUID = 1L;

		@Override
		public void selectionChange(SelectionEvent<Grid<T>, T> event) {
			listener.selectionChanged(new ArrayList<>(event.getAllSelectedItems()));
		}

		@Override
		public void onComponentEvent(ItemClickEvent<T> event) {
			if (event.getClickCount() == 2) {
				listener.action(event.getItem());
			}

		}
	}

//	private class VaadinTableActionHandler implements Handler {
//		private static final long serialVersionUID = 1L;
//
//		@Override
//		public Action[] getActions(Object target, Object sender) {
//			if (sender == VaadinTable.this) {
//				return new Action[]{action_delete, action_enter};
//			} else {
//				return null;
//			}
//		}
//
//		
//	}

}
