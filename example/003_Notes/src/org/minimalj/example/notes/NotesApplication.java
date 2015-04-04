package org.minimalj.example.notes;

import java.util.List;

import org.minimalj.application.Application;
import org.minimalj.frontend.page.ActionGroup;
import org.minimalj.frontend.toolkit.Action;

public class NotesApplication extends Application {

	@Override
	public List<Action> getActionsNew() {
		ActionGroup menu = new ActionGroup(null);
		menu.add(new NewNoteEditor());
		return menu.getItems();
	}

	@Override
	public Class<?>[] getEntityClasses() {
		return new Class[]{Note.class};
	}
	
	
}
