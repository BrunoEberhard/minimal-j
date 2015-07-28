package org.minimalj.frontend.editor;

import java.util.List;

import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.Frontend.Search;
import org.minimalj.frontend.Frontend.TableActionListener;
import org.minimalj.frontend.action.Action;
import org.minimalj.frontend.page.IDialog;

public abstract class SearchDialogAction<T> extends Action implements Search<T> {
	private final Object[] keys;
	private IDialog dialog;
	
	protected SearchDialogAction(Object... keys) {
		this.keys = keys;
	}
	
	@Override
	public void action() {
		dialog = Frontend.getBrowser().showSearchDialog(this, keys, new SearchClickListener());
	}

	protected int getColumnWidthPercentage() {
		return 100;
	}

	public abstract List<T> search(String query);

	protected abstract void save(T object);
	
	private class SearchClickListener implements TableActionListener<T> {
		@Override
		public void action(T selectedObject) {
			save(selectedObject);
			dialog.closeDialog();
		}
	}
	
}
