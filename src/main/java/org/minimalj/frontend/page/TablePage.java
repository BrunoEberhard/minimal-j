package org.minimalj.frontend.page;

import java.util.List;

import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.Frontend.IContent;
import org.minimalj.frontend.Frontend.ITable;
import org.minimalj.frontend.Frontend.TableActionListener;


/**
 * Shows a table of objects of one class. 
 *
 * @param <T> Class of objects in this overview
 */
public abstract class TablePage<T> extends Page implements TableActionListener<T> {

	private final Object[] keys;
	private transient ITable<T> table;
	private transient List<T> objects;
	
	/*
	 * this flag indicates if the next call of getContent should trigger a new loading
	 * of the data. A second call of getContent probably means that the user revisits
	 * the page and doesn't want to see the old data. 
	 */
	private transient boolean reloadFlag;
	
	public TablePage(Object[] keys) {
		this.keys = keys;
	}

	protected abstract List<T> load();

	@Override
	public IContent getContent() {
		table = Frontend.getInstance().createTable(keys, this);
		if (objects == null || reloadFlag) {
			objects = load();
			reloadFlag = true;
		}
		table.setObjects(objects);
		return table;
	}

	public int getResultCount() {
		if (objects == null) {
			objects = load();
			reloadFlag = false;
		}
		return objects.size();
	}
	
	public void refresh() {
		if (table != null) {
			objects = load();
			table.setObjects(objects);
			reloadFlag = false;
		}
	}
	
	public static abstract class TablePageWithDetail<T, DETAIL_PAGE extends Page> extends TablePage<T> {
		
		private DETAIL_PAGE detailPage;

		public TablePageWithDetail(Object[] keys) {
			super(keys);
		}

		protected abstract DETAIL_PAGE createDetailPage(T mainObject);

		protected abstract DETAIL_PAGE updateDetailPage(DETAIL_PAGE page, T mainObject);

		@Override
		public void action(T selectedObject) {
			if (detailPage != null) {
				updateDetailPage(selectedObject);
			} else {
				detailPage = createDetailPage(selectedObject);
				if (detailPage != null) {
					Frontend.getBrowser().showDetail(detailPage);
				}
			}
		}

		@Override
		public void selectionChanged(T selectedObject, List<T> selectedObjects) {
			if (detailPage != null && Frontend.getBrowser().isDetailShown(detailPage)) {
				updateDetailPage(selectedObject);
			}
		}
		
		private void updateDetailPage(T selectedObject) {
			DETAIL_PAGE updatedDetailPage = updateDetailPage(detailPage, selectedObject);
			if (Frontend.getBrowser().isDetailShown(detailPage)) {
				if (updatedDetailPage == null || updatedDetailPage != detailPage) {
					Frontend.getBrowser().hideDetail(detailPage);
				}
			}
			if (updatedDetailPage != null) {
				Frontend.getBrowser().showDetail(updatedDetailPage);
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
