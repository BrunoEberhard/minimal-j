package ch.openech.mj.edit;

import java.util.List;

import ch.openech.mj.search.Search;
import ch.openech.mj.toolkit.ClientToolkit;
import ch.openech.mj.toolkit.IComponent;
import ch.openech.mj.toolkit.IDialog;
import ch.openech.mj.toolkit.ITable.TableActionListener;
import ch.openech.mj.toolkit.ResourceAction;

public abstract class SearchDialogAction<T> extends ResourceAction {
	private final IComponent source;
	private final Search<T> search;
	private final Object[] keys;
	private IDialog dialog;
	
	protected SearchDialogAction(IComponent source, Search<T> search, Object... keys) {
		this.source = source;
		this.search = search;
		this.keys = keys;
	}
	
	@Override
	public void action(IComponent context) {
		try {
			showPageOn(source);
		} catch (Exception x) {
			// TODO show dialog
			x.printStackTrace();
		}
	}

	protected int getColumnWidthPercentage() {
		return 100;
	}

	protected void run(IComponent source) {
		
	}
	
	private void showPageOn(IComponent source) {
		dialog = ClientToolkit.getToolkit().createSearchDialog(source, search, keys, new SearchClickListener());
		dialog.openDialog();
	}
	
	protected abstract void save(T object);
	
	private class SearchClickListener implements TableActionListener {
		@Override
		public void action(int selectedId, List<Integer> selectedObjects) {
			T selectedObject = search.lookup(selectedId);
			save(selectedObject);
			dialog.closeDialog();
		}
	}
	
}
