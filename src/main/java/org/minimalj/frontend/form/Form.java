package org.minimalj.frontend.form;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.minimalj.application.Configuration;
import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.Frontend.FormContent;
import org.minimalj.frontend.Frontend.IComponent;
import org.minimalj.frontend.form.element.BigDecimalFormElement;
import org.minimalj.frontend.form.element.CheckBoxFormElement;
import org.minimalj.frontend.form.element.CodeFormElement;
import org.minimalj.frontend.form.element.ComboBoxFormElement;
import org.minimalj.frontend.form.element.Enable;
import org.minimalj.frontend.form.element.EnumFormElement;
import org.minimalj.frontend.form.element.EnumSetFormElement;
import org.minimalj.frontend.form.element.FormElement;
import org.minimalj.frontend.form.element.Indication;
import org.minimalj.frontend.form.element.IntegerFormElement;
import org.minimalj.frontend.form.element.LocalDateFormElement;
import org.minimalj.frontend.form.element.LocalDateTimeFormElement;
import org.minimalj.frontend.form.element.LocalTimeFormElement;
import org.minimalj.frontend.form.element.LongFormElement;
import org.minimalj.frontend.form.element.PasswordFormElement;
import org.minimalj.frontend.form.element.SelectionFormElement;
import org.minimalj.frontend.form.element.SmallCodeListFormElement;
import org.minimalj.frontend.form.element.StringFormElement;
import org.minimalj.frontend.form.element.TableFormElement;
import org.minimalj.frontend.form.element.TextFormElement;
import org.minimalj.frontend.form.element.UnknownFormElement;
import org.minimalj.model.Code;
import org.minimalj.model.Keys;
import org.minimalj.model.Rendering;
import org.minimalj.model.Selection;
import org.minimalj.model.annotation.Enabled;
import org.minimalj.model.annotation.NotEmpty;
import org.minimalj.model.annotation.Visible;
import org.minimalj.model.properties.ChainedProperty;
import org.minimalj.model.properties.Property;
import org.minimalj.model.validation.ValidationMessage;
import org.minimalj.security.model.Password;
import org.minimalj.util.ChangeListener;
import org.minimalj.util.CloneHelper;
import org.minimalj.util.Codes;
import org.minimalj.util.EqualsHelper;
import org.minimalj.util.ExceptionUtils;
import org.minimalj.util.FieldUtils;
import org.minimalj.util.mock.Mocking;

public class Form<T> {
	private static Logger logger = Logger.getLogger(Form.class.getSimpleName());

	public static final boolean EDITABLE = true;
	public static final boolean READ_ONLY = false;

	public static final int DEFAULT_COLUMN_WIDTH = 100;

	protected final boolean editable;

	private final int columns;
	private final FormContent formContent;
	private boolean ignoreCaption;

	private final LinkedHashMap<Property, FormElement<?>> elements = new LinkedHashMap<>();

	private final FormChangeListener formChangeListener = new FormChangeListener();

	private ChangeListener<Form<?>> changeListener;
	private boolean changeFromOutside;

	private final LinkedHashMap<String, List<Property>> dependencies = new LinkedHashMap<>();
	@SuppressWarnings("rawtypes")
	private final LinkedHashMap<Property, LinkedHashMap<Property, PropertyUpdater>> propertyUpdater = new LinkedHashMap<>();

