package org.minimalj.frontend.impl.json;

import org.minimalj.frontend.impl.util.HtmlString;
import org.minimalj.model.Rendering;
import org.minimalj.util.StringUtils;

public class JsonText extends JsonComponent {

	public JsonText(String value) {
		super("Text");
		if (value != null) {
			put(JsonInputComponent.VALUE, value);
		}
	}

	public JsonText(Rendering rendering) {
		super("Text");
		if (rendering != null) {
			CharSequence c = rendering.render();
			if (c instanceof HtmlString) {
				put("htmlValue", ((HtmlString) c).getHtml());
			} else if (c != null) {
				String string = c.toString();
				if (string.contains("\n")) {
					string = StringUtils.escapeHTML(string);
					string = string.replaceAll("\n", "<br>");
					put("htmlValue", string);
				} else {
					put(JsonInputComponent.VALUE, Rendering.toString(c));
				}
			}
		}
	}
}
