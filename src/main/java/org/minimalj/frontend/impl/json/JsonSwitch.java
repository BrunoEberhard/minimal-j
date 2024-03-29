package org.minimalj.frontend.impl.json;

import org.minimalj.frontend.Frontend.IComponent;
import org.minimalj.frontend.Frontend.IContent;
import org.minimalj.frontend.Frontend.SwitchComponent;
import org.minimalj.frontend.Frontend.SwitchContent;

public class JsonSwitch extends JsonComponent implements SwitchContent, SwitchComponent {

	private final JsonPageManager jsonPageManager;
	
	public JsonSwitch(JsonPageManager jsonPageManager) {
		super("Switch");
		this.jsonPageManager = jsonPageManager;
	}

	@Override
	public void show(IContent content) {
		if (content != get("component")) {
			putSilent("component", content);
			jsonPageManager.replaceContent(this, (JsonComponent) content);
		}
	}

	@Override
	public void show(IComponent component) {
		if (component != get("component")) {
			putSilent("component", component);
			jsonPageManager.replaceContent(this, (JsonComponent) component);
		}
	}
}
