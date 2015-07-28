package org.minimalj.frontend.lanterna.toolkit;

import java.util.Locale;

import org.minimalj.frontend.Frontend.IComponent;
import org.minimalj.model.Rendering;
import org.minimalj.model.Rendering.RenderType;

import com.googlecode.lanterna.gui.component.Label;

public class LanternaLabel extends Label implements IComponent {

	public LanternaLabel(String text) {
		super(text);
	}

	public LanternaLabel(Object object) {
		if (object instanceof Rendering) {
			Rendering rendering = (Rendering) object;
			String s = rendering.render(RenderType.PLAIN_TEXT, Locale.getDefault());
			setText(s);
		} else if (object != null) {
			setText(object.toString());
		}
	}

	@Override
	public void setText(String text) {
		super.setText(text != null ? text : "");
	}
	
}
