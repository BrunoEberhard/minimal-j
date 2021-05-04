package org.minimalj.frontend.form;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.Frontend.FormContent;
import org.minimalj.frontend.Frontend.IComponent;
import org.minimalj.frontend.form.element.BigDecimalFormElement;
import org.minimalj.frontend.form.element.CheckBoxFormElement;
import org.minimalj.frontend.form.element.CodeFormElement;
import org.minimalj.frontend.form.element.Enable;
import org.minimalj.frontend.form.element.EnumFormElement;
import org.minimalj.frontend.form.element.EnumSetFormElement;
import org.minimalj.frontend.form.element.FormElement;
import org.minimalj.frontend.form.element.IntegerFormElement;
import org.minimalj.frontend.form.element.LocalDateFormElement;
import org.minimalj.frontend.form.element.LocalDateTimeFormElement;
import org.minimalj.frontend.form.element.LocalTimeFormElement;
import org.minimalj.frontend.form.element.LongFormElement;
import org.minimalj.frontend.form.element.PasswordFormElement;
import org.minimalj.frontend.form.element.SelectionFormElement;
import org.minimalj.frontend.form.element.SmallCodeListFormElement;
import org.minimalj.frontend.form.element.StringFormElement;
import org.minimalj.frontend.form.element.TextFormElement;
import org.minimalj.frontend.form.element.UnknownFormElement;
import org.minimalj.model.Code;
import org.minimalj.model.Keys;
import org.minimalj.model.Selection;
import org.minimalj.model.annotation.Enabled;
import org.minimalj.model.properties.ChainedProperty;
import org.minimalj.model.properties.Properties;
import org.minimalj.model.properties.PropertyInterface;
import org.minimalj.model.validation.ValidationMessage;
import org.minimalj.security.model.Password;
import org.minimalj.util.ChangeListener;
import org.minimalj.util.CloneHelper;
import org.minimalj.util.Codes;
import org.minimalj.util.ExceptionUtils;
import org.minimalj.util.FieldUtils;
import org.minimalj.util.mock.Mocking;

public class Form<T> {
	private static Logger logger = Logger.getLogger(Form.class.getSimpleName());

	public static final boolean EDITABLE = true;
	public static final boolean READ_ONLY = false;

	public static final int DEFAULT_COLUMN_WIDTH = 100;
	public static final Object GROW_FIRST_ELEMENT = new Object();

	protected final boolean editable;
	
	private final int columns;
	private final FormContent formContent;
	
	private final LinkedHashMap<PropertyInterface, FormElement<?>> elements = new LinkedHashMap<>();
	
	private final FormChangeListener formChangeListener = new FormChangeListener();
	
	private ChangeListener<Form<?>> changeListener;
	private boolean changeFromOutside;

	private final Map<String, List<PropertyInterface>> dependencies = new HashMap<>();
	@SuppressWarnings("rawtypes")
	private final Map<PropertyInterface, Map<PropertyInterface, PropertyUpdater>> propertyUpdater = new HashMap<>();
	
	private T object;

	public Form() {
		this(EDITABLE);
	}

	public Form(boolean editable) {
		this(editable, 1);
	}

	public Form(int columns) {
		this(EDITABLE, columns);
	}

	public Form(boolean editable, int columns) {
		this(editable, columns, DEFAULT_COLUMN_WIDTH);
	}

	public Form(boolean editable, int columns, int columnWidth) {
		this.editable = editable;
		this.columns = columns;
		this.formContent = Frontend.getInstance().createFormContent(columns, columnWidth);
	}
	
	// Methods to create the form

	public FormContent getContent() {
		return formContent;
	}

	protected FormElement<?> createElement(Object key) {
		FormElement<?> element = null;
		PropertyInterface property;
		boolean forcedReadonly = false;
		if (key instanceof ReadOnlyWrapper) {
			forcedReadonly = true;
			key = ((ReadOnlyWrapper) key).key;
		}
		if (key == null) {
			throw new NullPointerException("Key must not be null");
		} else if (key instanceof FormElement) {
			element = (FormElement<?>) key;
			property = element.getProperty();
			if (property == null) throw new IllegalArgumentException(IComponent.class.getSimpleName() + " has no key");
		} else {
			property = Keys.getProperty(key);
			if (property != null) {
				boolean editable = !forcedReadonly && this.editable
						&& !(FieldUtils.isAllowedPrimitive(property.getClazz()) && property.isFinal());
				element = createElement(property, editable);
			}
		}
		return element;
	}
	
