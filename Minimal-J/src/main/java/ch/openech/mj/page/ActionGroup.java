package ch.openech.mj.page;

import java.util.ArrayList;
import java.util.List;

import ch.openech.mj.edit.Editor;
import ch.openech.mj.resources.Resources;
import ch.openech.mj.toolkit.IAction;
import ch.openech.mj.toolkit.IComponent;

public class ActionGroup implements IAction {

	private final String name;
	private final List<IAction> items = new ArrayList<>();

	public ActionGroup(String name) {
		this.name = name;
	}

	@Override
	public String getName() {
		return name;
	}
	
	public boolean isEnabled() {
		return true;
	}
	
	@Override
	public void action(IComponent pageContext) {
		// n/a
	}

	public void add(IAction item) {
		items.add(item);
	}

	public void add(Editor<?> editor) {
		items.add(new EditorPageAction(editor));
	}

	public List<IAction> getItems() {
		return items;
	}

	public void add(Class<? extends Page> clazz, String... args) {
		StringBuilder link = new StringBuilder();
		link.append(clazz.getSimpleName());
		for (int i = 0; i<args.length; i++) {
			link.append("/"); link.append(args[i]);
		}
		String name = Resources.getResourceBundle().getString(clazz.getSimpleName() + ".text");
		
		items.add(new PageLink(name, link.toString()));
	}
	
	public ActionGroup addGroup(String name) {
		ActionGroup group = new ActionGroup(name);
		add(group);
		return group;
	}

	public void addSeparator() {
		add(new Separator());
	}

	@Override
	public String getDescription() {
		return null;
	}

	@Override
	public void setChangeListener(ActionChangeListener changeListener) {
		// n/a
	}
	
}
