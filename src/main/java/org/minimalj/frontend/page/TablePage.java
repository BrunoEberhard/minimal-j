package org.minimalj.frontend.page;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.minimalj.backend.Backend;
import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.Frontend.IContent;
import org.minimalj.frontend.Frontend.ITable;
import org.minimalj.frontend.Frontend.TableActionListener;
import org.minimalj.frontend.action.Action;
import org.minimalj.util.ClassHolder;
import org.minimalj.util.GenericUtils;
import org.minimalj.util.IdUtils;
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
public abstract class TablePage<T> extends Page implements TableActionListener<T> {

	private transient boolean multiSelect;
	private transient Object[] columns;
	final ClassHolder<T> clazz;
	private transient ITable<T> table;
	private transient List<T> objects;
	private transient List<Action> actions;
	protected final Object[] nameArguments;
	
	/*
	 * this flag indicates if the next call of getContent should trigger a new loading
	 * of the data. A second call of getContent probably means that the user revisits
	 * the page and doesn't want to see the old data. 
	 */
	private transient boolean reloadFlag;

	@SuppressWarnings("unchecked")
	public TablePage() {
		this.multiSelect = allowMultiselect();

		this.clazz = new ClassHolder<T>((Class<T>) GenericUtils.getGenericClass(getClass()));
		this.nameArguments = new Object[] { Resources.getString(Resources.getResourceName(clazz.getClazz())) };
	}
	
	public TablePage(Object[] columns) {
		this();
		this.columns = columns;
	}

	protected boolean allowMultiselect() {
		return false;
	}
	
	protected Object[] getColumns() {
		return columns;
	}
	
	protected abstract List<T> load();

	@Override
	public String getTitle() {
		String title = Resources.getStringOrNull(getClass());
		if (title != null) {
			return title;
		} else {
			String className = Resources.getString(clazz.getClazz());
			return MessageFormat.format(Resources.getString(TablePage.class.getSimpleName() + ".title"), className);
		}
	}
	
	private ITable<T> createTable() {
		columns = getColumns();
		multiSelect = allowMultiselect();
		return Frontend.getInstance().createTable(columns, multiSelect, this);
	}
	
	@Override
	public IContent getContent() {
		if (table == null || multiSelect != allowMultiselect() || !Arrays.equals(columns, getColumns())) {
			table = createTable();
		}
		if (objects == null || reloadFlag) {
			objects = load();
			reloadFlag = true;
		}
		table.setObjects(objects);
		// for hidden/reshown detail it can happen that getContent is called
		// for a second time. Then the selection has to be cleared not to keep the old selection
		// (or table could be reused but than every Frontend has to take care about selection state)
		selectionChanged(null); // TODO should table.setObjects takes care of this?
		return table;
	}
	
	@Override
	public final List<Action> getActions() {
		if (actions == null) {
			actions = getTableActions();
		}
		return actions;
	}

	public List<Action> getTableActions() {
		return Collections.emptyList();
	}
		
	public void refresh() {
		if (table != null) {
			objects = load();
			table.setObjects(objects);
			reloadFlag = false;
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void selectionChanged(List<T> selectedObjects) {
		if (actions != null) {
			for (Action action : actions) {
				if (action instanceof TableSelectionAction) {
					((TableSelectionAction<T>) action).selectionChanged(selectedObjects);
				}
			}
		}
	}
	
	//

	public interface TableSelectionAction<T> {
		
		public abstract void selectionChanged(List<T> selectedObjects);
	}
	
	protected void delete(List<T> selectedObjects) {
		for (T object : selectedObjects) {
			Backend.delete(object.getClass(), IdUtils.getId(object));
		}
	}

	public class DeleteDetailAction extends Action implements TableSelectionAction<T> {
		private transient List<T> selectedObjects;
		
		public DeleteDetailAction() {
			selectionChanged(null);
		}
		
		@Override
		protected Object[] getNameArguments() {
			return nameArguments;
		}
		
		@Override
		public void action() {
			delete(selectedObjects);
			TablePage.this.refresh();
			TablePage.this.selectionChanged(Collections.emptyList());
		}

		@Override
		public void selectionChanged(List<T> selectedObjects) {
			this.selectedObjects = selectedObjects;
			setEnabled(selectedObjects != null && !selectedObjects.isEmpty());
		}
	}	
	
}