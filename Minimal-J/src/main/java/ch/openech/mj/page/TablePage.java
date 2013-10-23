package ch.openech.mj.page;

import java.util.List;

import ch.openech.mj.search.Item;
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
	private ITable table;
	private List<Item> items;

	public TablePage(PageContext context, Search<T> search, String text) {
		super(context);
		this.search = search;
		this.text = text;
		table = ClientToolkit.getToolkit().createTable(search.getKeys());
		table.setClickListener(new TableClickListener());
		refresh();
	}

	protected ITable getTable() {
		return table;
	}

	protected abstract void clicked(Item item, List<Item> items);

	@Override
	public IComponent getComponent() {
		return table;
	}
	
	private class TableClickListener implements TableActionListener {
		@Override
		public void action(Item selectedItem, List<Item> selectedItems) {
			clicked(selectedItem, selectedItems);
		}
	}
	
	protected List<Item> getItems() {
		return items;
	}
	
	@Override
	public void refresh() {
		try {
			items = search.search(text);
			table.setObjects(items);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
