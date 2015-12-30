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
import org.minimalj.frontend.form.element.FormElement.FormElementListener;
import org.minimalj.frontend.form.element.IntegerFormElement;
import org.minimalj.frontend.form.element.LocalDateFormElement;
import org.minimalj.frontend.form.element.LocalDateTimeFormElement;
import org.minimalj.frontend.form.element.LocalTimeFormElement;
import org.minimalj.frontend.form.element.LongFormElement;
import org.minimalj.frontend.form.element.StringFormElement;
import org.minimalj.frontend.form.element.TextFormElement;
import org.minimalj.frontend.form.element.TypeUnknownFormElement;
import org.minimalj.model.Code;
import org.minimalj.model.Keys;
import org.minimalj.model.annotation.Enabled;
import org.minimalj.model.properties.Properties;
import org.minimalj.model.properties.PropertyInterface;
import org.minimalj.model.validation.ValidationMessage;
import org.minimalj.util.CloneHelper;
import org.minimalj.util.ExceptionUtils;
import org.minimalj.util.mock.Mocking;
import org.minimalj.util.resources.Resources;

public class Form<T> {
	private static Logger logger = Logger.getLogger(Form.class.getSimpleName().toUpperCase());

	public static final boolean EDITABLE = true;
	public static final boolean READ_ONLY = false;
	
	protected final boolean editable;
	
	private final int columns;
	private final FormContent formContent;
	
	private final LinkedHashMap<PropertyInterface, FormElement<?>> elements = new LinkedHashMap<PropertyInterface, FormElement<?>>();
	
	private final FormPanelChangeListener formPanelChangeListener = new FormPanelChangeListener();
	
	private FormChangeListener<T> changeListener;
	private boolean changeFromOutsite;

