package org.minimalj.frontend.impl.json;

import java.util.ArrayList;
import java.util.List;

import org.minimalj.frontend.Frontend.FormContent;
import org.minimalj.frontend.Frontend.IContent;
import org.minimalj.frontend.action.Action;

public class JsonCustomFilter extends JsonComponent implements IContent {

	public JsonCustomFilter(FormContent filter, Object table, Action search, Action reset) {
		super("CustomFilter");

		put("filter", filter);
		if (search != null) {
			List<JsonAction> jsonActions = new ArrayList<>();
			JsonAction jsonSearchAction = new JsonAction(search);
			put("searchAction", jsonSearchAction);
			jsonActions.add(jsonSearchAction);
			if (reset != null) {
				jsonActions.add(new JsonAction(reset));
			}
			put("overviewActions", jsonActions);
		}

		// With a custom filter the auto filter of the table should be disabled
		if (table instanceof JsonTable) {
			((JsonTable<?>) table).setFilterVisible(false);
		}
		put("table", table);
	}
}
