package ch.openech.mj.edit.fields;

import javax.swing.Action;

import ch.openech.mj.edit.EditorDialogAction;

public abstract class ObjectLinkField<T> extends ObjectField<T> {
	// private static final Logger logger = Logger.getLogger(ObjectField.class.getName());
	
	public ObjectLinkField(Object key) {
		this(key, true);
	}

	public ObjectLinkField(Object key, boolean editable) {
		super(key, editable);
	}

	@Override
	protected void show(T object) {
		if (isEditable()) {
			Action action = new EditorDialogAction(new ObjectFieldEditor());
			action.putValue(Action.NAME, display(object));
			visual.addAction(action);
		} else {
			visual.addObject(display(object));
		}
	}
	
	@Override
	protected final void showActions() {
		// not to be overwritten
	}

	protected abstract String display(T object);

}
