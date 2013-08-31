package ch.openech.mj.edit;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import ch.openech.mj.toolkit.ClientToolkit;
import ch.openech.mj.toolkit.GridFormLayout;
import ch.openech.mj.toolkit.IComponent;
import ch.openech.mj.toolkit.IDialog;
import ch.openech.mj.toolkit.IDialog.CloseListener;
import ch.openech.mj.toolkit.ITable;
import ch.openech.mj.toolkit.ResourceAction;
import ch.openech.mj.toolkit.TextField;
import ch.openech.mj.util.GenericUtils;

public abstract class SearchDialogAction<T> extends ResourceAction {
	private final IComponent source;
	private final Object[] keys;
	private IDialog dialog;
	private ITable<T> table;
	private TextField textFieldSearch;
	
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


	protected void run(IComponent source) {
		
	}
	
	private void showPageOn(IComponent source) {
		textFieldSearch = ClientToolkit.getToolkit().createTextField(new SearchChangeListener(), 100);
		textFieldSearch.setCommitListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				search(textFieldSearch.getText());
			}
		});
		
		@SuppressWarnings("unchecked")
		Class<T> clazz = (Class<T>) GenericUtils.getGenericClass(getClass());		
		table = ClientToolkit.getToolkit().createTable(clazz, keys);
		table.setClickListener(new SearchClickListener());
		
		GridFormLayout layout = ClientToolkit.getToolkit().createGridLayout(1, 100);
		
		layout.add(textFieldSearch, 1);
		layout.add(table, 1);
		
		dialog = ClientToolkit.getToolkit().createDialog(source, "Suche", layout);
		
		dialog.setCloseListener(new CloseListener() {
			@Override
			public boolean close() {
				return true;
			}
		});
		
		dialog.openDialog();
//		ClientToolkit.getToolkit().focusFirstComponent(textFieldSearch);
	}
	
	protected abstract List<T> search(String text);
	
	protected abstract void save(T object);
	
	private class SearchChangeListener implements ChangeListener {

		@Override
		public void stateChanged(ChangeEvent e) {
			// At the moment no continous - search
		}
		
	}
	
//	private class SearchAction extends ResourceAction {
//		
//		@Override
//		public void action(IComponent context) {
//			List<T> objects = search(textFieldSearch.getText());
//			table.setObjects(objects);
//		}
//	}

//	private class OkAction extends ResourceAction {
//		
//		@Override
//		public void action(IComponent context) {
//			saveAndClose();
//		}
//	}
	
	private class SearchClickListener implements ActionListener {
		
		@Override
		public void actionPerformed(ActionEvent e) {
			saveAndClose();
		}
	}
	
	private void saveAndClose() {
		T selected = table.getSelectedObject();
		save(selected);
		dialog.closeDialog();
	}
	
}
