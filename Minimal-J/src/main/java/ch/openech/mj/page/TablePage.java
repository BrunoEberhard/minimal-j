package ch.openech.mj.page;

import java.util.ArrayList;
import java.util.List;

import ch.openech.mj.toolkit.ClientToolkit;
import ch.openech.mj.toolkit.IComponent;
import ch.openech.mj.toolkit.ITable;
import ch.openech.mj.toolkit.ITable.TableActionListener;
import ch.openech.mj.util.IdUtils;


/**
 * Shows a table of objects of one class. 
 *
 * @param <T> Class of objects in this overview
 */
public abstract class TablePage<T> extends AbstractPage implements RefreshablePage {

	private String text;
	private ITable<T> table;
	private List<T> objects;
	
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

	/**
	 * not only shows the selected object but provides the pageContext a list of links.
	 * The user can sroll up/down through all (not only the selected) rows of the table
	 */
	protected void showDetail(Class<? extends Page> detailPageClass, T selectedObject) {
		List<String> links = new ArrayList<>(objects.size());
		for (Object object : objects) {
			long id = IdUtils.getId(object);
			links.add(PageLink.link(detailPageClass, id));
		}
		int index = objects.indexOf(selectedObject);
		getPageContext().show(links, index);
	}
	
	@Override
	public void refresh() {
		objects = load(text);
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
