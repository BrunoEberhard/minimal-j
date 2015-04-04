package org.minimalj.frontend.page;

import java.util.List;

import org.minimalj.frontend.toolkit.ClientToolkit;
import org.minimalj.frontend.toolkit.ClientToolkit.IContent;
import org.minimalj.frontend.toolkit.ClientToolkit.ITable;
import org.minimalj.frontend.toolkit.ClientToolkit.TableActionListener;
import org.minimalj.util.GenericUtils;
import org.minimalj.util.resources.Resources;

public abstract class AbstractSearchPage<T> implements SearchPage, TableActionListener<T> {

	private final Object[] keys;
	private String query;
	
	public AbstractSearchPage(Object[] keys) {
		this.keys = keys;
	}

	protected abstract List<T> load(String query);

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
	
}
