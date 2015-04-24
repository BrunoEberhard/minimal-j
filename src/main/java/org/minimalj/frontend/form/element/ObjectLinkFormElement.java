package org.minimalj.frontend.form.element;

import org.minimalj.frontend.editor.EditorAction;
import org.minimalj.frontend.toolkit.ClientToolkit;
import org.minimalj.model.properties.PropertyInterface;

public abstract class ObjectLinkFormElement<T> extends ObjectFormElement<T> {
	// private static final Logger logger = Logger.getLogger(ObjectField.class.getName());
	
	public ObjectLinkFormElement(PropertyInterface property) {
		this(property, true);
	}

	public ObjectLinkFormElement(PropertyInterface property, boolean editable) {
		super(property, editable);
	}

	@Override
	protected void show(T object) {
		if (isEditable()) {
			flowField.add(ClientToolkit.getToolkit().createLabel(new EditorAction(new ObjectFieldEditor(display(object)))));
		} else {
			flowField.add(ClientToolkit.getToolkit().createLabel(display(object)));
		}
	}
	
	@Override
	protected final void showActions() {
		// not to be overwritten
	}

	protected abstract String display(T object);

}
