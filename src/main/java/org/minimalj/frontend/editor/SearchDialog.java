package org.minimalj.frontend.editor;

import java.util.List;

import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.Frontend.IContent;
import org.minimalj.frontend.Frontend.Search;
import org.minimalj.frontend.Frontend.TableActionListener;
import org.minimalj.frontend.action.Action;
import org.minimalj.frontend.form.Form;
import org.minimalj.model.Keys;
import org.minimalj.model.annotation.Size;
import org.minimalj.util.resources.Resources;

public class SearchDialog<T> extends TableDialog<T> {
	private IContent content;
	
	public SearchDialog(Search<T> search, String title, Object[] keys, boolean multiSelect, TableActionListener<T> listener, List<Action> additionalActions) {
		super(search, title, keys, multiSelect, listener, additionalActions);

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
	public IContent getContent() {
		return content;
	}
	
}
