package org.minimalj.frontend.page;

import java.util.List;

import org.minimalj.frontend.page.TablePage.TablePageWithDetail;
import org.minimalj.frontend.toolkit.ClientToolkit.TableActionListener;
import org.minimalj.util.GenericUtils;
import org.minimalj.util.resources.Resources;

public abstract class AbstractSearchPage<T, DETAIL> extends TablePageWithDetail<T, DETAIL> implements SearchPage, TableActionListener<T> {

	private String query;
	
	public AbstractSearchPage(Object[] keys) {
		super(keys);
	}
	
	protected List<T> load() {
		return load(query);
	}

	protected abstract List<T> load(String query);

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
	
	public static abstract class SimpleSearchPage<T> extends AbstractSearchPage<T, T> {

		public SimpleSearchPage(Object[] keys) {
			super(keys);
		}
		
		protected T load(T searchObject) {
			return (T) searchObject;
		}
	}
}
