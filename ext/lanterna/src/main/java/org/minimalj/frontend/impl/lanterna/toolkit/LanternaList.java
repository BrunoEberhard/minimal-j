package org.minimalj.frontend.impl.lanterna.toolkit;

import java.util.List;
import java.util.function.Function;

import org.minimalj.frontend.Frontend.Input;
import org.minimalj.frontend.action.Action;
import org.minimalj.frontend.impl.lanterna.toolkit.LanternaFrontend.LanternaActionText;
import org.minimalj.model.Rendering;

import com.googlecode.lanterna.gui2.Direction;
import com.googlecode.lanterna.gui2.LinearLayout;
import com.googlecode.lanterna.gui2.Panel;

public class LanternaList<T> extends Panel implements Input<List<T>> {
	
	private final Function<T, CharSequence> renderer;
	private final Function<T, List<Action>> itemActions;
	private final Action[] listActions;
	private List<T> value;
	
	public LanternaList(Function<T, CharSequence> renderer, Function<T, List<Action>> itemActions,
			Action... listActions) {
		setLayoutManager(new LinearLayout(Direction.VERTICAL));
		this.renderer = renderer != null ? renderer : Rendering::toString;
		this.itemActions = itemActions;
		this.listActions = listActions;

		if (listActions != null) {
			// alle listActions hinzufügen
		}
	}

	@Override
	public void setValue(List<T> value) {
		this.value = value;
		if (value != null && !value.isEmpty()) {
			super.removeAllComponents();
			for (T item : value) {
				CharSequence rendered = renderer.apply(item);
				addComponent(new LanternaText(rendered.toString()));

				for (Action action : this.itemActions.apply(item)) {
					addComponent(new LanternaActionText(action));
				}
			}
		} else {
			super.removeAllComponents();
		}
		if (listActions != null) {
			for (Action action : listActions) {
				addComponent(new LanternaActionText(action));
			}
		}
	}

	@Override
	public List<T> getValue() {
		return value;
	}

	@Override
	public void setEditable(boolean editable) {
		// TODO Auto-generated method stub
	}
	
}
