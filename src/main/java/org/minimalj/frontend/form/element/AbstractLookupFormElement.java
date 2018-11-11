package org.minimalj.frontend.form.element;

import java.util.Collection;
import java.util.logging.Logger;

import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.Frontend.IComponent;
import org.minimalj.frontend.Frontend.Input;
import org.minimalj.model.Rendering;

// Framework internal. Only use specializations
public abstract class AbstractLookupFormElement extends AbstractFormElement<Object> {
	private static Logger logger = Logger.getLogger(AbstractLookupFormElement.class.getSimpleName());

	protected final Input<String> lookup;
	private Object object;
	private String inputValue;
	private boolean internal = false;

	AbstractLookupFormElement(Object key, boolean editable) {
		super(key);
		if (editable) {
			Input<String> input;
			if (this instanceof LookupParser) {
				input = Frontend.getInstance().createTextField(((LookupParser) this).getAllowedSize(), ((LookupParser) this).getAllowedCharacters(), null,
						this::inputChanged);
			} else {
				input = Frontend.getInstance().createReadOnlyTextField();
			}
			lookup = Frontend.getInstance().createLookup(input, this::lookup);
		} else {
			lookup = Frontend.getInstance().createReadOnlyTextField();
		}

		inputValue = render(object);
		lookup.setValue(inputValue);
	}

	public static interface LookupParser {

		public default String getAllowedCharacters() {
			return null;
		}

		public default int getAllowedSize() {
			return Integer.MAX_VALUE;
		}

		public abstract Object parse(String text);
	}


	@Override
	public IComponent getComponent() {
		return lookup;
	}

	@Override
	public Object getValue() {
		return object;
	}

	@Override
	public void setValue(Object object) {
		this.object = object;
		inputValue = render(object);
		lookup.setValue(inputValue);
	}

	protected void setValueInternal(Object object) {
		internal = true;
		try {
			if (getProperty().isFinal() && object != this.object) {
				logger.warning("Validation may not work for: " + getProperty().getPath());
			}
			setValue(object);
			listener().changed(lookup);
		} finally {
			internal = false;
		}
	}

	protected String render(Object value) {
		return Rendering.toString(value);
	}

	public void inputChanged(IComponent source) {
		if (!internal) {
			String newInputValue = lookup.getValue();
			object = ((LookupParser) this).parse(newInputValue);
			if (object != null && !(object instanceof Collection) && object.getClass() != getProperty().getClazz()) {
				throw new IllegalStateException("Parser result of wrong class: " + object.getClass().getName() + " instead of " + getProperty().getClazz());
			}
			listener().changed(source);
		}
	}

	protected abstract void lookup();

}