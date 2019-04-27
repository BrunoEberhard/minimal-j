package org.minimalj.frontend.impl.json;

import java.util.ArrayList;
import java.util.List;

import org.minimalj.frontend.action.Action;

public class JsonListItem extends JsonComponent {

	public JsonListItem(String text, List<Action> itemActions, Action[] listActions) {
		super("ListItem");
		put("text", text);

		List<JsonAction> actionLabels = new ArrayList<>();
		for (Action action : itemActions) {
			actionLabels.add(new JsonAction(action));
		}
		for (Action action : listActions) {
			actionLabels.add(new JsonAction(action));
		}
		if (!actionLabels.isEmpty()) {
			put("actions", actionLabels);
		}
	}

}
