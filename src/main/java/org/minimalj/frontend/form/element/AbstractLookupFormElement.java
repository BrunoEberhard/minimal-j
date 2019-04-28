package org.minimalj.frontend.form.element;

import java.util.Collection;
import java.util.logging.Logger;

import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.Frontend.IComponent;
import org.minimalj.frontend.Frontend.Input;
import org.minimalj.frontend.Frontend.Search;
import org.minimalj.model.Rendering;

// Framework internal. Only use specializations
public abstract class AbstractLookupFormElement<T> extends AbstractFormElement<T> {
	private static Logger logger = Logger.getLogger(AbstractLookupFormElement.class.getSimpleName());

	protected final Input<String> lookup;
	private T object;
	private boolean internal = false;

	AbstractLookupFormElement(Object key, boolean editable) {
		super(key);
		if (editable) {
			Input<String> input;
			if (this instanceof LookupParser) {
				input = Frontend.getInstance().createTextField(((LookupParser) this).getAllowedSize(),
						((LookupParser) this).getAllowedCharacters(), getSearch(),
						this::inputChanged);
			} else {
				input = Frontend.getInstance().createReadOnlyTextField();
			}
			lookup = Frontend.getInstance().createLookup(input, this::lookup);
		} else {
			lookup = Frontend.getInstance().createReadOnlyTextField();
		}

		lookup.setValue(render(object));
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
	public T getValue() {
		return object;
	}

	@Override
	public void setValue(T object) {
		this.object = object;
		lookup.setValue(render(object));
	}

	protected void setValueInternal(T object) {
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

	protected String render(T value) {
		return Rendering.toString(value);
	}

	public void inputChanged(IComponent source) {
		if (!internal) {
			String newInputValue = lookup.getValue();
			object = (T) ((LookupParser) this).parse(newInputValue);
			if (object != null && !(object instanceof Collection) && object.getClass() != getProperty().getClazz()) {
				throw new IllegalStateException("Parser result of wrong class: " + object.getClass().getName() + " instead of " + getProperty().getClazz());
			}
			listener().changed(source);
		}
	}

	protected abstract void lookup();

	protected Search<String> getSearch() {
		return null;
	}

}