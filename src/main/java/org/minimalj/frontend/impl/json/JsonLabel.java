package org.minimalj.frontend.impl.json;

import org.minimalj.model.Rendering;
import org.minimalj.model.Rendering.RenderType;

public class JsonLabel extends JsonComponent {
	
	public JsonLabel(Object object) {
		super("Label");
		if (object instanceof Rendering) {
			Rendering rendering = (Rendering) object;
			RenderType renderType = rendering.getPreferredRenderType(RenderType.HMTL, RenderType.PLAIN_TEXT);
			String s = rendering.render(renderType);
			put(JsonInputComponent.VALUE, s);
		} else if (object != null) {
			put(JsonInputComponent.VALUE, object.toString());
		}
	}
}
