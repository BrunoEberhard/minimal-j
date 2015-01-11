package org.minimalj.frontend.lanterna.toolkit;

import java.util.ArrayList;
import java.util.List;

import org.minimalj.frontend.toolkit.FlowField;
import org.minimalj.frontend.toolkit.ClientToolkit.IComponent;

import com.googlecode.lanterna.gui.Component;
import com.googlecode.lanterna.gui.component.Panel;
import com.googlecode.lanterna.gui.layout.VerticalLayout;

public class LanternaFlowField extends Panel implements FlowField {
	private List<Component> components = new ArrayList<>();
	
	public LanternaFlowField() {
		setLayoutManager(new VerticalLayout());
	}
	
	@Override
	public void clear() {
		for (Component c : components) {
			removeComponent(c);
		}
		components.clear();
	}

	@Override
	public void add(IComponent component) {
		addComponent((Component) component);
	}

//	@Override
//	public void addAction(final Action action) {
//		final Button button = new Button((String) action.getValue(Action.NAME), new com.googlecode.lanterna.gui.Action() {
//			@Override
//			public void doAction() {
//				action.actionPerformed(new ActionEvent(LanternaFlowField.this, 0, ""));
//			}
//		});
//		components.add(0, button);
//		addComponent(button);
//	}

	@Override
	public void addGap() {
		add(new LanternaReadOnlyTextField());
	}

}
