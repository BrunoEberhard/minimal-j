package org.minimalj.frontend.form.element;

import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.Frontend.IComponent;
import org.minimalj.frontend.Frontend.Input;
import org.minimalj.frontend.Frontend.InputComponentListener;
import org.minimalj.model.properties.PropertyInterface;

public class ImageFormElement extends AbstractFormElement<byte[]> {

	private final Input<byte[]> input;
	private final FormElementConstraint constraint;

	public ImageFormElement(byte[] key, boolean editable) {
		this(key, editable, 3);
	}
	
	public ImageFormElement(byte[] key, boolean editable, int size) {
		super(key);
		this.constraint = new FormElementConstraint(size, size);
		input = Frontend.getInstance().createImage(editable ? new ImageFieldChangeListener() : null);
	}

	public ImageFormElement(PropertyInterface property, boolean editable) {
		this(property, editable, 3);
	}

	public ImageFormElement(PropertyInterface property, boolean editable, int size) {
		super(property);
		this.constraint = new FormElementConstraint(size, size);
		input = Frontend.getInstance().createImage(editable ? new ImageFieldChangeListener() : null);
	}

	@Override
	public FormElementConstraint getConstraint() {
		return constraint;
	}

	@Override
	public void setValue(byte[] object) {
		input.setValue(object);
	}

	@Override
	public byte[] getValue() {
		return input.getValue();
	}

	@Override
	public IComponent getComponent() {
		return input;
	}
	
	private class ImageFieldChangeListener implements InputComponentListener {
		@Override
		public void changed(IComponent source) {
			// TODO image validation
			fireChange();
		}
	}
}