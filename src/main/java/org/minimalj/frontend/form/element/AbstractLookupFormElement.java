package org.minimalj.frontend.form.element;

import java.util.logging.Logger;

import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.Frontend.IComponent;
import org.minimalj.frontend.Frontend.Input;
import org.minimalj.model.Rendering;

public abstract class AbstractLookupFormElement<T> extends AbstractFormElement<T> {
	private static Logger logger = Logger.getLogger(AbstractLookupFormElement.class.getSimpleName());

	protected final Input<String> lookup;
	private T object;
	private String inputValue;
	private boolean internal = false;

	public AbstractLookupFormElement(Object key, boolean textEditable, boolean editable) {
		super(key);
		if (editable) {
			Input<String> input = textEditable ? Frontend.getInstance().createTextField(Integer.MAX_VALUE, null, null, this::inputChanged)
					: Frontend.getInstance().createReadOnlyTextField();
			lookup = Frontend.getInstance().createLookup(input, this::lookup);
		} else {
			lookup = Frontend.getInstance().createReadOnlyTextField();
		}

		inputValue = render(object);
		lookup.setValue(inputValue);
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
		inputValue = render(object);
		lookup.setValue(inputValue);
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

	protected abstract T parse(String text);

	public void inputChanged(IComponent source) {
		if (!internal) {
			String newInputValue = lookup.getValue();
			object = parse(newInputValue);
			listener().changed(source);
		}
	}

	protected abstract void lookup();

}