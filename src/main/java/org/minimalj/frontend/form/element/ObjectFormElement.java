package org.minimalj.frontend.form.element;

import org.minimalj.frontend.action.Action;
import org.minimalj.model.properties.PropertyInterface;

public abstract class ObjectFormElement<T> extends AbstractObjectFormElement<T> {
	// private static final Logger logger = Logger.getLogger(ObjectField.class.getName());
	
	public ObjectFormElement(PropertyInterface property) {
		this(property, true);
	}

	public ObjectFormElement(PropertyInterface property, boolean editable) {
		super(property, editable);
	}

	public class RemoveObjectAction extends Action {
		@Override
		public void action() {
			ObjectFormElement.this.setValue(null);
		}
	}
}
