package org.minimalj.frontend.impl.vaadin.toolkit;

import org.minimalj.frontend.Frontend.IComponent;
import org.minimalj.model.Rendering;

import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;


public class VaadinText extends HorizontalLayout implements IComponent {
	private static final long serialVersionUID = 1L;

	private static final Label label = new Label();
	
	public VaadinText(String content) {
		label.setText(content);
		label.setSizeFull();
		add(label);
	}

	public VaadinText(Rendering rendering) {
		if (rendering != null) {
			CharSequence s = rendering.render();
//			if (s instanceof HtmlString) {
//				setContentMode(ContentMode.HTML);
//				setValue(((HtmlString) s).getHtml());
//			} else if (s != null) {
//				String string = s.toString();
//				if (string.contains("\n")) {
//					string = StringUtils.escapeHTML(string);
//					string = string.replaceAll("\n", "<br>");
//					setContentMode(ContentMode.HTML);
//					setValue(string);
//				} else {
//					setContentMode(ContentMode.TEXT);
//					setValue(string);
//				}
//			}
			label.setText(s.toString());
		}
	}
	
}