	@SuppressWarnings("rawtypes")
	protected FormElement<?> createElement(PropertyInterface property, boolean editable) {
		Class<?> fieldClass = property.getClazz();

		if (fieldClass == String.class) {
			return editable ? new StringFormElement(property) : new TextFormElement(property);
		} else if (fieldClass == Boolean.class) {
			return new CheckBoxFormElement(property, editable);
		} else if (fieldClass == Integer.class) {
			return new IntegerFormElement(property, editable);
		} else if (fieldClass == Long.class) {
			return new LongFormElement(property, editable);
		} else if (fieldClass == BigDecimal.class) {
			return new BigDecimalFormElement(property, editable);
		} else if (fieldClass == LocalDate.class) {
			return new LocalDateFormElement(property, editable);
		} else if (fieldClass == LocalTime.class) {
			return new LocalTimeFormElement(property, editable);
		} else if (fieldClass == LocalDateTime.class) {
			return new LocalDateTimeFormElement(property, editable);			
		} else if (Code.class.isAssignableFrom(fieldClass)) {
			return editable ? new CodeFormElement(property) : new TextFormElement(property);
		} else if (Enum.class.isAssignableFrom(fieldClass)) {
			return editable ? new EnumFormElement(property) : new TextFormElement(property);
		} else if (fieldClass == Set.class) {
            return new EnumSetFormElement(property, editable);
		} else if (fieldClass == List.class && Codes.isCode(property.getGenericClass())) {
			return new SmallCodeListFormElement(property, editable);
		} else if (fieldClass == Password.class) {
			return new PasswordFormElement(new ChainedProperty(property, Keys.getProperty(Password.$.getPassword())));
		} else if (fieldClass == Selection.class) {
			return new SelectionFormElement(property);
		}	
		logger.severe("No FormElement could be created for: " + property.getName() + " of class " + fieldClass.getName());
		return new UnknownFormElement(property);
	}
	
	// 

	public void line(Object... keys) {
		if (keys[0] == GROW_FIRST_ELEMENT) {
			assertColumnCount(keys.length - 1);
			for (int i = 1; i < keys.length; i++) {
				int elementSpan = i == 1 ? columns - keys.length + 2 : 1;
				add(keys[i], elementSpan);
			}
		} else {
			assertColumnCount(keys.length);
			int span = columns / keys.length;
			int rest = columns;
			for (int i = 0; i < keys.length; i++) {
				int elementSpan = i < keys.length - 1 ? span : rest;
				add(keys[i], elementSpan);
				rest = rest - elementSpan;
			}
		}
	}

	private void assertColumnCount(int elementCount) {
		if (elementCount > columns) {
			logger.severe("This form was constructed for " + columns + " column(s) but should be filled with " + elementCount + " form elements");
			logger.fine("The solution is most probably to add/set the correct number of columns when calling the Form constructor");
			throw new IllegalArgumentException("Not enough columns (" + columns + ") for form elements (" + elementCount + ")");
		}
	}

	private void add(Object key, int elementSpan) {
		FormElement<?> element = createElement(key);
		if (element != null) {
			add(element, elementSpan);
		} else {
			formContent.add(null, Frontend.getInstance().createText("" + key), null, elementSpan);
		}
	}

	private void add(FormElement<?> element, int span) {
		formContent.add(element.getCaption(), element.getComponent(), element.getConstraint(), span);
		registerNamedElement(element);
		addDependencies(element);
	}
	
	// 
	
	public static Object readonly(Object key) {
		ReadOnlyWrapper wrapper = new ReadOnlyWrapper();
		wrapper.key = key;
		return wrapper;
	}
	
	private static class ReadOnlyWrapper {
		private Object key;
	}
	
	public void addTitle(String text) {
		IComponent label = Frontend.getInstance().createTitle(text);
		formContent.add(null, label, null, -1);
	}

	//

	/**
	 * Declares that if the <i>from</i> property changes all
	 * the properties with <i>to</i> could change. This is normally used
	 * if the to <i>to</i> property is a getter that calculates something that
	 * depends on the <i>from</i> in some way.
	 * 
	 * @param from the key or property of the field triggering the update
	 * @param to the field possible changed its value implicitly
	 */
	public void addDependecy(Object from, Object... to) {
		PropertyInterface fromProperty = Keys.getProperty(from);
		if (!dependencies.containsKey(fromProperty.getPath())) {
			dependencies.put(fromProperty.getPath(), new ArrayList<>());
		}
		List<PropertyInterface> list = dependencies.get(fromProperty.getPath());
		for (Object key : to) {
			list.add(Keys.getProperty(key));
		}
	}

