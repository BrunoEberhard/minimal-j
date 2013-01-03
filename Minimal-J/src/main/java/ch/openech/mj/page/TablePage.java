package ch.openech.mj.page;

import java.util.List;

import ch.openech.mj.toolkit.ClientToolkit;
import ch.openech.mj.toolkit.IComponent;
import ch.openech.mj.toolkit.VisualTable;
import ch.openech.mj.toolkit.VisualTable.ClickListener;


public abstract class TablePage<T> extends Page implements RefreshablePage {

	private String text;
	private VisualTable<T> table;

	public TablePage(PageContext context, Object[] FIELDS, String text) {
		super(context);
		this.text = text;
		table = ClientToolkit.getToolkit().createVisualTable((Class<T>)Object.class, FIELDS);
		table.setClickListener(new TableClickListener());
		refresh();
	}

	protected abstract void clicked(T object);

	protected abstract List<T> find(String text);

	@Override
	public IComponent getComponent() {
		return table;
	}
	

	private class TableClickListener implements ClickListener {

		@Override
		public void clicked() {
			T selectedObject = table.getSelectedObject();
			if (selectedObject != null) {
				TablePage.this.clicked(selectedObject);
			}
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
