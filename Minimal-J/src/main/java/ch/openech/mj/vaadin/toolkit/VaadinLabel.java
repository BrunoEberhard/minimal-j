package ch.openech.mj.vaadin.toolkit;

import ch.openech.mj.toolkit.IComponent;

import com.vaadin.ui.Label;

public class VaadinLabel extends Label implements IComponent {

	public VaadinLabel(String content) {
		super(content);
		setContentMode(Label.CONTENT_XHTML);
	}

}
