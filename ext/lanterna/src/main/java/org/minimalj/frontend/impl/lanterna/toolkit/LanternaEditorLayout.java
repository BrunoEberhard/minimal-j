package org.minimalj.frontend.impl.lanterna.toolkit;

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
		for (final Action action : actions) {
			Button button = new Button(action.getName());
			button.addListener(b -> LanternaFrontend.run(b, () -> action.action()));
			panelActions.addComponent(button);
		}
		addComponent(panelActions, Location.BOTTOM);
	}
	
}
