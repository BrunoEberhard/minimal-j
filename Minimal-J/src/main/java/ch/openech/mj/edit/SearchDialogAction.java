package ch.openech.mj.edit;

import java.util.List;

import ch.openech.mj.search.Item;
import ch.openech.mj.search.Search;
import ch.openech.mj.toolkit.ClientToolkit;
import ch.openech.mj.toolkit.IComponent;
import ch.openech.mj.toolkit.IDialog;
import ch.openech.mj.toolkit.ITable.TableActionListener;
import ch.openech.mj.toolkit.ResourceAction;

public abstract class SearchDialogAction<T> extends ResourceAction {
	private final IComponent source;
	private final Search<T> search;
	private IDialog dialog;
	
	protected SearchDialogAction(IComponent source, Search<T> search) {
		this.source = source;
		this.search = search;
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
		dialog = ClientToolkit.getToolkit().createSearchDialog(source, search, new SearchClickListener());
		dialog.openDialog();
	}
	
	protected abstract void save(T object);
	
	private class SearchClickListener implements TableActionListener {
		@Override
		public void action(Item selectedItem, List<Item> selectedItems) {
			T selectedObject = search.lookup(selectedItem);
			save(selectedObject);
			dialog.closeDialog();
		}
	}
	
}
