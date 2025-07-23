package org.minimalj.frontend.form.element;

import java.util.Collection;
import java.util.logging.Logger;

import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.Frontend.IComponent;
import org.minimalj.frontend.Frontend.Input;
import org.minimalj.frontend.Frontend.Search;
import org.minimalj.frontend.Frontend.SwitchComponent;
import org.minimalj.frontend.action.Action;
import org.minimalj.frontend.action.ActionGroup;
import org.minimalj.frontend.impl.json.JsonTextField;
import org.minimalj.model.Rendering;
import org.minimalj.util.StringUtils;
import org.minimalj.util.resources.Resources;

// Framework internal. Only use specializations
public abstract class AbstractLookupFormElement<T> extends AbstractFormElement<T> implements Enable {
	private static Logger logger = Logger.getLogger(AbstractLookupFormElement.class.getSimpleName());

	private final SwitchComponent switchComponent;
	private Input<String> lookup;
	private Input<String> readOnlyInput;
	private boolean initialized = false;

	private T object;

	AbstractLookupFormElement(Object key, boolean editable) {
		super(key);
		if (editable) {
			switchComponent = Frontend.getInstance().createSwitchComponent();
		} else {
			switchComponent = null;
		}
	}

	@Override
	public final void setEnabled(boolean enabled) {
		if (switchComponent != null) {
			switchComponent.show(enabled ? getLookup() : getReadOnlyInput());
			initialized = true;
		}
	}

	private Input<String> getLookup() {
		if (lookup == null) {
			Input<String> input;
			if (this instanceof LookupParser) {
				input = Frontend.getInstance().createTextField(((LookupParser) this).getAllowedSize(),
						((LookupParser) this).getAllowedCharacters(), getSuggestionSearch(),
						this::inputChanged);
				if (input instanceof JsonTextField) {
					if (changeOnFocus()) {
						((JsonTextField) input).put("changeOnFocus", true);
					}
					((JsonTextField) input).setPlaceholder(getPlaceholer());
				}
			} else {
				input = Frontend.getInstance().createReadOnlyTextField();
			}
			ActionGroup actionGroup = createActionGroup();
			if (actionGroup != null) {
				lookup = Frontend.getInstance().createLookup(input, actionGroup);
			} else {
				String description = getLookupDescription();
				if (!StringUtils.isEmpty(description)) {
					lookup = Frontend.getInstance().createLookup(input, new Action(Resources.getString("SearchAction")) {
						@Override
						public void run() {
							lookup();
						}
						
						@Override
						public String getDescription() {
							return description;
						}
					});
				} else {
					lookup = Frontend.getInstance().createLookup(input, this::lookup);
				}
			}
			lookup.setValue(render(object));
		}
		return lookup;
	}
	
	protected String getPlaceholer() {
		return null;
	}
	
	private Input<String> getReadOnlyInput() {
		if (readOnlyInput == null) {
			readOnlyInput = Frontend.getInstance().createReadOnlyTextField();
			readOnlyInput.setValue(render(object));
		}
		return readOnlyInput;
	}
	
	protected ActionGroup createActionGroup() {
		return null;
	}
	
	protected String getLookupDescription() {
		return null;
	}

	public interface LookupParser {

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
		if (switchComponent != null) {
			if (!initialized) {
				switchComponent.show(getLookup());
			}
			return switchComponent;
		} else {
			return getReadOnlyInput();
		}
	}

	@Override
	public T getValue() {
		return object;
	}

	@Override
	public void setValue(T object) {
		this.object = object;
		if (lookup != null) {
			lookup.setValue(render(object));
		}
		if (readOnlyInput != null) {
			readOnlyInput.setValue(render(object));
		}
	}

	protected void setValueInternal(T object) {
		if (getProperty().isFinal() && object != this.object) {
			logger.warning("Validation may not work for: " + getProperty().getPath());
		}
		setValue(object);
		listener().changed(lookup);
	}

	protected String render(T value) {
		return Rendering.toString(value, getProperty());
	}
	
	protected boolean changeOnFocus() {
		return false;
	}

	public void inputChanged(IComponent source) {
		String newInputValue = lookup.getValue();
		if (object == null || !render(object).equals(newInputValue)) {
			object = (T) ((LookupParser) this).parse(newInputValue);
			if (object != null && !(object instanceof Collection) && object.getClass() != getProperty().getClazz()) {
				throw new IllegalStateException("Parser result of wrong class: " + object.getClass().getName() + " instead of " + getProperty().getClazz().getName());
			}
			listener().changed(source);
		}
	}

	protected abstract void lookup();

	protected Search<String> getSuggestionSearch() {
		return null;
	}

}