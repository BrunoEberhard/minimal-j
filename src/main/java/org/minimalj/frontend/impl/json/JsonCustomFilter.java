package org.minimalj.frontend.impl.json;

import java.util.ArrayList;
import java.util.List;

import org.minimalj.frontend.Frontend.FormContent;
import org.minimalj.frontend.Frontend.IContent;
import org.minimalj.frontend.Frontend.ITable;
import org.minimalj.frontend.action.Action;

public class JsonCustomFilter extends JsonComponent implements IContent {

	public JsonCustomFilter(FormContent filter, ITable<?> table, Action... actions) {
		super("CustomFilter");

		put("filter", filter);
		if (actions != null && actions.length > 0) {
			List<JsonAction> jsonActions = new ArrayList<>();
			for (Action action : actions) {
				jsonActions.add(new JsonAction(action));
			}
			((JsonTable<?>) table).put("overviewActions", jsonActions);
		}

		put("table", table);
	}
}
