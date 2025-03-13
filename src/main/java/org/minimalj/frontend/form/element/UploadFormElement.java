package org.minimalj.frontend.form.element;

import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.Frontend.IComponent;
import org.minimalj.frontend.Frontend.Input;
import org.minimalj.frontend.Frontend.NamedFile;
import org.minimalj.model.properties.Property;

public class UploadFormElement extends AbstractFormElement<NamedFile> {

	private final Input<NamedFile[]> input;

	public UploadFormElement(NamedFile key, boolean editable) {
		super(key);
		input = Frontend.getInstance().createUpload(editable ? listener() : null, false);
	}

	public UploadFormElement(Property property, boolean editable) {
		super(property);
		input = Frontend.getInstance().createUpload(editable ? listener() : null, false);
	}

	@Override
	public void setValue(NamedFile object) {
		input.setValue(object != null ? new NamedFile[] { object } : new NamedFile[0]);
	}

	@Override
	public NamedFile getValue() {
		NamedFile[] namedFiles = input.getValue();
		return namedFiles.length == 0 ? null : namedFiles[0];
	}

	@Override
	public IComponent getComponent() {
		return input;
	}

}