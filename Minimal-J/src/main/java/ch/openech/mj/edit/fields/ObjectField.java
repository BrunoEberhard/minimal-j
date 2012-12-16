package ch.openech.mj.edit.fields;

import java.util.List;

import ch.openech.mj.db.EmptyObjects;
import ch.openech.mj.db.model.PropertyInterface;
import ch.openech.mj.edit.Editor;
import ch.openech.mj.edit.form.IForm;
import ch.openech.mj.edit.validation.ValidationMessage;
import ch.openech.mj.edit.value.CloneHelper;
import ch.openech.mj.toolkit.ClientToolkit;
import ch.openech.mj.toolkit.FlowField;
import ch.openech.mj.toolkit.IComponent;

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
	protected final FlowField visual;
	
	public ObjectField(PropertyInterface property) {
		this(property, true);
	}
	
	public ObjectField(PropertyInterface property, boolean editable) {
		super(property, editable);
		visual = ClientToolkit.getToolkit().createFlowField();
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

	protected void validate(T changedObject, List<ValidationMessage> resultList) {
		// to be overwritten.
		
	}

	@Override
	public IComponent getComponent() {
		return visual;
	}
	
	protected abstract IForm<T> createFormPanel();

	public class ObjectFieldEditor extends Editor<T> {

		@Override
		public IForm<T> createForm() {
			return ObjectField.this.createFormPanel();
		}

		@Override
		public T load() {
			return ObjectField.this.getObject();
		}
		
		@Override
		public T newInstance() {
			// Delegate ObjectField class. Its not possible to override here
			// (because of some strange erasure thing)
			return ObjectField.this.newInstance();
		}

//		@Override
//		protected void validate(T object, List<ValidationMessage> resultList) {
//			super.validate(object, resultList);
//			ObjectField.this.validate(object, resultList);
//		}

		@Override
		public boolean save(T edited) {
			ObjectField.this.setObject(edited);
			return true;
		}
	}
	
	/*
	 * Only to be used in ObjectFieldEditor
	 */
	protected T newInstance() {
		@SuppressWarnings("unchecked")
		Class<T> clazz = (Class<T>) ch.openech.mj.util.GenericUtils.getGenericClass(ObjectField.this.getClass());
		T newInstance = CloneHelper.newInstance(clazz);
		return newInstance;
	}
	
	protected void fireObjectChange() {
		visual.clear();
		if (object != null) {
			show(object);
		}
		if (isEditable()) {
			showActions();
		}
		super.fireChange();
	}

	public boolean isEmpty() {
		Object object = getObject();
		return object == null || EmptyObjects.isEmpty(object);
	}
	
	protected abstract void show(T object);

	protected void showActions() {
		// to be overwritten
	}
	
	protected void setEnabled(boolean enabled) {
		visual.setEnabled(enabled);
	}

}
