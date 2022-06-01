package org.minimalj.frontend.editor;

import java.util.ArrayList;
import java.util.List;

import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.Frontend.IContent;
import org.minimalj.frontend.Frontend.Search;
import org.minimalj.frontend.Frontend.TableActionListener;
import org.minimalj.frontend.action.Action;
import org.minimalj.frontend.page.Page.Dialog;

public class SearchDialog<T> implements Dialog {
	private IContent content;
	private final TableActionListener<T> listener;
	private final List<Action> actions = new ArrayList<>();
	private final SaveAction saveAction;
	private final Action cancelAction;
	private List<T> selection;

	public SearchDialog(Search<T> search, Object[] keys, boolean multiSelect, TableActionListener<T> listener, List<Action> additionalActions) {
		this.listener = listener;

		this.saveAction = new SaveAction();
		this.cancelAction = new CancelAction();

		this.saveAction.setEnabled(false);

		if (additionalActions != null) {
			actions.addAll(additionalActions);
		}
		actions.add(cancelAction);
		actions.add(saveAction);
		
		content = Frontend.getInstance().createTable(search, keys, multiSelect, new SearchTableListener());
	}
	
	@Override
	public IContent getContent() {
		return content;
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
			SearchDialog.this.selection = selectedObjects;
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
			Frontend.closeDialog(SearchDialog.this);
			listener.action(selection.get(0));
		}
	}

	protected class CancelAction extends Action {
		@Override
		public void run() {
			Frontend.closeDialog(SearchDialog.this);
		}
	}
	
}
