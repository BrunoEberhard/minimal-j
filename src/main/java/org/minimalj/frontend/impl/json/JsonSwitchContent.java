package org.minimalj.frontend.impl.json;

import org.minimalj.frontend.Frontend.IContent;
import org.minimalj.frontend.Frontend.SwitchContent;

public class JsonSwitchContent extends JsonComponent implements SwitchContent {
	private static final long serialVersionUID = 1L;

	public JsonSwitchContent() {
		super("Switch");
	}

	@Override
	public void show(IContent content) {
		JsonFrontend.getClientSession().clearContent(getId());
		JsonFrontend.getClientSession().addContent(getId(), (JsonComponent) content);
	}

}
