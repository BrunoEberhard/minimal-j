package org.minimalj.frontend.form.element;

import org.minimalj.backend.db.EmptyObjects;
import org.minimalj.frontend.editor.Editor;
import org.minimalj.frontend.form.Form;
import org.minimalj.frontend.toolkit.ClientToolkit;
import org.minimalj.frontend.toolkit.ClientToolkit.IComponent;
import org.minimalj.frontend.toolkit.FlowField;
import org.minimalj.model.properties.PropertyInterface;
import org.minimalj.util.CloneHelper;

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
public abstract class ObjectFormElement<T> extends AbstractFormElement<T> implements Enable {
	// private static final Logger logger = Logger.getLogger(ObjectField.class.getName());
	
	private final boolean editable;
	protected final FlowField flowField;

	private T object;
	private boolean enabled = true;
	
	public ObjectFormElement(PropertyInterface property) {
		this(property, true);
	}
	
	public ObjectFormElement(PropertyInterface property, boolean editable) {
		super(property);
		this.editable = editable;
		flowField = ClientToolkit.getToolkit().createFlowField();
	}

	protected boolean isEditable() {
		return editable;
	}
	
	@Override
	public T getValue() {
		return object;
	}

	@Override
	public void setValue(T object) {
		this.object = object;
		fireObjectChange();
	}

	@Override
	public IComponent getComponent() {
		return flowField;
	}
	
	protected abstract Form<T> createFormPanel();

	public class ObjectFieldEditor extends Editor<T> {
		private final String title;
		
		public ObjectFieldEditor() {
			this(null);
		}

		public ObjectFieldEditor(String title) {
			this.title = title;
		}
		
		@Override
		public String getTitle() {
			if (title != null) {
				return title;
			} else {
				return super.getTitle();
			}
		}

		@Override
		public Form<T> createForm() {
			return ObjectFormElement.this.createFormPanel();
		}

		@Override
		public T load() {
			return ObjectFormElement.this.getValue();
		}
		
		@Override
		public T newInstance() {
			// Delegate ObjectField class. Its not possible to override here
			// (because of some strange erasure thing)
			return ObjectFormElement.this.newInstance();
		}

		@Override
		public Object save(T edited) {
			ObjectFormElement.this.setValue(edited);
			return SAVE_SUCCESSFUL;
		}
	}
	
	/*
	 * Only to be used in ObjectFieldEditor
	 */
	protected T newInstance() {
		@SuppressWarnings("unchecked")
		Class<T> clazz = (Class<T>) org.minimalj.util.GenericUtils.getGenericClass(ObjectFormElement.this.getClass());
		T newInstance = CloneHelper.newInstance(clazz);
		return newInstance;
	}
	
	protected void fireObjectChange() {
		display();
		super.fireChange();
	}

	protected void display() {
		flowField.clear();
		if (enabled) {
			if (object != null) {
				show(object);
			}
			if (isEditable()) {
				showActions();
			}
		}
	}

	public boolean isEmpty() {
		Object object = getValue();
		return object == null || EmptyObjects.isEmpty(object);
	}
	
	protected abstract void show(T object);

	protected void showActions() {
		// to be overwritten
	}
	
	public void setEnabled(boolean enabled) {
		if (enabled != this.enabled) {
			this.enabled = enabled;
			display();
		}
	}

}