	private void addDependecy(PropertyInterface fromProperty, PropertyInterface to) {
		if (!dependencies.containsKey(fromProperty.getPath())) {
			dependencies.put(fromProperty.getPath(), new ArrayList<>());
		}
		List<PropertyInterface> list = dependencies.get(fromProperty.getPath());
		list.add(to);
	}
	
	/**
	 * Declares that if the key or property <i>from</i> changes the specified
	 * updater should be called and after its return the <i>to</i> key or property
	 * could have changed.<p>
	 * 
	 * This is used if there is a more complex relation between two fields.
	 * 
	 * @param <FROM> the type (class) of the fromKey / field
	 * @param <TO> the type (class) of the toKey / field
	 * @param from the field triggering the update
	 * @param updater the updater doing the change of the to field
	 * @param to the changed field by the updater
	 */
	public <FROM, TO> void addDependecy(FROM from, PropertyUpdater<FROM, TO, T> updater, TO to) {
		PropertyInterface fromProperty = Keys.getProperty(from);
		if (!propertyUpdater.containsKey(fromProperty)) {
			propertyUpdater.put(fromProperty, new HashMap<>());
		}
		PropertyInterface toProperty = Keys.getProperty(to);
		propertyUpdater.get(fromProperty).put(toProperty, updater);
		addDependecy(from, to);
	}

	public interface PropertyUpdater<FROM, TO, EDIT_OBJECT> {
		
		/**
		 * 
		 * @param input The new value of the property that has changed
		 * @param copyOfEditObject The current object of the  This reference should <b>not</b> be changed.
		 * It should be treated as a read only version or a copy of the object.
		 * It's probably not a real copy as it is to expensive to copy the object for every call. 
		 * @return The new value the updater wants to set to the toKey property
		 */
		public TO update(FROM input, EDIT_OBJECT copyOfEditObject);
		
	}

	//

	private void registerNamedElement(FormElement<?> field) {
		elements.put(field.getProperty(), field);
		field.setChangeListener(formChangeListener);
	}

	private void addDependencies(FormElement<?> field) {
		List<PropertyInterface> dependencies = Keys.getDependencies(field.getProperty());
		for (PropertyInterface dependency : dependencies) {
			addDependecy(dependency, field.getProperty());
		}
	}
	
	public final void mock() {
		changeFromOutside = true;
		try {
			fillWithDemoData(object);
		} catch (Exception x) {
			logger.log(Level.SEVERE, "Fill with demo data failed", x);
		} finally {
			readValueFromObject();
			changeFromOutside = false;
		}
	}

	protected void fillWithDemoData(T object) {
		for (FormElement<?> field : elements.values()) {
			PropertyInterface property = field.getProperty();
			if (field instanceof Mocking) {
				Mocking demoEnabledElement = (Mocking) field;
				demoEnabledElement.mock();
				property.setValue(object, field.getValue());
			}
		}		
	}
	
	//
	
