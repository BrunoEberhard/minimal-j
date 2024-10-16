package org.minimalj.frontend.page;

import java.lang.ref.SoftReference;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import org.minimalj.backend.Backend;
import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.Frontend.FormContent;
import org.minimalj.frontend.Frontend.IContent;
import org.minimalj.frontend.Frontend.ITable;
import org.minimalj.frontend.Frontend.TableActionListener;
import org.minimalj.frontend.action.Action;
import org.minimalj.frontend.editor.Result;
import org.minimalj.frontend.impl.json.JsonFrontend;
import org.minimalj.frontend.impl.swing.toolkit.SwingFrontend;
import org.minimalj.model.Keys;
import org.minimalj.util.GenericUtils;
import org.minimalj.util.resources.Resources;

/**
 * A page containing a table of objects of class T. There are some
 * specializations for this class:
 * <UL>
 * <LI>TableDetailPage: An object in the table can have a page where the details
 * for the object is show</LI>
 * <LI>TableEditorPage: An object in the table can be edited (or created or
 * deleted)
 * </UL>
 *
 * @param <T>
 *            Class of objects in this TablePage. Must be specified.
 */
public abstract class TablePage<T> implements Page, TableActionListener<T> {

	private boolean multiSelect;
	private Object[] columns;
	private SoftReference<ITable<T>> table;
	private SoftReference<List<Action>> actions;
	
	public TablePage() {
	}

	@SuppressWarnings("unchecked")
	protected Class<T> getClazz() {
		return (Class<T>) GenericUtils.getGenericClass(getClass());
	}

	protected String getResourceName() {
		return Resources.getResourceName(getClazz());
	}
	
	protected Object[] getNameArguments() {
		return new Object[] { Resources.getString(getResourceName()) };
	}
	
	protected boolean allowMultiselect() {
		return false;
	}
	
	protected abstract Object[] getColumns();
	
	protected abstract List<T> load();

	protected FormContent getOverview() {
		return null;
	}

	protected boolean immediateRefresh() {
		return true;
	}

	@Override
	public String getTitle() {
		String title = Resources.getStringOrNull(getClass());
		if (title != null) {
			return title;
		} else {
			String className = Resources.getString(getClazz());
			return MessageFormat.format(Resources.getString(TablePage.class.getSimpleName() + ".title"), className);
		}
	}
	
	@Override
	public IContent getContent() {
		ITable<T> table = this.table != null ? this.table.get() : null;
		Object[] columns = getColumns();
		// Only SwingFrontend and JsonFrontend implement table.setColumns at the moment
		if ((table == null || multiSelect != allowMultiselect() || !(Frontend.getInstance() instanceof SwingFrontend || Frontend.getInstance() instanceof JsonFrontend))) {
			this.columns = columns;
			this.multiSelect = allowMultiselect();
			table = Frontend.getInstance().createTable(columns, multiSelect, this);
			this.table = new SoftReference<>(table);
		} else if (!Arrays.equals(this.columns, columns)) {
			this.columns = columns;
			table.setColumns(columns);
		}
		refresh();

		FormContent overview = getOverview();
		if (overview != null) {
			Action refreshAction = null;
			if (!immediateRefresh()) {
				refreshAction = new Action(Resources.getString("RefreshAction")) {
					@Override
					public void run() {
						refresh();
					};
				};
			}
			return Frontend.getInstance().createFilteredTable(overview, table, refreshAction, null);
		} else {
			return table;
		}
	}

//	@Override
//	public int getMaxWidth() {
//		int maxWidth = 0;
//		for (Object column : columns) {
//			Property property = Keys.getProperty(column);
//			maxWidth += ListUtil.maxWidth(property);
//		}
//		return maxWidth + columns.length * 6;
//	}
	
	@Override
	public final List<Action> getActions() {
		List<Action> actions = this.actions != null ? this.actions.get() : null;
		if (actions == null) {
			actions = getTableActions();
			this.actions = new SoftReference<>(actions);
			actions.forEach(a -> {
				if (a instanceof Result) {
					((Result<?>) a).setFinishedListener(result -> refresh());
				}
			});
		}
		return actions;
	}

