package org.minimalj.frontend.form.element;

import org.minimalj.frontend.action.Action;
import org.minimalj.model.properties.PropertyInterface;

public abstract class ObjectFormElement<T> extends AbstractObjectFormElement<T> {

	public ObjectFormElement(PropertyInterface property) {
		this(property, true);
	}

	public ObjectFormElement(PropertyInterface property, boolean editable) {
		super(property, editable);
	}

	public class RemoveObjectAction extends Action {

		public RemoveObjectAction() {
			assertEditable(this);
		}

		@Override
		public void action() {
			ObjectFormElement.this.setValue(null);
		}
	}
}