	private final Map<PropertyInterface, List<PropertyInterface>> dependencies = new HashMap<>();
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
		this.editable = editable;
		this.columns = columns;
		this.formContent = Frontend.getInstance().createFormContent(columns, getColumnWidthPercentage());
	}
	
	protected int getColumnWidthPercentage() {
		return 100;
	}

	// Methods to create the form

	public FormContent getContent() {
		return formContent;
	}

	public FormElement<?> createElement(Object key) {
		FormElement<?> element;
		PropertyInterface property;
		if (key == null) {
			throw new NullPointerException("Key must not be null");
		} else if (key instanceof FormElement) {
			element = (FormElement<?>) key;
			property = element.getProperty();
			if (property == null) throw new IllegalArgumentException(IComponent.class.getSimpleName() + " has no key");
		} else {
			property = Keys.getProperty(key);
			// if ths happens for a getter-method there is the special line missing
			if (property == null) throw new IllegalArgumentException("" + key);
			element = createElement(property);
		}

		return element;
	}
	
	@SuppressWarnings("rawtypes")
	protected FormElement<?> createElement(PropertyInterface property) {
		Class<?> fieldClass = property.getClazz();
		
		boolean editable = this.editable && !property.isFinal();

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
			return new EnumSetFormElement(property, this.editable); // 'this.editable' instead 'editable': the set field is always final. That doesn't mean its read only.
		}	
		logger.severe("No FormElement could be created for: " + property.getName() + " of class " + fieldClass.getName());
		return new TypeUnknownFormElement(property);
	}
	
	// 

	public void line(Object key) {
		FormElement<?> element = createElement(key);
		add(element, columns);
	}
	
	public void line(Object... keys) {
		if (keys.length > columns) {
			logger.severe("This form was constructed for " + columns + " column(s) but should be filled with " + keys.length + " form elements");
			logger.fine("The solution is most probably to add/set the correct number of columns when calling the Form constructor");
			throw new IllegalArgumentException("Not enough columns (" + columns + ") for form elements (" + keys.length + ")");
		}
		int span = columns / keys.length;
		int rest = columns;
		for (int i = 0; i<keys.length; i++) {
			Object key = keys[i];
			FormElement<?> element = createElement(key);
			add(element, i < keys.length - 1 ? span : rest);
			rest = rest - span;
		}
	}
	
	/**
	 * Use with care. Validation messages cannot be displayed without caption.
	 * At the moment this method is only meant to be used for the selection
	 * of elements in a Set of Enum.
	 * 
	 * @param key field that should be in the form without caption
	 */
	public void lineWithoutCaption(Object key) {
		FormElement<?> element = createElement(key);
		formContent.add(element.getComponent());
		registerNamedElement(element);
	}
	
	private void add(FormElement<?> element, int span) {
		String captionText = caption(element);
		formContent.add(captionText, element.getComponent(), span);
		registerNamedElement(element);
	}
	
	// 

	public void text(String text) {
		IComponent label = Frontend.getInstance().createText(text);
		formContent.add(label);
	}

	public void addTitle(String text) {
		IComponent label = Frontend.getInstance().createTitle(text);
		formContent.add(label);
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
		if (!dependencies.containsKey(fromProperty)) {
			dependencies.put(fromProperty, new ArrayList<PropertyInterface>());
		}
		List<PropertyInterface> list = dependencies.get(fromProperty);
		for (Object key : to) {
			list.add(Keys.getProperty(key));
		}
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
	 * @param to the changed field by the udpater
	 */
	@SuppressWarnings("rawtypes")
	public <FROM, TO> void addDependecy(FROM from, PropertyUpdater<FROM, TO, T> updater, TO to) {
		PropertyInterface fromProperty = Keys.getProperty(from);
		if (!propertyUpdater.containsKey(fromProperty)) {
			propertyUpdater.put(fromProperty, new HashMap<PropertyInterface, PropertyUpdater>());
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
		field.setChangeListener(formPanelChangeListener);
	}

	public final void mock() {
		changeFromOutsite = true;
		try {
			fillWithDemoData(object);
		} catch (Exception x) {
			logger.log(Level.SEVERE, "Fill with demo data failed", x);
		} finally {
			readValueFromObject();
			changeFromOutsite = false;
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

	protected String caption(FormElement<?> field) {
		return Resources.getPropertyName(field.getProperty());
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
		changeFromOutsite = true;
		this.object = object;
		readValueFromObject();
		changeFromOutsite = false;
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
	
	public void setChangeListener(FormChangeListener<T> changeListener) {
		if (changeListener == null) throw new IllegalArgumentException("Listener on Form must not be null");
		if (this.changeListener != null) throw new IllegalStateException("Listener on Form cannot be changed");
		this.changeListener = changeListener;
	}
	
	public interface FormChangeListener<S> {

		public void changed(PropertyInterface property, Object newValue);

		public void commit();

	}

	private class FormPanelChangeListener implements FormElementListener {

		@Override
		public void valueChanged(FormElement<?> changedField) {
			if (changeFromOutsite) return;
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
			
			changeListener.changed(property, newValue);
		}


		@SuppressWarnings({ "unchecked", "rawtypes" })
		private void refreshDependendFields(PropertyInterface property) {
			if (dependencies.containsKey(property)) {
				List<PropertyInterface> dependendProperties = dependencies.get(property);
				for (PropertyInterface dependendProperty : dependendProperties) {
					Object newDependedValue = dependendProperty.getValue(object);
					((FormElement) elements.get(dependendProperty)).setValue(newDependedValue);
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
			Enabled enabled = property.getAnnotation(Enabled.class);
			if (enabled != null) {
				String methodName = enabled.value();
				boolean invert = methodName.startsWith("!");
				if (invert) methodName = methodName.substring(1);
				try {
					Object o = findParentObject(property);
					Class<?> clazz = o.getClass();
					Method method = clazz.getMethod(methodName);
					boolean e = (Boolean) method.invoke(o);
					if (element.getValue() instanceof Enable) {
						((Enable) element.getValue()).setEnabled(e ^ invert);
					} else {
						if (editable) {
							logger.severe("element " + property.getPath() + " should implement Enable");
						} else {
							logger.fine("element " + property.getPath() + " should maybe implement Enable");
						}
					}
				} catch (Exception x) {
					String fieldName = property.getName();
					if (!fieldName.equals(property.getPath())) {
						fieldName += " (" + property.getPath() + ")";
					}
					logger.log(Level.SEVERE, "Update enable of " + fieldName + " failed" , x);
				}
			}
		}
	}
	
	public void indicate(List<ValidationMessage> validationMessages) {
		for (PropertyInterface property : getProperties()) {
			List<String> filteredValidationMessages = ValidationMessage.filterValidationMessage(validationMessages, property);
			setValidationMessage(property, filteredValidationMessages);
		}
	}

	private Object findParentObject(PropertyInterface property) {
		Object result = object;
		String fieldPath = property.getPath();
		while (fieldPath.indexOf(".") > -1) {
			int pos = property.getPath().indexOf(".");
			PropertyInterface p2 = Properties.getProperty(result.getClass(), fieldPath.substring(0, pos));
			result = p2.getValue(result);
			fieldPath = fieldPath.substring(pos + 1);
		}
		return result;
	}

	public class FormPanelActionListener implements Runnable {
		
		@Override
		public void run() {
			if (changeListener != null) {
				changeListener.commit();
			}
		}

	}


}