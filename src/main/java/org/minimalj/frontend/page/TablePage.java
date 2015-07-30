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
	
	public TablePage(Object[] keys) {
		this.keys = keys;
	}

	protected abstract List<T> load();

	@Override
	public IContent getContent() {
		table = Frontend.getInstance().createTable(keys, this);
		if (objects == null) {
			objects = load();
		}
		table.setObjects(objects);
		return table;
	}

	public int getResultCount() {
		if (objects == null) {
			objects = load();
		}
		return objects.size();
	}
	
	public void refresh() {
		if (table != null) {
			objects = load();
			table.setObjects(objects);
		}
	}
	
	public static abstract class TablePageWithDetail<T, DETAIL> extends TablePage<T> {
		
		public TablePageWithDetail(Object[] keys) {
			super(keys);
		}

		private ObjectPage<DETAIL> detailPage;
		
		protected abstract DETAIL load(T searchObject);
		
		protected abstract ObjectPage<DETAIL> createDetailPage(DETAIL initialObject);
	
		@Override
		public void selectionChanged(T selectedObject, List<T> selectedObjects) {
			if (detailPage != null && Frontend.getBrowser().isDetailShown(detailPage)) {
				DETAIL selectedDetailObject = selectedObject != null ? load(selectedObject) : null;
				if (selectedDetailObject != null) {
					detailPage.setObject(selectedDetailObject);
				} else {
					Frontend.getBrowser().hideDetail(detailPage);
				}
			}
		}
		
		@Override
		public void action(T selectedObject) {
			DETAIL selectedDetailObject = selectedObject != null ? load(selectedObject) : null;
			if (detailPage != null) {
				detailPage.setObject(selectedDetailObject);
			} else {
				detailPage = createDetailPage(selectedDetailObject);
			}
			Frontend.getBrowser().showDetail(detailPage);
		}
	}
	
	public static abstract class SimpleTablePageWithDetail<T> extends TablePageWithDetail<T, T> {

		public SimpleTablePageWithDetail(Object[] keys) {
			super(keys);
		}
		
		@Override
		protected T load(T searchObject) {
			return searchObject;
		}
	}

	
}
