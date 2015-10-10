package org.minimalj.frontend.impl.lanterna.toolkit;

import org.minimalj.frontend.Frontend.IComponent;
import org.minimalj.model.Rendering;
import org.minimalj.model.Rendering.RenderType;

import com.googlecode.lanterna.gui.component.Label;

public class LanternaText extends Label implements IComponent {

	public LanternaText(String text) {
		super(text);
	}

	public LanternaText(Rendering rendering) {
		if (rendering != null) {
			String s = rendering.render(RenderType.PLAIN_TEXT);
			setText(s);
		}
	}

	@Override
	public void setText(String text) {
		super.setText(text != null ? text : "");
	}

}
