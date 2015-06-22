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
		if (table == null) {
			table = ClientToolkit.getToolkit().createTable(keys, this);
		}
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
	
}
