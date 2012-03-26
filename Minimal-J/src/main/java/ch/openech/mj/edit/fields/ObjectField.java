package ch.openech.mj.edit.fields;

import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.AbstractAction;

import ch.openech.mj.db.EmptyObjects;
import ch.openech.mj.edit.Editor;
import ch.openech.mj.edit.form.FormVisual;
import ch.openech.mj.edit.validation.Indicator;
import ch.openech.mj.edit.validation.ValidationMessage;

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
public abstract class ObjectField<T> extends AbstractEditField<T> implements Indicator {
	// private static final Logger logger = Logger.getLogger(ObjectField.class.getName());
	
	private T object;
	
	public ObjectField(Object key) {
		this(key, true);
	}
	
	public ObjectField(Object key, boolean editable) {
		super(key, editable);
	}

	public class ObjectFieldEditor extends Editor<T> {

		@Override
		public FormVisual<T> createForm() {
			return ObjectField.this.createFormPanel();
		}

		@SuppressWarnings("unchecked")
		@Override
		public T load() {
			if (object != null) {
				return object;
			} else {
				Class<?> clazz = ch.openech.mj.util.GenericUtils.getGenericClass(ObjectField.this.getClass());
				if (clazz == null) {
					throw new RuntimeException("TODO");
				}
				try {
					return (T) clazz.newInstance();
				} catch (InstantiationException e) {
					throw new RuntimeException(e);
				} catch (IllegalAccessException e) {
					throw new RuntimeException(e);
				}
			}
		}

		@Override
		public boolean save(T object) {
			setObject(object);
			return true;
		}

		@Override
		public void validate(T object, List<ValidationMessage> resultList) {
			// may be overwritten
		}
	}

	public abstract class ObjectFieldPartEditor<P> extends Editor<P> {

		@Override
		public P load() {
			return getPart(ObjectField.this.getObject());
		}

		@Override
		public boolean save(P part) {
			setPart(ObjectField.this.getObject(), part);
			fireObjectChange();
			return true;
		}

		@Override
		public void validate(P object, List<ValidationMessage> resultList) {
			// may be overwritten
		}

		protected abstract P getPart(T object);

		protected abstract void setPart(T object, P p);
		
	}

	protected abstract FormVisual<T> createFormPanel();
	
	// why public
	public class RemoveObjectAction extends AbstractAction {
		@Override
		public void actionPerformed(ActionEvent e) {
			ObjectField.this.setObject(null);
		}
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
		display(object);
		super.fireChange();
	}

	@Override
	public boolean isEmpty() {
		Object object = getObject();
		return object == null || EmptyObjects.isEmpty(object);
	}
	
	protected abstract void display(T object);

}
