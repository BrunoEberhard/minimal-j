package org.minimalj.frontend.page;

import java.util.ArrayList;
import java.util.List;

import org.minimalj.frontend.edit.Editor;
import org.minimalj.frontend.edit.EditorAction;
import org.minimalj.frontend.toolkit.IAction;
import org.minimalj.frontend.toolkit.ResourceAction;

public class ActionGroup extends ResourceAction {

	private final List<IAction> items = new ArrayList<>();

	public ActionGroup(String resourceName) {
		super(resourceName);
	}

	@Override
	public void action() {
		// n/a
	}

	public void add(IAction item) {
		items.add(item);
	}

	public void add(Editor<?> editor) {
		items.add(new EditorAction(editor));
	}

	public List<IAction> getItems() {
		return items;
	}

	public void add(Class<? extends Page> clazz, String... args) {
		items.add(new PageLink(clazz, args));
	}
	
	public ActionGroup addGroup(String name) {
		ActionGroup group = new ActionGroup(name);
		add(group);
		return group;
	}

	public void addSeparator() {
		add(new Separator());
	}

}
