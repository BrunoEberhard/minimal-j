package org.minimalj.frontend.impl.lanterna.toolkit;

import org.minimalj.frontend.Frontend.IComponent;
import org.minimalj.model.Rendering;
import org.minimalj.util.StringUtils;

import com.googlecode.lanterna.gui2.Label;

public class LanternaText extends Label implements IComponent {

	public LanternaText(String text) {
		super(StringUtils.stripHtml(text));
	}

	public LanternaText(Rendering rendering) {
		super(Rendering.toString(rendering));
	}

	@Override
	public void setText(String text) {
		super.setText(text != null ? StringUtils.stripHtml(text) : "");
	}
	
}
