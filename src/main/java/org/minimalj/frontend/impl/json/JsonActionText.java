package org.minimalj.frontend.impl.json;

import java.util.ArrayList;
import java.util.List;

import org.minimalj.frontend.action.Action;
import org.minimalj.frontend.impl.util.ActionString;

public class JsonActionText extends JsonComponent {

	public JsonActionText(ActionString actionString) {
		super("inline");

		List<Object> elements = actionString.getComponents();
		List<JsonComponent> components = new ArrayList<>();
		for (Object e : elements) {
			if (e instanceof String) {
				components.add(new JsonText((String) e));
			} else if (e instanceof Action) {
				components.add(new JsonAction((Action) e));
			}
			put("components", components);
		}
	}
}
