package ch.openech.mj.edit;

import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import ch.openech.mj.page.PageContext;
import ch.openech.mj.page.PageContextHelper;
import ch.openech.mj.resources.ResourceAction;
import ch.openech.mj.resources.ResourceHelper;
import ch.openech.mj.resources.Resources;
import ch.openech.mj.toolkit.ClientToolkit;
import ch.openech.mj.toolkit.IComponent;
import ch.openech.mj.toolkit.TextField;
import ch.openech.mj.toolkit.VisualDialog;
import ch.openech.mj.toolkit.VisualDialog.CloseListener;
import ch.openech.mj.toolkit.VisualTable;
import ch.openech.mj.toolkit.VisualTable.ClickListener;
import ch.openech.mj.util.GenericUtils;

public abstract class SearchDialogAction<T> extends AbstractAction {
	private final Object[] keys;
	private VisualDialog dialog;
	private VisualTable<T> table;
	private TextField textFieldSearch;
	
	public SearchDialogAction(Object... keys) {
		this.keys = keys;
		String actionName = getClass().getSimpleName();
		ResourceHelper.initProperties(this, Resources.getResourceBundle(), actionName);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		try {
			PageContext context = PageContextHelper.findContext(e.getSource());
			showPageOn(context);
		} catch (Exception x) {
			// TODO show dialog
			x.printStackTrace();
		}
	}
	
	private void showPageOn(PageContext context) {
		textFieldSearch = ClientToolkit.getToolkit().createTextField(new SearchChangeListener(), 100);
		
		@SuppressWarnings("unchecked")
		Class<T> clazz = (Class<T>) GenericUtils.getGenericClass(getClass());		
		table = ClientToolkit.getToolkit().createVisualTable(clazz, keys);
				
		IComponent layout = ClientToolkit.getToolkit().createSearchLayout(textFieldSearch, new SearchAction(), table, new OkAction());
		
		dialog = ClientToolkit.getToolkit().openDialog(context, layout, "Suche");
		dialog.setResizable(true);
		
		dialog.setCloseListener(new CloseListener() {
			@Override
			public boolean close() {
				return true;
			}
		});
		
		table.setClickListener(new SearchClickListener());
		dialog.openDialog();
		ClientToolkit.getToolkit().focusFirstComponent(textFieldSearch);
	}
	
	protected abstract List<T> search(String text);
	
	protected abstract void save(T object);
	
	private class SearchChangeListener implements ChangeListener {

		@Override
		public void stateChanged(ChangeEvent e) {
			// At the moment no continous - search
		}
		
	}
	
	private class SearchAction extends ResourceAction {
		
		@Override
		public void actionPerformed(ActionEvent e) {
			List<T> objects = search(textFieldSearch.getText());
			table.setObjects(objects);
		}
	}

	private class OkAction extends ResourceAction {
		
		@Override
		public void actionPerformed(ActionEvent e) {
			saveAndClose();
		}
	}
	
	private class SearchClickListener implements ClickListener {
		
		@Override
		public void clicked() {
			if (table.getSelectedObject() != null) {
				saveAndClose();
			}
		}
	}
	
	private void saveAndClose() {
		T selected = table.getSelectedObject();
		save(selected);
		dialog.closeDialog();
	}
	
}
