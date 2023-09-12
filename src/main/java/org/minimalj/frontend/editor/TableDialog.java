package org.minimalj.frontend.editor;

import java.util.ArrayList;
import java.util.List;

import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.Frontend.IContent;
import org.minimalj.frontend.Frontend.ITable;
import org.minimalj.frontend.Frontend.Search;
import org.minimalj.frontend.Frontend.TableActionListener;
import org.minimalj.frontend.action.Action;
import org.minimalj.frontend.page.Page.Dialog;

public class TableDialog<T> implements Dialog {
	protected ITable<T> table;
	protected final TableActionListener<T> listener;
	private final List<Action> actions = new ArrayList<>();
	private final SaveAction saveAction;
	private final Action cancelAction;
	protected List<T> selection;
	private final String title;
	
	public TableDialog(Search<T> search, String title, Object[] keys, boolean multiSelect, TableActionListener<T> listener, List<Action> additionalActions) {
		this.listener = listener;
		this.title = title;
		
		this.saveAction = new SaveAction();
		this.cancelAction = new CancelAction();

		this.saveAction.setEnabled(false);

		if (additionalActions != null) {
			actions.addAll(additionalActions);
		}
		actions.add(cancelAction);
		actions.add(saveAction);

		table = Frontend.getInstance().createTable(keys, multiSelect, new SearchTableListener());
		initialSearch(search);
	}
	
	protected void initialSearch(Search<T> search) {
		table.setObjects(search.search(null));
	}
	
	@Override
	public String getTitle() {
		return title;
	}
	
	@Override
	public IContent getContent() {
		return table;
	}
	
	@Override
	public int getWidth() {
		return 1000;
	}
	
	@Override
	public int getHeight() {
		return 600;
	}
	
	@Override
	public List<Action> getActions() {
		return actions;
	}
	
	@Override
	public Action getSaveAction() {
		return saveAction;
	}
	
	@Override
	public Action getCancelAction() {
		return cancelAction;
	}
	
	private class SearchTableListener implements TableActionListener<T> {
		@Override
		public void selectionChanged(List<T> selectedObjects) {
			TableDialog.this.selection = selectedObjects;
			saveAction.setEnabled(!selectedObjects.isEmpty());
			listener.selectionChanged(selectedObjects);
		}

		@Override
		public void action(T selectedObject) {
			if (selectedObject != null) {
				listener.action(selectedObject);
			}
		}
	}

	protected class SaveAction extends Action {
		@Override
		public void run() {
			Frontend.closeDialog(TableDialog.this);
			listener.action(selection.get(0));
		}
	}

	protected class CancelAction extends Action {
		@Override
		public void run() {
			Frontend.closeDialog(TableDialog.this);
		}
	}
	
}