	private final Map<IComponent, Object[]> keysForTitle = new HashMap<>();

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
		Property property;
		boolean forcedReadonly = false;
		if (key instanceof ReadOnlyWrapper) {
			forcedReadonly = true;
			key = ((ReadOnlyWrapper) key).key;
		}
		if (key == null) {
			throw new NullPointerException("Key must not be null");
		} else if (key instanceof Function) {
			return createElement(((Function) key).apply(this.editable && !forcedReadonly));
		} else if (key instanceof FormElement) {
			element = (FormElement<?>) key;
			property = element.getProperty();
			if (property == null)
				throw new IllegalArgumentException(IComponent.class.getSimpleName() + " has no key");
		} else {
			property = Keys.getProperty(key);
			if (property != null) {
				boolean editable = !forcedReadonly && this.editable && !(FieldUtils.isAllowedPrimitive(property.getClazz()) && property.isFinal());
				element = createElement(property, editable);
			}
		}
		return element;
	}

	@SuppressWarnings("rawtypes")
	protected FormElement<?> createElement(Property property, boolean editable) {
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
			return editable ? new SelectionFormElement(property) : new TextFormElement(property);
		} else if (Rendering.class.isAssignableFrom(fieldClass)) {
			return new TextFormElement(property);
		}
		logger.severe("No FormElement could be created for: " + property.getName() + " of class " + fieldClass.getName());
		return new UnknownFormElement(property);
	}

	public void setIgnoreCaption(boolean ignoreCaption) {
		this.ignoreCaption = ignoreCaption;
		formContent.setIgnoreCaption(ignoreCaption);
	}

	//

	public void group(String caption) {
		formContent.group(caption);
	}
	
	public void line(Object... keys) {
		int span = columns / keys.length;
		int rest = columns;
		for (int i = 0; i < keys.length; i++) {
			if (assertColumnCount(i, keys.length)) {
				int elementSpan = span;
				while (i < keys.length - 1 && keys[i + 1] == keys[i]) {
					elementSpan += span;
					i++;
				}
				if (i == keys.length - 1) {
					elementSpan = rest;
				}
				add(keys[i], elementSpan);
				rest = rest - elementSpan;
			}
		}
	}

	private boolean assertColumnCount(int index, int elementCount) {
		if (index >= columns) {
			logger.severe("This form was constructed for " + columns + " column(s) but should be filled with " + elementCount + " form elements");
			logger.fine("The solution is most probably to add/set the correct number of columns when calling the Form constructor");
			if (Configuration.isDevModeActive()) {
				throw new IllegalArgumentException("Not enough columns (" + columns + ") for form elements (" + elementCount + ")");
			}
			return false;
		}
		return true;
	}

	private void add(Object key, int elementSpan) {
		boolean forcedNotEmpty = false;
		if (key instanceof NotEmptyWrapper) {
			forcedNotEmpty = true;
			key = ((NotEmptyWrapper) key).key;
		}
		if (key instanceof String && ((String) key).length() == 0) {
			formContent.add(null, false, null, null, elementSpan);
		} else {
			FormElement<?> element = createElement(key);
			if (element != null) {
				add(element, elementSpan, forcedNotEmpty);
			} else {
				formContent.add(null, false, Frontend.getInstance().createText("" + key), null, elementSpan);
			}
		}
	}

	private void add(FormElement<?> element, int span, boolean forcedNotEmpty) {
		boolean required = editable && element.canBeEmpty() && (forcedNotEmpty || element.getProperty().getAnnotation(NotEmpty.class) != null);
		formContent.add(ignoreCaption ? null : element.getCaption(), required, element.getComponent(), element.getConstraint(), span);
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

	public static Object notEmpty(Object key, boolean notEmpty) {
		return notEmpty ? notEmpty(key) : key;
	}

	/**
	 * Marks this key as must be not empty. This does not activate any validation,
	 * it only shows the red asterisk at the end of the caption.
	 */
	public static Object notEmpty(Object key) {
		NotEmptyWrapper wrapper = new NotEmptyWrapper();
		wrapper.key = key;
		return wrapper;
	}

	private static class NotEmptyWrapper {
		private Object key;
	}

	public static <T extends Code> FormElement<T> noNull(T key) {
		return new CodeFormElement<>(key, ComboBoxFormElement.NO_NULL_STRING);
	}

	public static <T extends Enum<T>> FormElement<T> noNull(T key) {
		return new EnumFormElement<>(key, false);
	}

	public void addTitle(String text, Object... keys) {
		IComponent label = Frontend.getInstance().createTitle(text);
		formContent.add(null, false, label, null, -1);
		if (keys.length > 0) {
			keysForTitle.put(label, keys);
		}
	}

	//

	/**
	 * Declares that if the <i>from</i> property changes all the properties with
	 * <i>to</i> could change. This is normally used if the to <i>to</i> property is
	 * a getter that calculates something that depends on the <i>from</i> in some
	 * way.
	 * 
	 * @param from the key or property of the field triggering the update
	 * @param to   the field possible changed its value implicitly
	 */
	public void addDependecy(Object from, Object... to) {
		Property fromProperty = Keys.getProperty(from);
		List<Property> list = dependencies.computeIfAbsent(fromProperty.getPath(), p -> new ArrayList<>());
		for (Object key : to) {
			list.add(Objects.requireNonNull(Keys.getProperty(key)));
		}
	}

	private void addDependecy(Property fromProperty, Property to) {
		addDependecy(fromProperty.getPath(), to);
	}

	private void addDependecy(String fromPropertyPath, Property to) {
		List<Property> list = dependencies.computeIfAbsent(fromPropertyPath, p -> new ArrayList<>());
		list.add(to);
	}

	/**
	 * Declares that if the key or property <i>from</i> changes the specified
	 * updater should be called and after its return the <i>to</i> key or property
	 * could have changed.
	 * <p>
	 * 
	 * This is used if there is a more complex relation between two fields.
	 * 
	 * @param <FROM>  the type (class) of the fromKey / field
	 * @param <TO>    the type (class) of the toKey / field
	 * @param from    the field triggering the update
	 * @param updater the updater doing the change of the to field
	 * @param to      the changed field by the updater
	 */
	public <FROM, TO> void addDependecy(FROM from, PropertyUpdater<FROM, TO, T> updater, TO to) {
		Property fromProperty = Keys.getProperty(from);
		Property toProperty = Keys.getProperty(to);
		propertyUpdater.computeIfAbsent(fromProperty, p -> new LinkedHashMap<>()).put(toProperty, updater);
		addDependecy(from, to);
	}

	@FunctionalInterface
	public interface PropertyUpdater<FROM, TO, EDIT_OBJECT> {

		/**
		 * 
		 * @param input            The new value of the property that has changed
		 * @param copyOfEditObject The current object of the This reference should
		 *                         <b>not</b> be changed. It should be treated as a read
		 *                         only version or a copy of the object. It's probably
		 *                         not a real copy as it is to expensive to copy the
		 *                         object for every call.
		 * @return The new value the updater wants to set to the toKey property
		 */
		public TO update(FROM input, EDIT_OBJECT copyOfEditObject);

	}

	//

	private void registerNamedElement(FormElement<?> field) {
		if (elements.containsKey(field.getProperty())) {
			throw new IllegalArgumentException("Not allowed to add property twice: " + field.getProperty().getPath());
		}
		elements.put(field.getProperty(), field);
		field.setChangeListener(formChangeListener);
	}

	private void addDependencies(FormElement<?> field) {
		Property property = field.getProperty();
		List<Property> dependencies = Keys.getDependencies(property);
		for (Property dependency : dependencies) {
			addDependecy(dependency, field.getProperty());
		}

		// a.b.c
		String path = property.getPath();
		while (path != null && path.contains(".")) {
			int pos = path.lastIndexOf('.');
			addDependecy(path.substring(0, pos), property);
			path = path.substring(0, pos);
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
			Property property = field.getProperty();
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
	public Collection<Property> getProperties() {
		return elements.keySet();
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void set(Property property, Object value) {
		FormElement element = elements.get(property);
		try {
			element.setValue(value);
		} catch (Exception x) {
			ExceptionUtils.logReducedStackTrace(logger, x);
		}
	}

	private void setValidationMessage(Property property, List<ValidationMessage> validationMessages) {
		FormElement<?> formElement = elements.get(property);
		if (formElement instanceof TableFormElement) {
			// TODO make tableFormElement to Indication
			TableFormElement<?> tableFormElement = (TableFormElement<?>) formElement;
			tableFormElement.setValidationMessages(validationMessages);
		} else if (formElement instanceof Indication) {
			Indication indication = (Indication) formElement;
			List<ValidationMessage> localValidationMessages = new ArrayList<>(validationMessages.size());
			int prefixSize = ChainedProperty.getChain(property).size();
			for (ValidationMessage message : validationMessages) {
				List<Property> chain = ChainedProperty.getChain(message.getProperty());
				chain = chain.subList(prefixSize, chain.size());
				ValidationMessage localValidationMessage = new ValidationMessage(chain.size() > 0 ? ChainedProperty.buildChain(chain) : null, message.getFormattedText());
				localValidationMessages.add(localValidationMessage);
			}
			indication.setValidationMessages(localValidationMessages, formContent);
		} else {
			formContent.setValidationMessages(formElement.getComponent(), validationMessages.stream().map(ValidationMessage::getFormattedText).collect(Collectors.toList()));
		}
	}

	public void setObject(T object) {
		if (editable && changeListener == null)
			throw new IllegalStateException("Listener has to be set on a editable Form");
		if (logger.isLoggable(Level.FINE)) {
			logDependencies();
		}
		changeFromOutside = true;
		this.object = object;
		readValueFromObject();
		changeFromOutside = false;
	}

	private void readValueFromObject() {
		for (Property property : getProperties()) {
			Object propertyValue = property.getValue(object);
			set(property, propertyValue);
		}
		updateEnable();
		updateVisible();
	}

	private void logDependencies() {
		if (editable) {
			logger.fine("Dependencies in " + this.getClass().getSimpleName());
			for (Map.Entry<String, List<Property>> entry : dependencies.entrySet()) {
				logger.fine(entry.getKey() + " -> " + entry.getValue().stream().map(Property::getPath).collect(Collectors.joining(", ")));
			}
		}
	}

	private String getName(FormElement<?> field) {
		Property property = field.getProperty();
		return property.getName();
	}

	public void setChangeListener(ChangeListener<Form<?>> changeListener) {
		this.changeListener = Objects.requireNonNull(changeListener);
	}

	private class FormChangeListener implements ChangeListener<FormElement<?>> {

		@Override
		public void changed(FormElement<?> changedField) {
			if (changeFromOutside)
				return;
			if (changeListener == null) {
				if (editable)
					logger.severe("Editable Form must have a listener");
				return;
			}

			Property property = changedField.getProperty();
			Object newValue = changedField.getValue();
			logger.finer(() -> "ChangeEvent from element: " + getName(changedField) + ", property: " + property.getPath() + ", value: " + newValue);

			HashSet<Property> changedProperties = new HashSet<>();

			setValue(property, newValue, changedProperties);
			logger.finer(() -> "Changed properties: " + changedProperties.stream().map(Property::getPath).collect(Collectors.joining(", ")));

			if (!changedProperties.isEmpty()) {
				// propagate all possible changed values to the form elements
				updateDependingFormElements(changedField, changedProperties);

				// update enable/disable status of the form elements
				updateEnable();
				updateVisible();

				changeListener.changed(Form.this);
			}
		}

		@SuppressWarnings({ "unchecked", "rawtypes" })
		private void updateDependingFormElements(FormElement<?> changedFormElement, HashSet<Property> changedProperties) {
			for (Property changedProperty : changedProperties) {
				for (FormElement formElement : elements.values()) {
					if (formElement == changedFormElement) {
						// don't need to update the FormElement where the change comes from
						continue;
					}
					Property formElementProperty = formElement.getProperty();
					String formElementPath = formElementProperty.getPath();
					String changedPropertyPath = changedProperty.getPath();
					if (formElementPath.equals(changedPropertyPath) || formElementPath.startsWith(changedPropertyPath) && formElementPath.charAt(changedPropertyPath.length()) == '.') {
						Object newValue = formElementProperty.getValue(object);
						formElement.setValue(newValue);
					}
				}
			}
		}

		@SuppressWarnings({ "rawtypes", "unchecked" })
		private void executeUpdater(Property property, Object updaterInput, Object clonedObject, HashSet<Property> changedProperties) {
			if (propertyUpdater.containsKey(property)) {
				Map<Property, PropertyUpdater> updaters = propertyUpdater.get(property);
				for (Map.Entry<Property, PropertyUpdater> entry : updaters.entrySet()) {
					logger.finer(() -> "Update from " + property.getPath() + " to " + entry.getKey().getPath());
					Object updaterOutput = entry.getValue().update(updaterInput, clonedObject);
					setValue(entry.getKey(), updaterOutput, changedProperties);
				}
			}
		}

		private void setValue(Property property, Object newValue, HashSet<Property> changedProperties) {
			Object oldValue = property.getValue(object);
			logger.finest(() -> "Set " + property.getPath() + " to " + newValue + " (previous: " + oldValue + ")");
			if (!EqualsHelper.equals(oldValue, newValue) || newValue instanceof Collection) {
				Object clonedObject = CloneHelper.clone(object); // clone before change!
				property.setValue(object, newValue);
				executeUpdater(property, newValue, clonedObject, changedProperties);
				addChangedPropertyRecursive(property, changedProperties);
			} else if (newValue instanceof Collection) {
				// same instance of Collection can have changed content always assume a change.
				// But not need of setValue .
				Object clonedObject = CloneHelper.clone(object);
				executeUpdater(property, newValue, clonedObject, changedProperties);
				addChangedPropertyRecursive(property, changedProperties);
			}
		}

		private void addChangedPropertyRecursive(Property property, HashSet<Property> changedProperties) {
			changedProperties.add(property);
			HashSet<Property> changedRoots = new HashSet<>(changedProperties);
			changedRoots.forEach(root -> changedProperties.addAll(collectDependencies(root)));
		}

		private HashSet<Property> collectDependencies(Property property) {
			HashSet<Property> collection = new HashSet<>();
			collectDependencies(property, collection);
			return collection;
		}

		private void collectDependencies(Property property, HashSet<Property> collection) {
			if (!collection.contains(property)) {
				collection.add(property);
				if (dependencies.containsKey(property.getPath())) {
					for (Property dependingProperty : dependencies.get(property.getPath())) {
						collectDependencies(dependingProperty, collection);
					}
				}
			}
		}
	}

	private void updateEnable() {
		for (Map.Entry<Property, FormElement<?>> element : elements.entrySet()) {
			Property property = element.getKey();

			boolean enabled = !(property.isFinal() && FieldUtils.isAllowedPrimitive(property.getClazz())) && evaluate(object, property, Enabled.class);

			if (element.getValue() instanceof Enable) {
				((Enable) element.getValue()).setEnabled(enabled);
			} else if (!enabled && !property.isFinal()) {
				if (editable) {
					logger.severe("element " + (element.getValue().getClass().getName()) + " for "  + property.getPath() + " should implement Enable");
				} else {
					logger.fine("element " + (element.getValue().getClass().getName()) + " for "  + property.getPath() + " should maybe implement Enable");
				}
			}
		}
	}

	private void updateVisible() {
		for (Map.Entry<Property, FormElement<?>> element : elements.entrySet()) {
			Property property = element.getKey();
			boolean visible = evaluate(object, property, Visible.class);
			formContent.setVisible(element.getValue().getComponent(), visible);
		}
		for (Map.Entry<IComponent, Object[]> element : keysForTitle.entrySet()) {
			boolean visible = false;
			for (Object key : element.getValue()) {
				if (evaluate(object, Keys.getProperty(key), Visible.class)) {
					visible = true;
					break;
				}
			}
			formContent.setVisible(element.getKey(), visible);
		}
	}

	// TODO move to properties package, write tests
	static boolean evaluate(Object object, Property property, Class<? extends Annotation> annotationClass) {
		for (Property p2 : ChainedProperty.getChain(property)) {
			Annotation annotation = p2.getAnnotation(annotationClass);

			if (annotation != null) {
				// No common class between Enabled and Visible
				String condition = annotation instanceof Enabled ? ((Enabled) annotation).value() : ((Visible) annotation).value();
				if (!evaluateCondition(object, p2, condition)) {
					return false;
				}
			}
			object = p2.getValue(object);
			if (object == null) {
				return true;
			}
		}
		return true;
	}

	static boolean evaluateCondition(Object object, Property property, String methodName) {
		if (Enabled.FALSE.equals(methodName)) {
			return false;
		}
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
		List<ValidationMessage> unused = new ArrayList<>(validationMessages);
		boolean relevantValidationMessage = false;
		for (Property property : getProperties()) {
			List<ValidationMessage> filteredValidationMessages = ValidationMessage.filterValidationMessage(validationMessages, property);
			setValidationMessage(property, filteredValidationMessages);
			relevantValidationMessage |= !filteredValidationMessages.isEmpty();
			unused.removeAll(filteredValidationMessages);
		}
		if (!unused.isEmpty()) {
			for (ValidationMessage unusedMessage : unused) {
				logger.log(Configuration.isDevModeActive() ? Level.WARNING : Level.FINER, "Unused validation message for: " + unusedMessage.getProperty().getPath());
			}
		}
		return relevantValidationMessage;
	}
}