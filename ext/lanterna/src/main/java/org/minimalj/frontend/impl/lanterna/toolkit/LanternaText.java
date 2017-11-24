package org.minimalj.frontend.impl.lanterna.toolkit;

import org.minimalj.frontend.Frontend.IComponent;
import org.minimalj.model.Rendering;
import org.minimalj.model.Rendering.RenderType;

import com.googlecode.lanterna.gui2.Label;

public class LanternaText extends Label implements IComponent {

	public LanternaText(String text) {
		super(text);
	}

	public LanternaText(Rendering rendering) {
		super(rendering != null ? rendering.render(RenderType.PLAIN_TEXT) : "");
	}

	@Override
	public void setText(String text) {
		super.setText(text != null ? text : "");
	}

}
