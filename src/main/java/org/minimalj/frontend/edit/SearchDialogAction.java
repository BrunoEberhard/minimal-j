package org.minimalj.frontend.edit;

import java.util.List;

import org.minimalj.frontend.toolkit.Action;
import org.minimalj.frontend.toolkit.ClientToolkit;
import org.minimalj.frontend.toolkit.ClientToolkit.Search;
import org.minimalj.frontend.toolkit.ClientToolkit.TableActionListener;
import org.minimalj.frontend.toolkit.IDialog;

public abstract class SearchDialogAction<T> extends Action implements Search<T> {
	private final Object[] keys;
	private IDialog dialog;
	
	protected SearchDialogAction(Object... keys) {
		this.keys = keys;
	}
	
	@Override
	public void action() {
		dialog = ClientToolkit.getToolkit().showSearchDialog(this, keys, new SearchClickListener());
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
