package org.minimalj.frontend.edit.fields;

import org.minimalj.frontend.edit.EditorDialogAction;
import org.minimalj.frontend.toolkit.ClientToolkit;
import org.minimalj.model.PropertyInterface;

public abstract class ObjectLinkField<T> extends ObjectField<T> {
	// private static final Logger logger = Logger.getLogger(ObjectField.class.getName());
	
	public ObjectLinkField(PropertyInterface property) {
		this(property, true);
	}

	public ObjectLinkField(PropertyInterface property, boolean editable) {
		super(property, editable);
	}

	@Override
	protected void show(T object) {
		if (isEditable()) {
			visual.add(ClientToolkit.getToolkit().createLabel(new EditorDialogAction(new ObjectFieldEditor(display(object)))));
		} else {
			visual.add(ClientToolkit.getToolkit().createLabel(display(object)));
		}
	}
	
	@Override
	protected final void showActions() {
		// not to be overwritten
	}

	protected abstract String display(T object);

}
