package org.minimalj.frontend.action;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.minimalj.frontend.editor.Editor;
import org.minimalj.frontend.page.Page;
import org.minimalj.frontend.page.PageAction;

public class ActionGroup extends Action {

	private final List<Action> items = new ArrayList<>();

	public ActionGroup(String name) {
		super(name);
	}

	@Override
	public void run() {
		// n/a
	}

	public void add(Action item) {
		items.add(item);
	}

	public final void add(Page page) {
		add((Action) new PageAction(page));
	}

	public final void add(Editor<?, ?> item) {
		add((Action) item);
	}

	public final void add(Page page, String name) {
		add((Action) new PageAction(page, name));
	}

	public final List<Action> getItems() {
		return Collections.unmodifiableList(items);
	}
	
	public ActionGroup addGroup(String name) {
		ActionGroup group = new ActionGroup(name);
		add(group);
		return group;
	}

	public final void addSeparator() {
		add(new Separator());
	}

}
