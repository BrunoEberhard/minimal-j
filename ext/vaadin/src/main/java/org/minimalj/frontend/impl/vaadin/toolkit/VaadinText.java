package org.minimalj.frontend.impl.vaadin.toolkit;

import org.minimalj.frontend.Frontend.IComponent;
import org.minimalj.model.Rendering;
import org.minimalj.model.Rendering.RenderType;

import com.vaadin.v7.shared.ui.label.ContentMode;
import com.vaadin.v7.ui.Label;

public class VaadinText extends Label implements IComponent {
	private static final long serialVersionUID = 1L;

	public VaadinText(String content) {
		super(content);
	}

	public VaadinText(Rendering rendering) {
		if (rendering != null) {
			RenderType renderType = rendering.getPreferredRenderType(RenderType.HMTL, RenderType.PLAIN_TEXT);
			String s = rendering.render(renderType);
			setValue(s);
			if (renderType == RenderType.HMTL) {
				setContentMode(ContentMode.HTML);
			} else {
				setContentMode(ContentMode.TEXT);
			}
		}
	}
	
}
