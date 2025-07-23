package org.minimalj.frontend.form.element;

import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.Frontend.IComponent;
import org.minimalj.frontend.Frontend.Input;
import org.minimalj.frontend.Frontend.NamedFile;
import org.minimalj.model.properties.Property;

public class UploadsFormElement extends AbstractFormElement<NamedFile[]> {

	private final Input<NamedFile[]> input;

	public UploadsFormElement(NamedFile[] key, boolean editable) {
		super(key);
		input = Frontend.getInstance().createUpload(editable ? listener() : null, true);
	}

	public UploadsFormElement(Property property, boolean editable) {
		super(property);
		input = Frontend.getInstance().createUpload(editable ? listener() : null, true);
	}

	@Override
	public void setValue(NamedFile[] object) {
		input.setValue(object);
	}

	@Override
	public NamedFile[] getValue() {
		return input.getValue();
	}

	@Override
	public IComponent getComponent() {
		return input;
	}

}