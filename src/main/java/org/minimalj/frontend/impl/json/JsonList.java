package org.minimalj.frontend.impl.json;

import java.util.ArrayList;
import java.util.List;

import org.minimalj.frontend.Frontend.IComponent;
import org.minimalj.frontend.Frontend.IList;
import org.minimalj.frontend.action.Action;

public class JsonList extends JsonComponent implements IList {

	public JsonList(Action... actions) {
		super("List");
		if (actions != null && actions.length > 0) {
			List<JsonAction> actionLabels = new ArrayList<>();
			for (Action action : actions) {
				actionLabels.add(new JsonAction(action));
			}
			put("actions", actionLabels);
		}
	}

	@Override
	public void clear() {
		JsonFrontend.getClientSession().clearContent(getId());
	}

	@Override
	public void setEnabled(boolean enabled) {
		put("enabled", enabled);
	}

	@Override
	public void add(IComponent component, Action... actions) {
		JsonFrontend.getClientSession().addContent(getId(), (JsonComponent) component);
		
		for (Action action : actions) {
			JsonFrontend.getClientSession().addContent(getId(), new JsonAction(action));
		}
	}

	public void addComponent(JsonComponent c) {
		JsonFrontend.getClientSession().addContent(getId(), c);
	}
}
