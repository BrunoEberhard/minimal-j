package ch.openech.mj.page;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import ch.openech.mj.toolkit.ClientToolkit;
import ch.openech.mj.toolkit.IComponent;
import ch.openech.mj.toolkit.ITable;


/**
 * Shows a table of objects of one class. 
 *
 * @param <T> Class of objects in this overview
 */
public abstract class TablePage<T> extends AbstractPage implements RefreshablePage {

	private String text;
	private ITable<T> table;

	public TablePage(PageContext context, Object[] FIELDS, String text) {
		super(context);
		this.text = text;
		table = ClientToolkit.getToolkit().createTable((Class<T>)Object.class, FIELDS);
		table.setClickListener(new TableClickListener());
		refresh();
	}

	protected ITable<T> getTable() {
		return table;
	}

	protected abstract void clicked(T object);

	protected abstract List<T> find(String text);

	@Override
	public IComponent getComponent() {
		return table;
	}
	
	private class TableClickListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			TablePage.this.clicked(table.getSelectedObject());
		}
	}
	
	@Override
	public void refresh() {
		try {
			List<T> result = find(text);
			table.setObjects(result);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