	public List<Action> getTableActions() {
		return Collections.emptyList();
	}
		
	public void refresh() {
		ITable<T> table = this.table != null ? this.table.get() : null;
		if (table != null) {
			Object[] newColumns = getColumns();
			if (!Keys.equalsIdentity(columns, newColumns)) {
				table.setColumns(newColumns);
				columns = newColumns;
			}
			table.setObjects(load());
		}
	}
	
	public void resetPage() {
		this.table = null;
		this.actions = null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void selectionChanged(List<T> selectedObjects) {
		List<Action> actions = this.actions != null ? this.actions.get() : null;
		if (actions != null) {
			T selectedObject = selectedObjects.isEmpty() ? null : selectedObjects.get(0);
			for (Action action : actions) {
				if (action instanceof ObjectsAction) {
					((ObjectsAction<T>) action).selectionChanged(selectedObjects);
				} else if (action instanceof ObjectAction) {
					((ObjectAction<T>) action).selectionChanged(selectedObject);
				}
			}
		}
	}
	
	//

	public interface ObjectsAction<T> {
		
		public abstract void selectionChanged(List<T> selectedObjects);
	}

	public interface ObjectAction<T> {
		
		public abstract void selectionChanged(T selectedObject);
	}

	public static abstract class AbstractObjectsAction<U> extends Action implements ObjectsAction<U>, Result<List<U>> {
		private Consumer<List<U>> finishedListener;
		private List<U> selectedObjects;

		public AbstractObjectsAction() {
			selectionChanged(Collections.emptyList());
		}
		
		public List<U> getSelectedObjects() {
			return selectedObjects;
		}

		public void setFinishedListener(Consumer<List<U>> finishedListener) {
			this.finishedListener = finishedListener;
		}

		public Consumer<List<U>> getFinishedListener() {
			return finishedListener;
		}
		
		@Override
		public final boolean isEnabled() {
			return super.isEnabled();
		}
		
		@Override
		public final void setEnabled(boolean enabled) {
			super.setEnabled(enabled);
		}
		
		@Override
		public void selectionChanged(List<U> selectedObjects) {
			this.selectedObjects = selectedObjects;
			setEnabled(accept(selectedObjects));
		}

		protected boolean accept(List<U> selectedObjects) {
			return !selectedObjects.isEmpty();
		}
	}
	
	public static abstract class AbstractObjectAction<U> extends Action implements ObjectAction<U>, Result<U> {
		private U selectedObject;
		private Consumer<U> finishedListener;

		public AbstractObjectAction() {
			setEnabled(false);
		}
		
		public U getSelectedObject() {
			return selectedObject;
		}

		@Override
		public void setFinishedListener(Consumer<U> finishedListener) {
			this.finishedListener = finishedListener;
		}
		
		public Consumer<U> getFinishedListener() {
			return finishedListener;
		}
		
		@Override
		public final boolean isEnabled() {
			return super.isEnabled();
		}
		
		@Override
		public final void setEnabled(boolean enabled) {
			super.setEnabled(enabled);
		}
		
		@Override
		public void selectionChanged(U selectedObject) {
			if (this.selectedObject != selectedObject) {
				this.selectedObject = selectedObject;
				setEnabled(accept(selectedObject));
			}
		}

		protected boolean accept(U selectedObject) {
			return selectedObject != null;
		}
	}
	
	protected void delete(List<T> selectedObjects) {
		for (T object : selectedObjects) {
			Backend.delete(object);
		}
	}

	public class DeleteDetailAction extends AbstractObjectsAction<T> {
		
		@Override
		protected Object[] getNameArguments() {
			return TablePage.this.getNameArguments();
		}
		
		@Override
		public void run() {
			delete(getSelectedObjects());
			TablePage.this.refresh();
			TablePage.this.selectionChanged(Collections.emptyList());
		}
	}

}