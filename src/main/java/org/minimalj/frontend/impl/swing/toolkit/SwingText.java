package org.minimalj.frontend.impl.swing.toolkit;

import javax.swing.JLabel;

import org.minimalj.frontend.Frontend.IComponent;
import org.minimalj.model.Rendering;
import org.minimalj.model.Rendering.RenderType;

public class SwingText extends JLabel implements IComponent {
	private static final long serialVersionUID = 1L;

	public SwingText(String string) {
		super(string);
	}

	public SwingText(Rendering rendering) {
		if (rendering != null) {
			RenderType renderType = rendering.getPreferredRenderType(RenderType.HMTL, RenderType.PLAIN_TEXT);
			String s = rendering.render(renderType);
			if (renderType == RenderType.HMTL) {
				setText("<html><body>" + s + "</body></html>");
			} else {
				setText(s);
			}
		}
	}
}
