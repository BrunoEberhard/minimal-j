package org.minimalj.frontend.impl.json;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import org.minimalj.frontend.Frontend.Input;
import org.minimalj.frontend.action.Action;
import org.minimalj.model.Rendering;

public class JsonList<T> extends JsonComponent implements Input<List<T>> {

	private final Function<T, CharSequence> renderer;
	private final Function<T, List<Action>> itemActions;
	private final Action[] listActions;
	private boolean empty = true;
	private List<T> value;

	public JsonList(Function<T, CharSequence> renderer, Function<T, List<Action>> itemActions, Action... listActions) {
		super("List");
		this.renderer = renderer != null ? renderer : Rendering::toString;
		this.itemActions = itemActions;
		this.listActions = listActions;
		addEmptyItem();
	}

	@Override
	public void setEditable(boolean enabled) {
		// TODO
		put("enabled", enabled);
	}

	@Override
	public void setValue(List<T> value) {
		this.value = value;
		if (value != null && !value.isEmpty()) {
			JsonFrontend.getClientSession().clearContent(getId());
			empty = false;
			for (T item : value) {
				List<Action> itemActions = this.itemActions != null ? this.itemActions.apply(item)
						: Collections.emptyList();
				CharSequence rendered = renderer.apply(item);
				JsonFrontend.getClientSession().addContent(getId(),
						new JsonListItem(rendered.toString(), itemActions, listActions));
			}
		} else if (!empty) {
			JsonFrontend.getClientSession().clearContent(getId());
			addEmptyItem();
			empty = true;
		}
	}

	private void addEmptyItem() {
		JsonFrontend.getClientSession().addContent(getId(),
				new JsonListItem(null, Collections.emptyList(), listActions));
	}

	@Override
	public List<T> getValue() {
		return value;
	}

}
