package org.minimalj.frontend.page;

import java.util.List;

import org.minimalj.frontend.toolkit.ClientToolkit;
import org.minimalj.frontend.toolkit.ClientToolkit.IContent;
import org.minimalj.frontend.toolkit.ClientToolkit.ITable;
import org.minimalj.frontend.toolkit.ClientToolkit.TableActionListener;


/**
 * Shows a table of objects of one class. 
 *
 * @param <T> Class of objects in this overview
 */
public abstract class TablePage<T> implements Page, TableActionListener<T> {

	private final Object[] keys;
	private transient ITable<T> table;
	
	public TablePage(Object[] keys) {
		this.keys = keys;
	}

	protected abstract List<T> load();

	@Override
	public IContent getContent() {
		table = ClientToolkit.getToolkit().createTable(keys, this);
		List<T> objects = load();
		table.setObjects(objects);
		return table;
	}
	
	public void refresh() {
		if (table != null) {
			List<T> objects = load();
			table.setObjects(objects);
		}
	}
	
	public static abstract class TablePageWithDetail<T, DETAIL> extends TablePage<T> implements PageWithDetail {
		
		public TablePageWithDetail(Object[] keys) {
			super(keys);
		}

		private ObjectPage<DETAIL> objectPage;
		
		protected abstract DETAIL load(T searchObject);
		
		protected abstract ObjectPage<DETAIL> createPage(DETAIL initialObject);
	
		@Override
		public void selectionChanged(T selectedObject, List<T> selectedObjects) {
			if (objectPage != null) {
				DETAIL selectedDetailObject = selectedObject != null ? load(selectedObject) : null;
				if (selectedDetailObject != null) {
					objectPage.setObject(selectedDetailObject);
				} else {
					objectPage = null;
					ClientToolkit.getToolkit().show(this, null);
				}
			}
		}
			
		@Override
		public void detailClosed(Page page) {
			if (page == objectPage) {
				objectPage = null;
			}
		}
		
		@Override
		public void action(T selectedObject) {
			DETAIL selectedDetailObject = selectedObject != null ? load(selectedObject) : null;
			if (objectPage != null) {
				objectPage.setObject(selectedDetailObject);
			} else {
				objectPage = createPage(selectedDetailObject);
				ClientToolkit.getToolkit().show(this, objectPage);
			}
		}
	}
	
	public static abstract class SimpleTablePageWithDetail<T> extends TablePageWithDetail<T, T> {

		public SimpleTablePageWithDetail(Object[] keys) {
			super(keys);
		}
		
		protected T load(T searchObject) {
			return (T) searchObject;
		}
	}

	
}
