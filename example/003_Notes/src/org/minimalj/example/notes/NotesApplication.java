package org.minimalj.example.notes;

import java.util.List;

import org.minimalj.application.Application;
import org.minimalj.frontend.action.Action;
import org.minimalj.frontend.action.ActionGroup;
import org.minimalj.frontend.page.Page;

public class NotesApplication extends Application {

	@Override
	public List<Action> getNavigation() {
		ActionGroup menu = new ActionGroup(null);
		menu.add(new NewNoteEditor());
		return menu.getItems();
	}

	@Override
	public Page createDefaultPage() {
		return new NoteTablePage();
	}
	
	@Override
	public Class<?>[] getEntityClasses() {
		return new Class[]{Note.class};
	}

}
