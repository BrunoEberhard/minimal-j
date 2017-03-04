package org.minimalj.frontend.page;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.minimalj.backend.Backend;
import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.Frontend.IContent;
import org.minimalj.frontend.Frontend.ITable;
import org.minimalj.frontend.Frontend.TableActionListener;
import org.minimalj.frontend.action.Action;
import org.minimalj.frontend.editor.Editor;
import org.minimalj.util.CloneHelper;
import org.minimalj.util.GenericUtils;
import org.minimalj.util.IdUtils;
import org.minimalj.util.resources.Resources;

/**
 * Shows a table of objects of one class. 
 *
 * @param <T> Class of objects in this overview
 */
public abstract class TablePage<T> extends Page implements TableActionListener<T> {

	private final boolean multiSelect;
	private final Object[] keys;
	private transient ITable<T> table;
	private transient List<T> objects;
	private transient List<TableSelectionAction> actions;
	private transient List<T> selectedObjects;
	
	/*
	 * this flag indicates if the next call of getContent should trigger a new loading
	 * of the data. A second call of getContent probably means that the user revisits
	 * the page and doesn't want to see the old data. 
	 */
	private transient boolean reloadFlag;
	
	public TablePage(Object[] keys) {
		this.multiSelect = allowMultiselect();
		this.keys = keys;
	}

	protected boolean allowMultiselect() {
		return false;
	}
	
	protected abstract List<T> load();

	@Override
	public String getTitle() {
		String title = Resources.getStringOrNull(getClass());
		if (title != null) {
			return title;
		} else {
			Class<?> tableClazz = GenericUtils.getGenericClass(getClass());
			String className = Resources.getString(tableClazz);
			return MessageFormat.format(Resources.getString(TablePage.class.getSimpleName() + ".title"), className);
		}
	}
	
	@Override
	public IContent getContent() {
		table = Frontend.getInstance().createTable(keys, multiSelect, this);
		if (objects == null || reloadFlag) {
			objects = load();
			reloadFlag = true;
		}
		table.setObjects(objects);
		// for hidden/reshown detail it can happen that getContent is called
		// for a second time. Then the selection has to be cleared not to keep the old selection
		// (or table could be reused but than every Frontend has to take care about selection state)
		selectedObjects = null;
		return table;
	}
	
	public void refresh() {
		if (table != null) {
			objects = load();
			table.setObjects(objects);
			reloadFlag = false;
		}
	}
	
	@Override
	public void selectionChanged(List<T> selectedObjects) {
		this.selectedObjects = selectedObjects;
		if (actions != null) {
			actions.stream().forEach(action -> action.selectionChanged(selectedObjects));
		}
	}
	
	public abstract class NewDetailEditor<DETAIL> extends Editor<DETAIL, T> {
		
		@Override
		protected DETAIL createObject() {
			@SuppressWarnings("unchecked")
			Class<DETAIL> clazz = (Class<DETAIL>) GenericUtils.getGenericClass(getClass());
			DETAIL newInstance = CloneHelper.newInstance(clazz);
			return newInstance;
		}
		
		@Override
		protected void finished(T result) {
			TablePage.this.refresh();
			if (TablePage.this instanceof TablePageWithDetail) {
				// after closing the editor the user expects the new object
				// to be displayed as the detail. This call provides that (opens the detail)
				TablePage.this.action(result);
			}
		}
	}
	
	public abstract class TableSelectionAction extends Action {
		
		protected TableSelectionAction() {
			if (actions == null) {
				actions = new ArrayList<>();
			}
			actions.add(this);
		}
		
		public abstract void selectionChanged(List<T> selectedObjects);
	}
	
	public class DeleteDetailAction extends TableSelectionAction {

		public DeleteDetailAction() {
			selectionChanged(selectedObjects);
		}
		
		@Override
		protected Object[] getNameArguments() {
			Class<?> clazz = GenericUtils.getGenericClass(TablePage.this.getClass());
			if (clazz != null) {
				String resourceName = Resources.getResourceName(clazz);
				return new Object[] { Resources.getString(resourceName) };
			} else {
				return null;
			}
		}
		
		@Override
		public void action() {
			for (T object : TablePage.this.selectedObjects) {
				Backend.delete(object.getClass(), IdUtils.getId(object));
			}
		}

		@Override
		public void selectionChanged(List<T> selectedObjects) {
			setEnabled(selectedObjects != null && !selectedObjects.isEmpty());
		}
	}	
	
	public static abstract class TablePageWithDetail<T, DETAIL_PAGE extends Page> extends TablePage<T> {
		
		private DETAIL_PAGE detailPage;

		public TablePageWithDetail(Object[] keys) {
			super(keys);
		}

		protected abstract DETAIL_PAGE createDetailPage(T mainObject);

		protected abstract DETAIL_PAGE updateDetailPage(DETAIL_PAGE page, T mainObject);

		protected DETAIL_PAGE updateDetailPage(DETAIL_PAGE page, List<T> selectedObjects) {
			if (selectedObjects == null || selectedObjects.size() != 1) {
				return null;
			} else {
				return updateDetailPage(page, selectedObjects.get(0));
			}
		}
		
		@Override
		public void action(T selectedObject) {
			if (detailPage != null) {
				updateDetailPage(Collections.singletonList(selectedObject));
			} else {
				detailPage = createDetailPage(selectedObject);
				if (detailPage != null) {
					Frontend.showDetail(TablePageWithDetail.this, detailPage);
				}
			}
		}

		@Override
		public void selectionChanged(List<T> selectedObjects) {
			super.selectionChanged(selectedObjects);
			boolean detailVisible = detailPage != null && Frontend.isDetailShown(detailPage); 
			if (detailVisible) {
				if (selectedObjects != null && !selectedObjects.isEmpty()) {
					updateDetailPage(selectedObjects);
				} else {
					Frontend.hideDetail(detailPage);
				}
			}
		}
		
		private void updateDetailPage(List<T> selectedObjects) {
			DETAIL_PAGE updatedDetailPage = updateDetailPage(detailPage, selectedObjects);
			if (Frontend.isDetailShown(detailPage)) {
				if (updatedDetailPage == null || updatedDetailPage != detailPage) {
					Frontend.hideDetail(detailPage);
				}
			}
			if (updatedDetailPage != null) {
				Frontend.showDetail(TablePageWithDetail.this, updatedDetailPage);
				detailPage = updatedDetailPage;
			}
		}
	}
	
	public static abstract class SimpleTablePageWithDetail<T> extends TablePageWithDetail<T, ObjectPage<T>> {

		public SimpleTablePageWithDetail(Object[] keys) {
			super(keys);
		}
		
		@Override
		protected ObjectPage<T> updateDetailPage(ObjectPage<T> detailPage, T mainObject) {
			detailPage.setObject(mainObject);
			return detailPage;
		}
	}
	
}
