package org.minimalj.frontend.impl.json;

import org.minimalj.model.Rendering;
import org.minimalj.model.Rendering.RenderType;
import org.minimalj.util.StringUtils;

public class JsonText extends JsonComponent {

	public JsonText(Object object) {
		super("Text");
		if (object != null) {
			String string = object.toString();
			if (StringUtils.isHtml(string)) {
				string = StringUtils.sanitizeHtml(string);
				put("htmlValue", string);
			} else {
				put(JsonInputComponent.VALUE, string);
			}
		}
	}

	public JsonText(Rendering rendering) {
		super("Text");
		if (rendering != null) {
			RenderType renderType = rendering.getPreferredRenderType(RenderType.HMTL, RenderType.PLAIN_TEXT);
			String string = rendering.render(renderType);
			string = StringUtils.sanitizeHtml(string);
			put(renderType == RenderType.HMTL ? "htmlValue" : JsonInputComponent.VALUE, string);
		}
	}
}
