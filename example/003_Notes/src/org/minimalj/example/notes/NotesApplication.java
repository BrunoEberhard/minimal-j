package org.minimalj.example.notes;

import java.util.List;

import org.minimalj.application.Application;
import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.action.Action;
import org.minimalj.frontend.action.ActionGroup;

public class NotesApplication extends Application {

	@Override
	public List<Action> getNavigation() {
		ActionGroup menu = new ActionGroup(null);
		menu.add(new NewNoteEditor());
		return menu.getItems();
	}

	@Override
	public void init() {
		Frontend.show(new NoteTablePage());
	}
	
	@Override
	public Class<?>[] getEntityClasses() {
		return new Class[]{Note.class};
	}

}
