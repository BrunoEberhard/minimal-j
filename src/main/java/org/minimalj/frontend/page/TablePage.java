package org.minimalj.frontend.page;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.minimalj.backend.Backend;
import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.Frontend.FormContent;
import org.minimalj.frontend.Frontend.IContent;
import org.minimalj.frontend.Frontend.ITable;
import org.minimalj.frontend.Frontend.TableActionListener;
import org.minimalj.frontend.action.Action;
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
public abstract class TablePage<T> extends Page implements TableActionListener<T> {

	private boolean multiSelect;
	private Object[] columns;
	private transient ITable<T> table;
	private transient List<Action> actions;
	private Object[] nameArguments;
	
	public TablePage() {
		this.multiSelect = allowMultiselect();
	}
	
	public TablePage(Object[] columns) {
		this();
		this.columns = columns;
	}

	@SuppressWarnings("unchecked")
	protected Class<T> getClazz() {
		return (Class<T>) GenericUtils.getGenericClass(getClass());
	}

	protected String getResourceName() {
		return Resources.getResourceName(getClazz());
	}
	
	protected Object[] getNameArguments() {
		if (nameArguments == null) {
			nameArguments = new Object[] { Resources.getString(getResourceName()) };
		}
		return nameArguments;
	}
	
	protected boolean allowMultiselect() {
		return false;
	}
	
	protected Object[] getColumns() {
		return columns;
	}
	
	protected abstract List<T> load();

	protected FormContent getOverview() {
		return null;
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
		table.setObjects(load());

		FormContent overview = getOverview();
		if (overview != null) {
			return Frontend.getInstance().createFormTableContent(overview, table);
		} else {
			return table;
		}
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
			table.setObjects(load());
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
			Backend.delete(object);
		}
	}

	public class DeleteDetailAction extends Action implements TableSelectionAction<T> {
		private transient List<T> selectedObjects;
		
		public DeleteDetailAction() {
			selectionChanged(null);
		}
		
		@Override
		protected Object[] getNameArguments() {
			return TablePage.this.getNameArguments();
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