package org.minimalj.frontend.impl.swing.toolkit;

import java.util.Locale;

import javax.swing.JLabel;

import org.minimalj.frontend.Frontend.IComponent;
import org.minimalj.model.Rendering;
import org.minimalj.model.Rendering.RenderType;

public class SwingLabel extends JLabel implements IComponent {
	private static final long serialVersionUID = 1L;

	public SwingLabel(String string) {
		super(string);
	}

	public SwingLabel(Object object) {
		if (object instanceof Rendering) {
			Rendering rendering = (Rendering) object;
			RenderType renderType = rendering.getPreferredRenderType(RenderType.HMTL, RenderType.PLAIN_TEXT);
			String s = rendering.render(renderType, Locale.getDefault());
			if (renderType == RenderType.HMTL) {
				setText("<html><body>" + s + "</body></html>");
			} else {
				setText(s);
			}
		} else if (object != null) {
			setText(object.toString());
		}
	}
}
