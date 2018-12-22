package org.minimalj.frontend.form.element;

import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.Frontend.IComponent;
import org.minimalj.frontend.Frontend.Input;
import org.minimalj.model.Keys;
import org.minimalj.model.Rendering;
import org.minimalj.model.properties.PropertyInterface;
import org.minimalj.util.ChangeListener;

/**
 * Read only FormElement. This element is used as a complement
 * for elements providing only editable mode. For those elements
 * the Form (ElementFactory) has to construct the right element.
 *
 */
public class TextFormElement implements FormElement<Object> {

	private final PropertyInterface property;

	protected final Input<String> textField;

	public TextFormElement(Object key) {
		this(Keys.getProperty(key));
	}
	
	public TextFormElement(PropertyInterface property) {
		this.property = property;
		this.textField = Frontend.getInstance().createReadOnlyTextField();
	}

	@Override
	public PropertyInterface getProperty() {
		return property;
	}

	@Override
	public IComponent getComponent() {
		return textField;
	}

	@Override
	public Object getValue() {
		throw new RuntimeException("getObject() on TextElement " + this.getClass().getSimpleName() + " must not be called");
	}

	@Override
	public void setChangeListener(ChangeListener<FormElement<?>> changeListener) {
		// ignored
	}

	@Override
	public void setValue(Object object) {
		textField.setValue(Rendering.toString(object));
	}

	@Override
	public FormElementConstraint getConstraint() {
		return null;
	}
}
