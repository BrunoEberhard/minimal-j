package org.minimalj.frontend.page;

import java.util.List;

import org.minimalj.frontend.toolkit.ClientToolkit;
import org.minimalj.frontend.toolkit.ClientToolkit.IContent;
import org.minimalj.frontend.toolkit.ClientToolkit.ITable;
import org.minimalj.frontend.toolkit.ClientToolkit.TableActionListener;
import org.minimalj.util.GenericUtils;
import org.minimalj.util.resources.Resources;

public abstract class AbstractSearchPage<T, D> implements SearchPage, PageWithDetail, TableActionListener<T> {

	private final Object[] keys;
	private String query;
	private ObjectPage<D> objectPage;
	
	public AbstractSearchPage(Object[] keys) {
		this.keys = keys;
	}

	protected abstract List<T> load(String query);

	protected D load(T searchObject) {
		return (D) searchObject;
	}
	
	protected abstract ObjectPage<D> createPage(D initialObject);
	
	@Override
	public IContent getContent() {
		ITable<T> table = ClientToolkit.getToolkit().createTable(keys, this);
		List<T> objects = load(query);
		table.setObjects(objects);
		return table;
	}
	
	@Override
	public void setQuery(String query) {
		this.query = query;
	}

	@Override
	public String getTitle() {
		return getName() + " / " + query;
	}

	@Override
	public String getName() {
		Class<?> genericClass = GenericUtils.getGenericClass(getClass());
		return Resources.getString(genericClass);
	}
	
	@Override
	public void selectionChanged(T selectedObject, List<T> selectedObjects) {
		if (objectPage != null) {
			D selectedDetailObject = selectedObject != null ? load(selectedObject) : null;
			objectPage.setObject(selectedDetailObject);
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
		D selectedDetailObject = selectedObject != null ? load(selectedObject) : null;
		if (objectPage != null) {
			objectPage.setObject(selectedDetailObject);
		} else {
			objectPage = createPage(selectedDetailObject);
			ClientToolkit.getToolkit().show(objectPage, this);
		}
	}
	
	public static abstract class SimpleSearchPage<T> extends AbstractSearchPage<T, T> {

		public SimpleSearchPage(Object[] keys) {
			super(keys);
		}
	}
}
