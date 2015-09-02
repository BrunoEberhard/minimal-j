package org.minimalj.frontend.impl.vaadin6.toolkit;

import java.util.Locale;

import org.minimalj.frontend.Frontend.IComponent;
import org.minimalj.model.Rendering;
import org.minimalj.model.Rendering.RenderType;

import com.vaadin.ui.Label;

public class VaadinLabel extends Label implements IComponent {
	private static final long serialVersionUID = 1L;

	public VaadinLabel(String content) {
		super(content);
		setContentMode(Label.CONTENT_XHTML);
	}

	public VaadinLabel(Object object) {
		if (object instanceof Rendering) {
			Rendering rendering = (Rendering) object;
			RenderType renderType = rendering.getPreferredRenderType(RenderType.HMTL, RenderType.PLAIN_TEXT);
			String s = rendering.render(renderType, Locale.getDefault());
			if (renderType == RenderType.HMTL) {
				setContentMode(Label.CONTENT_XHTML);
			} else {
				setContentMode(Label.CONTENT_TEXT);
			}
			setValue(s);
		} else if (object != null) {
			setValue(object.toString());
		}
	}
}
