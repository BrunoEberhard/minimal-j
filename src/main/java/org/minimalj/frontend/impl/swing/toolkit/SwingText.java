package org.minimalj.frontend.impl.swing.toolkit;

import javax.swing.JLabel;

import org.minimalj.frontend.Frontend.IComponent;
import org.minimalj.frontend.impl.util.HtmlString;
import org.minimalj.model.Rendering;
import org.minimalj.util.StringUtils;

public class SwingText extends JLabel implements IComponent {
	private static final long serialVersionUID = 1L;

	public SwingText(String string) {
		super(string);
	}

	public SwingText(Rendering rendering) {
		if (rendering != null) {
			CharSequence s = rendering.render();
			if (s instanceof HtmlString) {
				setText("<html><body>" + ((HtmlString) s).getHtml() + "</body></html>");
			} else if (s != null) {
				String string = s.toString();
				if (string.contains("\n")) {
					string = StringUtils.escapeHTML(string);
					string = string.replaceAll("\n", "<br>");
					setText("<html><body>" + string + "</body></html>");
				} else {
					setText(string);
				}
			} else {
				setText(null);
			}
		}
	}
}
