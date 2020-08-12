package org.minimalj.frontend.impl.vaadin.toolkit;

import org.minimalj.frontend.Frontend.IComponent;
import org.minimalj.frontend.impl.util.HtmlString;
import org.minimalj.model.Rendering;
import org.minimalj.util.StringUtils;

import com.vaadin.shared.ui.ContentMode;
import com.vaadin.ui.Label;

public class VaadinText extends Label implements IComponent {
	private static final long serialVersionUID = 1L;

	public VaadinText(String content) {
		super(content);
	}

	public VaadinText(Rendering rendering) {
		if (rendering != null) {
			CharSequence s = rendering.render();
			if (s instanceof HtmlString) {
				setContentMode(ContentMode.HTML);
				setValue(((HtmlString) s).getHtml());
			} else if (s != null) {
				String string = s.toString();
				if (string.contains("\n")) {
					string = StringUtils.escapeHTML(string);
					string = string.replaceAll("\n", "<br>");
					setContentMode(ContentMode.HTML);
					setValue(string);
				} else {
					setContentMode(ContentMode.TEXT);
					setValue(string);
				}
			}
		}
	}
	
}
