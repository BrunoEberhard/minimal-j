package org.minimalj.frontend.editor;

import java.util.ArrayList;
import java.util.List;

import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.Frontend.IContent;
import org.minimalj.frontend.Frontend.ITable;
import org.minimalj.frontend.Frontend.Search;
import org.minimalj.frontend.Frontend.TableActionListener;
import org.minimalj.frontend.action.Action;
import org.minimalj.frontend.form.Form;
import org.minimalj.frontend.page.Page.Dialog;
import org.minimalj.model.Keys;
import org.minimalj.model.annotation.Size;
import org.minimalj.util.resources.Resources;

public class SearchDialog<T> implements Dialog {
	private IContent content;
	private final TableActionListener<T> listener;
	private final List<Action> actions = new ArrayList<>();
	private final SaveAction saveAction;
	private final Action cancelAction;
	private List<T> selection;
	private final String title;
	
	public SearchDialog(Search<T> search, String title, Object[] keys, boolean multiSelect, TableActionListener<T> listener, List<Action> additionalActions) {
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

		ITable<T> table = Frontend.getInstance().createTable(keys, multiSelect, new SearchTableListener());

		Form<SearchModel> form = new Form<>();
		form.setIgnoreCaption(true);
		form.line(SearchModel.$.query);
		SearchModel model = new SearchModel();
		form.setChangeListener(source -> {});
		form.setObject(model);
		
		Action searchAction = new Action(Resources.getString("SearchAction")) {
			@Override
			public void run() {
				table.setObjects(search.search(model.query));
			};
		};
		
		content = Frontend.getInstance().createFilteredTable(form.getContent(), table, searchAction, null);
	}
	
	public static class SearchModel {
		public static final SearchModel $ = Keys.of(SearchModel.class);
		
		@Size(255)
		public String query;
	}

	
	@Override
	public String getTitle() {
		return title;
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
