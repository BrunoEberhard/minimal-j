package org.minimalj.frontend.editor;

import java.util.ArrayList;
import java.util.List;

import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.Frontend.IContent;
import org.minimalj.frontend.Frontend.Search;
import org.minimalj.frontend.Frontend.TableActionListener;
import org.minimalj.frontend.action.Action;
import org.minimalj.frontend.page.IDialog;
import org.minimalj.util.resources.Resources;

public class SearchDialog<T> {
	private final Object[] keys;
	private final Search<T> search;
	private final boolean multiSelect;
	private final TableActionListener<T> listener;
	private final SaveAction saveAction;
	private final Action closeAction;
	private final Action[] actions;
	private List<T> selection;

	private IDialog dialog;
	
	public SearchDialog(Search<T> search, Object[] keys, boolean multiSelect, TableActionListener<T> listener, List<Action> additionalActions) {
		this.search = search;
		this.keys = keys;
		this.multiSelect = multiSelect;
		this.listener = listener;

		this.saveAction = new SaveAction();
		this.closeAction = new CancelAction();

		this.saveAction.setEnabled(false);

		List<Action> actionList = new ArrayList<>();
		if (additionalActions != null) {
			actionList.addAll(additionalActions);
		}
		actionList.add(closeAction);
		actionList.add(saveAction);
		this.actions = actionList.toArray(new Action[actionList.size()]);
	}
	
	public void show() {
		IContent content = Frontend.getInstance().createTable(search, keys, multiSelect, new SearchTableListener());

		dialog = Frontend.showDialog(Resources.getString("SearchAction"), content, saveAction, closeAction, actions);
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
			dialog.closeDialog();
			listener.action(selection.get(0));
		}
	}

	protected class CancelAction extends Action {
		@Override
		public void run() {
			dialog.closeDialog();
		}
	}

	public void closeDialog() {
		dialog.closeDialog();
	}
	
}
