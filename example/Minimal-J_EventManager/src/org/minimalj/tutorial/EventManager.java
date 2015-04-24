package org.minimalj.tutorial;

import java.util.ArrayList;
import java.util.List;

import org.minimalj.application.Application;
import org.minimalj.frontend.editor.EditorAction;
import org.minimalj.frontend.toolkit.Action;
import org.minimalj.tutorial.domain.Event;
import org.minimalj.tutorial.domain.Person;
import org.minimalj.tutorial.view.AddPersonEditor;

public class EventManager extends Application {

	@Override
	public Class<?>[] getEntityClasses() {
		return new Class<?>[]{Person.class, Event.class};
	}

	@Override
	public List<Action> getActionsNew() {
		List<Action> actions = new ArrayList<>();
		actions.add(new EditorAction(new AddPersonEditor()));
		return actions;
	}

}
