package ch.openech.mj.edit.fields;

import ch.openech.mj.db.EmptyObjects;

/**
 * The state of an ObjectField is saved in the object variable.<p>
 * 
 * You have to implement for an ObjectField:
 * <ul>
 * <li>display: The widgets have to be updated according to the object</li>
 * <li>fireChange: The object has to be updated according the widgets</li>
 * </ul>
 *
 * @param <T>
 */
public abstract class ObjectField<T> extends AbstractEditField<T> {
	// private static final Logger logger = Logger.getLogger(ObjectField.class.getName());
	
	private T object;
	
	public ObjectField(Object key) {
		this(key, true);
	}
	
	public ObjectField(Object key, boolean editable) {
		super(key, editable);
	}

	@Override
	public T getObject() {
		return object;
	}

	@Override
	public void setObject(T object) {
		this.object = object;
		fireObjectChange();
	}
	
	protected void fireObjectChange() {
		if (object != null) {
			show(object);
		}
		if (isEditable()) {
			showActions();
		}
		super.fireChange();
	}

	@Override
	public boolean isEmpty() {
		Object object = getObject();
		return object == null || EmptyObjects.isEmpty(object);
	}
	
	protected abstract void show(T object);

	protected void showActions() {
		// to be overwritten
	}

}
