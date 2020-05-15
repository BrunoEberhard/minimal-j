package org.minimalj.frontend.impl.vaadin.toolkit;

import org.minimalj.frontend.Frontend.Input;
import org.minimalj.util.StringUtils;

import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

public class VaadinReadOnlyTextField extends VerticalLayout implements Input<String> {
	private static final long serialVersionUID = 1L;
	private Label label;
	
	public VaadinReadOnlyTextField() {
		setMargin(false);
		setSpacing(false);
		setWidth("100%");
	}

	@Override
	public void setValue(String text) {
		removeAll();
		if (!StringUtils.isEmpty(text)) {
			for (String line : text.split("\n")) {
				Label label = new Label(line);
				label.getElement().getClassList().add("v-html-readonly");
				add(label);
			}
		} else {
			Label label = new Label("");
			label.getElement().getClassList().add("v-html-readonly");
			add(label);
		}
	}

	@Override
	public String getValue() {
		// not possible
		return label != null ? (String) label.getText() : null;
	}

	@Override
	public void setEditable(boolean editable) {
		// read only field cannot be enabled
	}
}
