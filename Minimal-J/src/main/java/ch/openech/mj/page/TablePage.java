package ch.openech.mj.page;

import java.util.List;

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

	private String text;
	private ITable<T> table;
	
	public TablePage(PageContext context, Object[] keys, String text) {
		super(context);
		this.text = text;
		table = ClientToolkit.getToolkit().createTable(keys);
		table.setClickListener(new TableClickListener());
		refresh();
	}

	protected ITable<T> getTable() {
		return table;
	}
	
	protected abstract List<T> load(String query);

	protected abstract void clicked(T selectedObject, List<T> selectedObjects);

	@Override
	public void refresh() {
		List<T> objects = load(text);
		table.setObjects(objects);
	}

	@Override
	public IComponent getComponent() {
		return table;
	}
	
	private class TableClickListener implements TableActionListener<T> {

		@Override
		public void action(T selectedObject, List<T> selectedObjects) {
			clicked(selectedObject, selectedObjects);
		}
	}
	
}
