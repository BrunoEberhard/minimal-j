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
			put(JsonInputComponent.VALUE, s);
		}
	}
}
