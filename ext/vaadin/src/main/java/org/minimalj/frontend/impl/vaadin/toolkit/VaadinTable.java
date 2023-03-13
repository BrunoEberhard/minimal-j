package org.minimalj.frontend.impl.vaadin.toolkit;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.minimalj.frontend.Frontend.ITable;
import org.minimalj.frontend.Frontend.TableActionListener;
import org.minimalj.frontend.impl.util.ColumnFilter;
import org.minimalj.frontend.util.LazyLoadingList;
import org.minimalj.frontend.util.ListUtil;
import org.minimalj.model.Keys;
import org.minimalj.model.properties.Properties;
import org.minimalj.model.properties.Property;
import org.minimalj.util.resources.Resources;

import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.ItemClickEvent;
import com.vaadin.flow.data.provider.AbstractBackEndDataProvider;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.data.provider.QuerySortOrder;
import com.vaadin.flow.data.provider.SortDirection;
import com.vaadin.flow.data.renderer.BasicRenderer;
import com.vaadin.flow.data.selection.SelectionEvent;
import com.vaadin.flow.data.selection.SelectionListener;

public class VaadinTable<T> extends Grid<T> implements ITable<T> {
	private static final long serialVersionUID = 1L;

	private final TableActionListener<T> listener;
	private Class<?> clazz;
	// private Action action_delete = new ShortcutAction("Delete",
	// ShortcutAction.KeyCode.DELETE, null);
	// private Action action_enter = new ShortcutAction("Enter",
	// ShortcutAction.KeyCode.DELETE, null);

	public VaadinTable(Object[] keys, boolean multiSelect, TableActionListener<T> listener) {
		for (Object key : keys) {
			Property p = Keys.getProperty(key);
			if (clazz == null) {
				clazz = p.getDeclaringClass();
			}
			addColumn(new MinimalRenderer(p)).setHeader(Resources.getPropertyName(p)).setComparator((a, b) -> compareMaybeComparables(p.getValue(a), p.getValue(b))).setSortProperty(p.getPath());

			/*
			 * add column filters:
			 * 
			 * grid.getHeaderRows().clear(); HeaderRow headerRow = grid.appendHeaderRow();
			 * 
			 * headerRow.getCell(nameColumn).setComponent( createFilterHeader("Name",
			 * personFilter::setFullName)); headerRow.getCell(emailColumn).setComponent(
			 * createFilterHeader("Email", personFilter::setEmail));
			 * headerRow.getCell(professionColumn).setComponent(
			 * createFilterHeader("Profession", personFilter::setProfession));
			 */
		}

		addClassName("table");
		this.listener = listener;

		setSelectionMode(multiSelect ? SelectionMode.MULTI : SelectionMode.SINGLE);
		setSizeFull();

		VaadinTableListener tableListener = new VaadinTableListener();
		addItemClickListener(tableListener);
		addSelectionListener(tableListener);

		for (int i = 0; i < keys.length; i++) {
			getColumns().get(i).setSortable(true);
		}
	}

	private class MinimalRenderer extends BasicRenderer<T, Object> {
		private static final long serialVersionUID = 1L;

		private final Property property;

		protected MinimalRenderer(Property property) {
			super(T -> property.getValue(T));
			this.property = property;
		}

		@Override
		protected String getFormattedValue(Object object) {
			return org.minimalj.model.Rendering.toString(object, property);
		}
	}

	private class LazyBackEndDataProvider extends AbstractBackEndDataProvider<T, Object> {
		private static final long serialVersionUID = 1L;

		private final LazyLoadingList<T> list;

		public LazyBackEndDataProvider(LazyLoadingList<T> list) {
			this.list = list;
		}

		@Override
		protected Stream<T> fetchFromBackEnd(Query<T, Object> query) {
			List<QuerySortOrder> sortOrders = query.getSortOrders();
			Object[] sortKeys = new Object[sortOrders.size()];
			boolean[] sortDirections = new boolean[sortOrders.size()];
			for (int i = 0; i < sortOrders.size(); i++) {
				sortKeys[i] = Properties.getPropertyByPath(clazz, sortOrders.get(i).getSorted());
				sortDirections[i] = sortOrders.get(i).getDirection() == SortDirection.ASCENDING;
			}
			return ListUtil.get(list, new ColumnFilter[0], sortKeys, sortDirections, query.getOffset(), query.getLimit()).stream();
		}

		@Override
		protected int sizeInBackEnd(Query<T, Object> query) {
			return ListUtil.count(list, new ColumnFilter[0]);
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void setObjects(List<T> objects) {
		if (objects instanceof LazyLoadingList) {
			setItems((DataProvider) new LazyBackEndDataProvider((LazyLoadingList) objects));
		} else {
			setItems(objects);
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
