package org.minimalj.frontend.impl.javafx.toolkit;

import org.minimalj.frontend.Frontend.IComponent;
import org.minimalj.frontend.action.Action;

public class FxAction extends javafx.scene.control.Hyperlink implements IComponent {

	public FxAction(Action action) {
		super(action.getName());
		if (action.getDescription() != null) {
			setTooltip(new javafx.scene.control.Tooltip(action.getDescription()));
		}
		// todo change to event thread
		setOnAction(event -> action.action());
	}
}
