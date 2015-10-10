package org.minimalj.frontend.form.element;

import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.Frontend.IComponent;
import org.minimalj.frontend.Frontend.IList;
import org.minimalj.frontend.action.Action;
import org.minimalj.frontend.editor.Editor;
import org.minimalj.frontend.form.Form;
import org.minimalj.frontend.page.Page;
import org.minimalj.frontend.page.PageAction;
import org.minimalj.model.Rendering;
import org.minimalj.model.properties.PropertyInterface;
import org.minimalj.util.CloneHelper;
import org.minimalj.util.GenericUtils;

/**
 * The state of an ObjectField is saved in the object variable.<p>
 * 
 * You have to implement for an ObjectField:
 * <ul>
 * <li>display: The widgets have to be updated according to the object</li>
 * <li>fireChange: The object has to be updated according the widgets</li>
 * </ul>
 * 
 */
public abstract class AbstractObjectFormElement<T> extends AbstractFormElement<T> implements Enable {
	// private static final Logger logger = Logger.getLogger(ObjectField.class.getName());
	
	private final boolean editable;

	private final IList list;

	private T object;
	
	public AbstractObjectFormElement(PropertyInterface property, boolean editable) {
		super(property);
		this.editable = editable;
		list = editable ? Frontend.getInstance().createList(getActions()) : Frontend.getInstance().createList();
	}

	protected final boolean isEditable() {
		return editable;
	}
	
	@Override
	public T getValue() {
		return object;
	}

	@Override
	public void setValue(T object) {
		this.object = object;
		handleChange();
	}

	@Override
	public IComponent getComponent() {
		return list;
	}
	
	protected abstract Form<T> createFormPanel();

	protected T createObject() {
		if (AbstractObjectFormElement.this.getValue() != null) {
			return CloneHelper.clone(AbstractObjectFormElement.this.getValue());
		} else {
			return (T) CloneHelper.newInstance(GenericUtils.getGenericClass(AbstractObjectFormElement.this.getClass()));
		}
	}

	public class ObjectFormElementEditor extends Editor<T, Void> {
		public ObjectFormElementEditor() {
		}

		public ObjectFormElementEditor(String name) {
			super(name);
		}

		@Override
		public Form<T> createForm() {
			return AbstractObjectFormElement.this.createFormPanel();
		}

		@Override
		public T createObject() {
			return AbstractObjectFormElement.this.createObject();
		}

		@Override
		public Void save(T edited) {
			AbstractObjectFormElement.this.setValue(edited);
			return null;
		}
		
		@Override
		protected void finished(Void result) {
			handleChange();
		}
	}
	
	protected Action getEditorAction() {
		return new ObjectFormElementEditor();
	}
	
	/*
	 * Only to be used in ObjectFieldEditor
	 */
	protected T newInstance() {
		@SuppressWarnings("unchecked")
		Class<T> clazz = (Class<T>) org.minimalj.util.GenericUtils.getGenericClass(AbstractObjectFormElement.this.getClass());
		T newInstance = CloneHelper.newInstance(clazz);
		return newInstance;
	}
	
	protected void handleChange() {
		display();
		super.fireChange();
	}

	protected void display() {
		list.clear();
		if (object != null) {
			show(object);
		}
	}

	protected abstract void show(T object);
	
	protected void add(Rendering rendering, Action... actions) {
		if (isEditable()) {
			list.add(Frontend.getInstance().createText(rendering), actions);
		} else if (!(object instanceof Action)) {
			list.add(Frontend.getInstance().createText(rendering));
		}
	}

	protected void add(Object object, Action... actions) {
		add(object.toString(), actions);
	}
	
	protected void add(String text, Action... actions) {
		if (isEditable()) {
			list.add(Frontend.getInstance().createLabel(text), actions);
		} else if (!(object instanceof Action)) {
			list.add(Frontend.getInstance().createLabel(text));
		}
	}

	protected void add(Action action) {
		if (isEditable()) {
			list.add(Frontend.getInstance().createLabel(action));
		}
	}
	
	protected void add(String text, Page linkedPage) {
		list.add(Frontend.getInstance().createLabel(new PageAction(linkedPage, text)));
	}

	protected void add(Rendering rendering, Page linkedPage) {
		list.add(Frontend.getInstance().createText(rendering), new PageAction(linkedPage));
	}
	
	/**
	 * Null as return is ok, but mostly an 'Add'-Action is returned.
	 * The actions are only shown if the Form(Element) is editable
	 * 
	 * @return the actions that apply for all (and the 'empty') entries
	 */
	protected Action[] getActions() {
		return null;
	}
	
	@Override
	public void setEnabled(boolean enabled) {
		list.setEnabled(enabled);
	}

}
