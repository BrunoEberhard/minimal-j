package org.minimalj.frontend.action;

import java.util.ArrayList;
import java.util.List;

import org.minimalj.frontend.page.DetailPageAction;
import org.minimalj.frontend.page.Page;
import org.minimalj.frontend.page.PageAction;

public class ActionGroup extends Action {

	private final List<Action> items = new ArrayList<>();

	public ActionGroup(String resourceName) {
		super(resourceName);
	}

	@Override
	public void action() {
		// n/a
	}

	public void add(Action item) {
		items.add(item);
	}

	public void add(Page page) {
		items.add(new PageAction(page));
	}

	public void addDetail(Page page) {
		items.add(new DetailPageAction(page));
	}

	public void add(Page page, String name) {
		items.add(new PageAction(page, name));
	}

	public List<Action> getItems() {
		return items;
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
