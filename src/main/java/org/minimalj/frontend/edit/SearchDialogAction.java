package org.minimalj.frontend.edit;

import java.util.List;

import org.minimalj.frontend.toolkit.ClientToolkit;
import org.minimalj.frontend.toolkit.ClientToolkit.Search;
import org.minimalj.frontend.toolkit.IDialog;
import org.minimalj.frontend.toolkit.ITable.TableActionListener;
import org.minimalj.frontend.toolkit.ResourceAction;

public abstract class SearchDialogAction<T> extends ResourceAction implements Search<T> {
	private final Object[] keys;
	private IDialog dialog;
	
	protected SearchDialogAction(Object... keys) {
		this.keys = keys;
	}
	
	@Override
	public void action() {
		try {
			dialog = ClientToolkit.getToolkit().createSearchDialog(this, keys, new SearchClickListener());
			dialog.openDialog();
		} catch (Exception x) {
			// TODO show dialog
			x.printStackTrace();
		}
	}

	protected int getColumnWidthPercentage() {
		return 100;
	}

	public abstract List<T> search(String query);

	protected abstract void save(T object);
	
	private class SearchClickListener implements TableActionListener<T> {
		@Override
		public void action(T selectedObject, List<T> selectedObjects) {
			save(selectedObject);
			dialog.closeDialog();
		}
	}
	
}
