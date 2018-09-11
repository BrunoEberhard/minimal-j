package org.minimalj.frontend.form.element;

import java.util.logging.Logger;

import org.minimalj.application.Configuration;
import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.Frontend.IComponent;
import org.minimalj.frontend.Frontend.IList;
import org.minimalj.frontend.Frontend.Input;
import org.minimalj.frontend.action.Action;
import org.minimalj.frontend.page.Page;
import org.minimalj.frontend.page.PageAction;
import org.minimalj.model.Rendering;
import org.minimalj.model.properties.PropertyInterface;
import org.minimalj.util.CloneHelper;
import org.minimalj.util.GenericUtils;

/**
 * The state of an ObjectField is saved in the object variable.
 * <p>
 * 
 * You have to implement for an ObjectField:
 * <ul>
 * <li>display: The widgets have to be updated according to the object</li>
 * <li>fireChange: The object has to be updated according the widgets</li>
 * </ul>
 * 
 */
public abstract class AbstractObjectFormElement<T> extends AbstractFormElement<T>implements Enable {
	private static final Logger logger = Logger.getLogger(AbstractObjectFormElement.class.getName());

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

	protected T createObject() {
		if (AbstractObjectFormElement.this.getValue() != null) {
			return CloneHelper.clone(AbstractObjectFormElement.this.getValue());
		} else {
			return (T) CloneHelper.newInstance(GenericUtils.getGenericClass(AbstractObjectFormElement.this.getClass()));
		}
	}

	protected void assertEditable(Object object) {
		if (!isEditable()) {
			String msg = object.getClass().getSimpleName() + " should not be used if " + AbstractObjectFormElement.class.getSimpleName()
					+ " is not editable";
			if (Configuration.isDevModeActive()) {
				throw new IllegalArgumentException(msg);
			} else {
				logger.warning(msg);
			}
		}
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
		list.add(Frontend.getInstance().createText(rendering), actions);
	}

	protected void add(Object object, Action... actions) {
		if (object instanceof Rendering) {
			add((Rendering) object, actions);
		} else {
			add(object.toString(), actions);
		}
	}

	protected void add(String text, Action... actions) {
		list.add(Frontend.getInstance().createText(text), actions);
	}

	protected void add(Action action) {
		list.add(Frontend.getInstance().createText(action));
	}

	protected void add(String text, Page linkedPage) {
		list.add(Frontend.getInstance().createText(new PageAction(linkedPage, text)));
	}

	protected void add(Rendering rendering, Page linkedPage) {
		list.add(Frontend.getInstance().createText(rendering), new PageAction(linkedPage));
	}

	protected void addTextArea(String text, Action... actions) {
		Input<String> textArea = Frontend.getInstance().createAreaField(text.length(), null, listener());
		textArea.setValue(text);
		textArea.setEditable(false);
		list.add(textArea, actions);
	}

	/**
	 * Null as return is ok, but mostly an 'Add'-Action is returned. The actions
	 * are only shown if the Form(Element) is editable
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
