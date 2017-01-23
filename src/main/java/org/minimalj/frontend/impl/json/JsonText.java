package org.minimalj.frontend.impl.json;

import org.minimalj.model.Rendering;
import org.minimalj.model.Rendering.RenderType;

public class JsonText extends JsonComponent {

	public JsonText(Object object) {
		super("Text");
		if (object != null) {
			put(JsonInputComponent.VALUE, object.toString());
		}
	}

	public JsonText(Rendering rendering) {
		super("Text");
		if (rendering != null) {
			RenderType renderType = rendering.getPreferredRenderType(RenderType.HMTL, RenderType.PLAIN_TEXT);
			String s = rendering.render(renderType);
			// Security: should the rendered string be checked for attacks?
			// the value is later inserted in the html page as innerHtml
			put(renderType == RenderType.HMTL ? "htmlValue" : JsonInputComponent.VALUE, s);
		}
	}
}