	/**
	 * 
	 * @return Collection provided by a LinkedHashMap so it will be a ordered set
	 */
	public Collection<PropertyInterface> getProperties() {
		return elements.keySet();
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void set(PropertyInterface property, Object value) {
		FormElement element = elements.get(property);
		try {
			element.setValue(value);
		} catch (Exception x) {
			ExceptionUtils.logReducedStackTrace(logger, x);
		}
	}

	private void setValidationMessage(PropertyInterface property, List<String> validationMessages) {
		FormElement<?> field = elements.get(property);
		formContent.setValidationMessages(field.getComponent(), validationMessages);
	}

	public void setObject(T object) {
		if (editable && changeListener == null) throw new IllegalStateException("Listener has to be set on a editable Form");
		changeFromOutside = true;
		this.object = object;
		readValueFromObject();
		changeFromOutside = false;
	}

	private void readValueFromObject() {
		for (PropertyInterface property : getProperties()) {
			Object propertyValue = property.getValue(object);
			set(property, propertyValue);
		}
		updateEnable();
	}
	
	private String getName(FormElement<?> field) {
		PropertyInterface property = field.getProperty();
		return property.getName();
	}
	
	public void setChangeListener(ChangeListener<Form<?>> changeListener) {
		this.changeListener = Objects.requireNonNull(changeListener);
	}

	private class FormChangeListener implements ChangeListener<FormElement<?>> {

		@Override
		public void changed(FormElement<?> changedField) {
			if (changeFromOutside) return;
			if (changeListener == null) {
				if (editable) logger.severe("Editable Form must have a listener");
				return;
			}
			
			logger.fine("ChangeEvent from " + getName(changedField));
			
			PropertyInterface property = changedField.getProperty();
			Object newValue = changedField.getValue();

			// Call updaters before set the new value  (so they also can read the old value)
			executeUpdater(property, newValue);
			
			// now set the new value. method - properties can use it as base
			property.setValue(object, newValue);
			
			// propagate all possible changed values to the form elements
			refreshDependendFields(property);
			
			// update enable/disable fields
			updateEnable();
			
			changeListener.changed(Form.this);
		}


		@SuppressWarnings({ "unchecked", "rawtypes" })
		private void refreshDependendFields(PropertyInterface property) {
			if (dependencies.containsKey(property.getPath())) {
				List<PropertyInterface> dependendProperties = dependencies.get(property.getPath());
				for (PropertyInterface dependendProperty : dependendProperties) {
					for (FormElement formElement : elements.values()) {
						String formElementPath = formElement.getProperty().getPath();
						String dependedPath = dependendProperty.getPath();
						if (formElementPath.equals(dependedPath) || formElementPath.startsWith(dependedPath) && formElementPath.charAt(dependedPath.length()) == '.') {
							Object newDependedValue = formElement.getProperty().getValue(object);
							formElement.setValue(newDependedValue);
							changed(formElement);
						}
					}
				}
			}
		}

		@SuppressWarnings({ "rawtypes", "unchecked" })
		private void executeUpdater(PropertyInterface property, Object value) {
			if (propertyUpdater.containsKey(property)) {
				Map<PropertyInterface, PropertyUpdater> updaters = propertyUpdater.get(property);
				for (Map.Entry<PropertyInterface, PropertyUpdater> entry : updaters.entrySet()) {
					Object ret = entry.getValue().update(value, CloneHelper.clone(object));
					entry.getKey().setValue(object, ret);
				}
			}
		}
	}
	
	private void updateEnable() {
		for (Map.Entry<PropertyInterface, FormElement<?>> element : elements.entrySet()) {
			PropertyInterface property = element.getKey();

			boolean enabled = true;
			if (property instanceof ChainedProperty) {
				enabled = isEnabled(object, (ChainedProperty) property);
			} else {
				Enabled enabledAnnotation = property.getAnnotation(Enabled.class);
				enabled = enabledAnnotation == null || isEnabled(object, property, enabledAnnotation);
			}

			if (element.getValue() instanceof Enable) {
				((Enable) element.getValue()).setEnabled(enabled);
			} else if (!enabled) {
				if (editable) {
					logger.severe("element " + property.getPath() + " should implement Enable");
				} else {
					logger.fine("element " + property.getPath() + " should maybe implement Enable");
				}
			}
		}
	}

	// TODO move to properties package, remove use of getPath, write tests
	static boolean isEnabled(Object object, ChainedProperty property) {
		String fieldPath = property.getPath();
		while (fieldPath.contains(".")) {
			if (object == null) {
				return false;
			}
			int pos = fieldPath.indexOf(".");
			PropertyInterface p2 = Properties.getProperty(object.getClass(), fieldPath.substring(0, pos));
			Enabled enabledAnnotation = p2.getAnnotation(Enabled.class);
			
			if (enabledAnnotation != null) {
				if (!isEnabled(object, p2, enabledAnnotation)) {
					return false;
				}
			}	
			object = p2.getValue(object);
			fieldPath = fieldPath.substring(pos + 1);
		}
		return true;
	}

	// TODO move to properties package, write tests
	static boolean isEnabled(Object object, PropertyInterface property, Enabled enabledAnnotation) {
		String methodName = enabledAnnotation.value();
		boolean invert = methodName.startsWith("!");
		if (invert)
			methodName = methodName.substring(1);
		try {
			Class<?> clazz = object.getClass();
			Method method = clazz.getMethod(methodName);
			if (!((Boolean) method.invoke(object) ^ invert)) {
				return false;
			}
		} catch (Exception x) {
			String fieldName = property.getName();
			if (!fieldName.equals(property.getPath())) {
				fieldName += " (" + property.getPath() + ")";
			}
			logger.log(Level.SEVERE, "Update enable of " + fieldName + " failed", x);
		}
		return true;
	}
	
	public boolean indicate(List<ValidationMessage> validationMessages) {
		boolean relevantValidationMessage = false;
		for (PropertyInterface property : getProperties()) {
			List<String> filteredValidationMessages = ValidationMessage.filterValidationMessage(validationMessages, property);
			setValidationMessage(property, filteredValidationMessages);
			relevantValidationMessage |= !filteredValidationMessages.isEmpty();
		}
		return relevantValidationMessage;
	}
}