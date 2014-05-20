package org.minimalj.frontend.edit;

import java.util.List;

import org.minimalj.frontend.toolkit.ClientToolkit;
import org.minimalj.frontend.toolkit.IComponent;
import org.minimalj.frontend.toolkit.IDialog;
import org.minimalj.frontend.toolkit.ResourceAction;
import org.minimalj.frontend.toolkit.ClientToolkit.Search;
import org.minimalj.frontend.toolkit.ITable.TableActionListener;

public abstract class SearchDialogAction<T> extends ResourceAction implements Search<T> {
	private final IComponent source;
	private final Object[] keys;
	private IDialog dialog;
	
	protected SearchDialogAction(IComponent source, Object... keys) {
		this.source = source;
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
		dialog = ClientToolkit.getToolkit().createSearchDialog(source, this, keys, new SearchClickListener());
		dialog.openDialog();
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
