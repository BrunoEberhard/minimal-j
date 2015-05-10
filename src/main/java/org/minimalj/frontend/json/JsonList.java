package org.minimalj.frontend.json;

import java.util.ArrayList;
import java.util.List;

import org.minimalj.frontend.toolkit.Action;
import org.minimalj.frontend.toolkit.ClientToolkit.IComponent;
import org.minimalj.frontend.toolkit.IList;

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
		JsonClientToolkit.getSession().clearContent(getId());
	}

	@Override
	public void setEnabled(boolean enabled) {
		put("editable", enabled);
	}

	@Override
	public void add(Object object, Action... actions) {
		IComponent label = (object instanceof Action) ? new JsonAction((Action) object) : new JsonLabel(object);
		JsonClientToolkit.getSession().addContent(getId(), (JsonComponent) label);
		
		for (Action action : actions) {
			JsonClientToolkit.getSession().addContent(getId(), new JsonAction(action));
		}
	}

}
