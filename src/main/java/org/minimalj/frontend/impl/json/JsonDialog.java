package org.minimalj.frontend.impl.json;

import java.util.Arrays;
import java.util.List;

import org.minimalj.frontend.Frontend.IContent;
import org.minimalj.frontend.Frontend.Search;
import org.minimalj.frontend.Frontend.TableActionListener;
import org.minimalj.frontend.action.Action;
import org.minimalj.frontend.page.IDialog;

public class JsonDialog extends JsonComponent implements IDialog {

	public JsonDialog(String title, IContent content, Action saveAction, Action[] actions) {
		super("Dialog");
		put("title", title);
		put("content", (content));
		
		List<Object> jsonActions = JsonFrontend.getClientSession().createActions(actions);
		put("actions", jsonActions);
		
		// if saveAction is one of the 'normal' actions the json adapter has to be reused
		// (there can be only one listener on the Minimal-J action, if there were two
		// one would not get the notifications for enable / disable)
		int saveActionIndex = Arrays.asList(actions).indexOf(saveAction);
		Object jsonSaveAction = saveActionIndex > -1 ? jsonActions.get(saveActionIndex) : JsonFrontend.getClientSession().createAction(saveAction);
		put("saveAction", jsonSaveAction);
		
		JsonFrontend.getClientSession().openDialog(this);
	}

	private JsonDialog(String type, JsonComponent content) {
		super(type);
		put("title", "Search");
		put("content", content);
		JsonFrontend.getClientSession().openDialog(this);
	}

	@Override
	public void closeDialog() {
		JsonFrontend.getClientSession().closeDialog((String) get("id"));
	}
	
	public static class JsonSearchDialog<T> extends JsonDialog {

		public JsonSearchDialog(Search<T> search, Object[] keys, TableActionListener<T> listener) {
			super("SearchDialog", new JsonSearchPanel(search, keys, listener));
		}
		
	}

}
