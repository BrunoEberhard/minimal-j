package org.minimalj.frontend.form.element;

import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.Frontend.IComponent;
import org.minimalj.frontend.Frontend.Input;
import org.minimalj.model.Keys;
import org.minimalj.model.Rendering;
import org.minimalj.model.annotation.AnnotationUtil;
import org.minimalj.model.properties.Property;
import org.minimalj.util.ChangeListener;
import org.minimalj.util.resources.Resources;

/**
 * Read only FormElement. This element is used as a complement
 * for elements providing only editable mode. For those elements
 * the Form (ElementFactory) has to construct the right element.
 *
 */
public class TextFormElement implements FormElement<Object> {

	private final Property property;

	private final int lines;
	protected final Input<String> textField;

	public TextFormElement(Object key) {
		this(Keys.getProperty(key));
	}
	
	public TextFormElement(String key, int lines) {
		this(Keys.getProperty(key), lines);
	}
	
	public TextFormElement(Property property) {
		this(property, AnnotationUtil.getSize(property, AnnotationUtil.OPTIONAL) < 256 ? StringFormElement.SINGLE_LINE : StringFormElement.MULTI_LINE);
	}
	
	public TextFormElement(Property property, int lines) {
		this.property = property;
		this.lines = lines;
		this.textField = Frontend.getInstance().createReadOnlyTextField();
	}

	@Override
	public Property getProperty() {
		return property;
	}

	@Override
	public String getCaption() {
		return Resources.getPropertyName(getProperty());
	}

	@Override
	public boolean canBeEmpty() {
		return false;
	}

	@Override
	public IComponent getComponent() {
		return textField;
	}
	
	@Override
	public FormElementConstraint getConstraint() {
		if (lines != 1) {
			return new FormElementConstraint(1, lines);
		} else {
			return null;
		}
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
		textField.setValue(object != null ? Rendering.toString(object, property) : null);
	}
}
