package org.minimalj.frontend.impl.javafx;

import java.util.List;

import org.minimalj.frontend.action.Action;

public class FxNavigationTree extends javafx.scene.control.TreeView {

	public FxNavigationTree(List<Action> actions) {
		javafx.scene.control.TreeItem root = new javafx.scene.control.TreeItem();

//		HyperLink wäre das Fx control
//		for (Action action : actions) {
//			if (action instanceof ActionGroup) {
//				add(new SwingText(action.getName()));
//				ActionGroup actionGroup = (ActionGroup) action;
//				add(new NavigationTree(actionGroup.getItems()));
//			} else {
//				add(new SwingActionText(action));
//			}
//		}
//	
	}

}
