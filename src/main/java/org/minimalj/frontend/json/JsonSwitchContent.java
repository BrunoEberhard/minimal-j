package org.minimalj.frontend.json;

import org.minimalj.frontend.toolkit.ClientToolkit.IContent;
import org.minimalj.frontend.toolkit.ClientToolkit.SwitchContent;

public class JsonSwitchContent extends JsonComponent implements SwitchContent {
	private static final long serialVersionUID = 1L;

	public JsonSwitchContent() {
		super("Switch");
	}

	@Override
	public void show(IContent content) {
		JsonClientToolkit.getSession().switchContent(getId(), (JsonComponent) content);
	}

}
