package org.minimalj.frontend.action;

import java.util.Collections;
import java.util.function.Consumer;

import org.minimalj.frontend.editor.Result;
import org.minimalj.frontend.page.TablePage.ObjectAction;
import org.minimalj.frontend.page.TablePage.ObjectsAction;

public class ObjectActionGroup<T> extends ActionGroup {

	private T object;
	private Consumer<T> finishedListener;

	public ObjectActionGroup(T object) {
		this(null, object);
	}
	
	public ObjectActionGroup(String name, T object) {
		super(null);
		this.object = object;
	}

	public void setObject(T object) {
		this.object = object;
		for (Action action : getItems()) {
			setSelection(action, object);
		}
	}

	public void setFinishedListener(Consumer<T> finishedListener) {
		this.finishedListener = finishedListener;
		for (Action action : getItems()) {
			if (action instanceof Result) {
				((Result) action).setFinishedListener(finishedListener);
			}
		}
	}
	
	@Override
	public void add(Action item) {
		super.add(item);
		setSelection(item, object);
		if (item instanceof Result) {
			((Result) item).setFinishedListener(finishedListener);
		}
	}
	
	private void setSelection(Action action, T object) {
		if (action instanceof ObjectAction) {
			((ObjectAction) action).selectionChanged(object);
		} 
		if (action instanceof ObjectsAction) {
			if (object != null) {
				((ObjectsAction) action).selectionChanged(Collections.singletonList(object));
			} else {
				((ObjectsAction) action).selectionChanged(Collections.emptyList());
			}
		}
	}
	
	public ActionGroup addGroup(String name) {
		ActionGroup group = new ActionGroup(name);
		add(group);
		return group;
	}
	
	public ObjectActionGroup addGroup(String name, Object object) {
		ObjectActionGroup group = new ObjectActionGroup(name, object);
		add(group);
		return group;
	}

}
