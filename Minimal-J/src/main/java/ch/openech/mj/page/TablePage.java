package ch.openech.mj.page;

import java.util.List;

import ch.openech.mj.search.Search;
import ch.openech.mj.toolkit.ClientToolkit;
import ch.openech.mj.toolkit.IComponent;
import ch.openech.mj.toolkit.ITable;
import ch.openech.mj.toolkit.ITable.TableActionListener;


/**
 * Shows a table of objects of one class. 
 *
 * @param <T> Class of objects in this overview
 */
public abstract class TablePage<T> extends AbstractPage implements RefreshablePage {

	private final Search<T> search;
	private String text;
	private ITable<T> table;

	public TablePage(PageContext context, Search<T> search, Object[] keys, String text) {
		super(context);
		this.search = search;
		this.text = text;
		table = ClientToolkit.getToolkit().createTable(search, keys);
		table.setClickListener(new TableClickListener());
		refresh();
	}

	protected ITable<T> getTable() {
		return table;
	}

	protected abstract void clicked(int selectedId, List<Integer> selectedIds);

	@Override
	public IComponent getComponent() {
		return table;
	}
	
	private class TableClickListener implements TableActionListener {
		@Override
		public void action(int selectedId, List<Integer> selectedIds) {
			clicked(selectedId, selectedIds);
		}
	}
	
	@Override
	public void refresh() {
		try {
			List<Integer> ids = search.search(text);
			table.setIds(ids);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
