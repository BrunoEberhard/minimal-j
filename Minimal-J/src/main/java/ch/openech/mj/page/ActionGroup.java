package ch.openech.mj.page;

import java.util.ArrayList;
import java.util.List;

import ch.openech.mj.edit.Editor;
import ch.openech.mj.resources.Resources;
import ch.openech.mj.toolkit.IAction;
import ch.openech.mj.toolkit.IComponent;
import ch.openech.mj.toolkit.ResourceAction;

public class ActionGroup extends ResourceAction {

	private final List<IAction> items = new ArrayList<>();

	public ActionGroup(String resourceName) {
		super(resourceName);
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
		String name = Resources.getString(clazz.getSimpleName() + ".text");
		
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

}
