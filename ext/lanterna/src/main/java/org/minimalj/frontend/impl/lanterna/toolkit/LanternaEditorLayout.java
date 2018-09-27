package org.minimalj.frontend.impl.lanterna.toolkit;

import java.util.Arrays;

import org.minimalj.frontend.Frontend.IComponent;
import org.minimalj.frontend.Frontend.IContent;
import org.minimalj.frontend.action.Action;

import com.googlecode.lanterna.gui2.BorderLayout;
import com.googlecode.lanterna.gui2.BorderLayout.Location;
import com.googlecode.lanterna.gui2.Button;
import com.googlecode.lanterna.gui2.Component;
import com.googlecode.lanterna.gui2.Direction;
import com.googlecode.lanterna.gui2.LinearLayout;
import com.googlecode.lanterna.gui2.Panel;

public class LanternaEditorLayout extends Panel implements IComponent {
	
	public LanternaEditorLayout(IContent content, Action[] actions) {
		setLayoutManager(new BorderLayout());

		addComponent((Component) content, Location.CENTER);
		
		Panel panelActions = new Panel(new LinearLayout(Direction.HORIZONTAL));
		Button[] buttons = LanternaFrontend.adaptActions(actions);
		Arrays.stream(buttons).forEach(panelActions::addComponent);
		addComponent(panelActions, Location.BOTTOM);
	}
	
}
