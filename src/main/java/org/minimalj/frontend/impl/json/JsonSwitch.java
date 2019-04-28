package org.minimalj.frontend.impl.json;

import org.minimalj.frontend.Frontend.IComponent;
import org.minimalj.frontend.Frontend.IContent;
import org.minimalj.frontend.Frontend.SwitchComponent;
import org.minimalj.frontend.Frontend.SwitchContent;

public class JsonSwitch extends JsonComponent implements SwitchContent, SwitchComponent {

	public JsonSwitch() {
		super("Switch");
	}

	@Override
	public void show(IContent content) {
		JsonFrontend.getClientSession().clearContent(this);
		JsonFrontend.getClientSession().addContent(getId(), (JsonComponent) content);
	}

	@Override
	public void show(IComponent component) {
		JsonFrontend.getClientSession().clearContent(this);
		JsonFrontend.getClientSession().addContent(getId(), (JsonComponent) component);
	}
}
