package org.minimalj.frontend.impl.vaadin.toolkit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import org.minimalj.frontend.Frontend.Input;
import org.minimalj.frontend.action.Action;
import org.minimalj.frontend.impl.vaadin.Vaadin;
import org.minimalj.model.Rendering;

import com.vaadin.shared.ui.ContentMode;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

public class VaadinList<T> extends VerticalLayout implements Input<List<T>> {
	private static final long serialVersionUID = 1L;

	private final Function<T, CharSequence> renderer;
	private final Function<T, List<Action>> itemActions;
	private final Action[] listActions;
	private boolean empty = true;
	private List<T> value;
	
	public VaadinList(Function<T, CharSequence> renderer, Function<T, List<Action>> itemActions,
			Action... listActions) {
		this.renderer = renderer != null ? renderer : Rendering::toString;
		this.itemActions = itemActions;
		this.listActions = listActions;

		setMargin(false);
		setSpacing(false);
		addStyleName("whiteBackground");

		addEmptyItem();
	}
	
	@Override
	public void setValue(List<T> value) {
		this.value = value;
		if (value != null && !value.isEmpty()) {
			super.removeAllComponents();
			empty = false;
			for (T item : value) {
				CharSequence rendered = renderer.apply(item);
				VaadinText text = new VaadinText(rendered.toString());

				List<Action> actions = new ArrayList<>();
				if (this.itemActions != null) {
					actions.addAll(this.itemActions.apply(item));
				}
				if (listActions != null) {
					Arrays.stream(listActions).forEach(actions::add);
				}
				Vaadin.createMenu(text, actions);

				addComponent(text);
			}
		} else if (!empty) {
			super.removeAllComponents();
			addEmptyItem();
			empty = true;
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

	private void addEmptyItem() {
		Label text = new Label("&nbsp;", ContentMode.HTML);
		text.setWidth("100%");
		if (listActions != null && listActions.length > 0) {
			Vaadin.createMenu(text, Arrays.asList(listActions));
		}
		addComponent(text);
	}
	
}
