package org.minimalj.frontend.edit.form;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.minimalj.frontend.edit.fields.BigDecimalEditField;
import org.minimalj.frontend.edit.fields.CheckBoxStringField;
import org.minimalj.frontend.edit.fields.EditField;
import org.minimalj.frontend.edit.fields.EditField.EditFieldListener;
import org.minimalj.frontend.edit.fields.Enable;
import org.minimalj.frontend.edit.fields.EnumEditField;
import org.minimalj.frontend.edit.fields.EnumFormField;
import org.minimalj.frontend.edit.fields.EnumSetEditField;
import org.minimalj.frontend.edit.fields.FormField;
import org.minimalj.frontend.edit.fields.IntegerEditField;
import org.minimalj.frontend.edit.fields.LocalDateField;
import org.minimalj.frontend.edit.fields.LocalTimeField;
import org.minimalj.frontend.edit.fields.LongEditField;
import org.minimalj.frontend.edit.fields.NumberFormField;
import org.minimalj.frontend.edit.fields.TextEditField;
import org.minimalj.frontend.edit.fields.TextFormField;
import org.minimalj.frontend.edit.fields.TypeUnknownField;
import org.minimalj.frontend.toolkit.ClientToolkit;
import org.minimalj.frontend.toolkit.ClientToolkit.IComponent;
import org.minimalj.frontend.toolkit.FormContent;
import org.minimalj.frontend.toolkit.TextField;
import org.minimalj.model.EmptyValidator;
import org.minimalj.model.InvalidValues;
import org.minimalj.model.Keys;
import org.minimalj.model.PropertyInterface;
import org.minimalj.model.annotation.AnnotationUtil;
import org.minimalj.model.annotation.Enabled;
import org.minimalj.model.annotation.Required;
import org.minimalj.model.properties.Properties;
import org.minimalj.model.validation.Validatable;
import org.minimalj.model.validation.Validation;
import org.minimalj.model.validation.ValidationMessage;
import org.minimalj.util.CloneHelper;
import org.minimalj.util.DemoEnabled;
import org.minimalj.util.LoggingRuntimeException;
import org.minimalj.util.resources.Resources;
import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalTime;

public class Form<T> implements DemoEnabled {
	private static Logger logger = Logger.getLogger(Form.class.getName());

	protected final boolean editable;
	private final ResourceBundle resourceBundle;
	
	private final int columns;
	private final FormContent formContent;
	
	private final LinkedHashMap<PropertyInterface, FormField<?>> fields = new LinkedHashMap<PropertyInterface, FormField<?>>();
	
	private final FormPanelChangeListener formPanelChangeListener = new FormPanelChangeListener();
	private final FormPanelActionListener formPanelActionListener = new FormPanelActionListener();
	
	private FormChangeListener<T> changeListener;
	private boolean changeFromOutsite;
	private boolean showWarningIfValidationForUnsuedField = true;
	
	private final Map<PropertyInterface, String> propertyValidations = new HashMap<>();

	private final Map<PropertyInterface, List<PropertyInterface>> dependencies = new HashMap<>();
	@SuppressWarnings("rawtypes")
	private final Map<PropertyInterface, Map<PropertyInterface, PropertyUpdater>> propertyUpdater = new HashMap<>();
	
	private T object;

	public Form() {
		this(true);
	}

	public Form(boolean editable) {
		this(editable, 1);
	}

	public Form(int columns) {
		this(true, columns);
	}

	public Form(boolean editable, int columns) {
		this(null, editable, columns);
	}
	
	public Form(ResourceBundle resourceBundle, boolean editable) {
		this(resourceBundle, editable, 1);
	}

	public Form(ResourceBundle resourceBundle, boolean editable, int columns) {
		this.resourceBundle = resourceBundle != null ? resourceBundle : Resources.getResourceBundle();
		this.editable = editable;
		this.columns = columns;
		this.formContent = ClientToolkit.getToolkit().createFormContent(columns, getColumnWidthPercentage());
	}
	
	protected int getColumnWidthPercentage() {
		return 100;
	}

	protected int getAreaHeightPercentage() {
		return 100;
	}
	
	// Methods to create the form

	public FormContent getContent() {
		return formContent;
	}

	public FormField<?> createField(Object key) {
		FormField<?> field;
		PropertyInterface property;
		if (key == null) {
			throw new NullPointerException("Key must not be null");
		} else if (key instanceof FormField) {
			field = (FormField<?>) key;
			property = field.getProperty();
			if (property == null) throw new IllegalArgumentException(IComponent.class.getSimpleName() + " has no key");
		} else {
			property = Keys.getProperty(key);
			// if ths happens for a getter-method there is the special line missing
			if (property == null) throw new IllegalArgumentException("" + key);
			field = createField(property);
		}

		return field;
	}
	
	protected FormField<?> createField(PropertyInterface property) {
		Class<?> fieldClass = property.getFieldClazz();
		if (editable && !property.isFinal()) {
			if (fieldClass == String.class) {
				int size = AnnotationUtil.getSize(property);
				return new TextEditField(property, size);
			} else if (fieldClass == LocalDate.class) {
				return new LocalDateField(property, editable);
			} else if (fieldClass == LocalTime.class) {
				return new LocalTimeField(property, editable);
			} else if (Enum.class.isAssignableFrom(fieldClass)) {
				return new EnumEditField(property);
			} else if (fieldClass == Boolean.class) {
				String checkBoxText = Resources.getObjectFieldName(resourceBundle, property, ".checkBoxText");
				CheckBoxStringField field = new CheckBoxStringField(property, checkBoxText, editable);
				return field;
			} else if (fieldClass == Integer.class) {
				int size = AnnotationUtil.getSize(property);
				boolean negative = AnnotationUtil.isNegative(property);
				return new IntegerEditField(property, size, negative);
			} else if (fieldClass == Long.class) {
				int size = AnnotationUtil.getSize(property);
				boolean negative = AnnotationUtil.isNegative(property);
				return new LongEditField(property, size, negative);
			} else if (fieldClass == BigDecimal.class) {
				int size = AnnotationUtil.getSize(property);
				int decimal = AnnotationUtil.getDecimal(property);
				boolean negative = AnnotationUtil.isNegative(property);
				return new BigDecimalEditField(property, size, decimal, negative);
			} 
		} else {
			if (fieldClass == String.class) return new TextFormField(property);
			else if (fieldClass == LocalDate.class) return new LocalDateField(property, false);
			else if (fieldClass == LocalTime.class) return new LocalTimeField(property, false);
			else if (Enum.class.isAssignableFrom(fieldClass)) return new EnumFormField(property);
			else if (fieldClass == Boolean.class) {
				String checkBoxText = Resources.getObjectFieldName(resourceBundle, property, ".checkBoxText");
				CheckBoxStringField field = new CheckBoxStringField(property, checkBoxText, false);
				return field;
			} 
			else if (fieldClass == Integer.class) return new NumberFormField.IntegerFormField(property);
			else if (fieldClass == Long.class) return new NumberFormField.LongFormField(property);
			else if (fieldClass == BigDecimal.class) return new NumberFormField.BigDecimalFormField(property);
			else if (fieldClass == Set.class) return new EnumSetEditField(property, editable);
			
		}
		logger.severe("No FormField could be created for: " + property.getFieldName() + " of class " + fieldClass.getName());
		return new TypeUnknownField(property);
	}
	
	// 

	public void line(Object key) {
		FormField<?> field = createField(key);
		add(field, columns);
	}
	
//  with this it would be possible to split cells	
//	public void line(Object... keys) {
//		int span = columns / keys.length;
//		int rest = columns;
//		for (int i = 0; i<keys.length; i++) {
//			Object key = keys[i];
//			if (key instanceof Object[]) {
//				Object[] split = (Object[]) key;
//				IComponent[] components = new IComponent[split.length];
//				int index = 0;
//				for (Object o : split) {
//					FormField<?> field = createField(o);
//					registerNamedField(field);
//					components[index++] = decorateWithCaption(field);
//				}
//				HorizontalLayout horizontalLayout = ClientToolkit.getToolkit().createHorizontalLayout(components);
//				layout.add(horizontalLayout, i < keys.length - 1 ? span : rest);
//			} else {
//				FormField<?> field = createField(key);
//				add(field, i < keys.length - 1 ? span : rest);
//			}
//			rest = rest - span;
//		}
//	}
	
	public void line(Object... keys) {
		if (keys.length > columns) throw new IllegalArgumentException("More keys than specified in the constructor");
		int span = columns / keys.length;
		int rest = columns;
		for (int i = 0; i<keys.length; i++) {
			Object key = keys[i];
			FormField<?> field = createField(key);
			add(field, i < keys.length - 1 ? span : rest);
			rest = rest - span;
		}
	}
	
	private void add(FormField<?> field, int span) {
		String captionText = caption(field);
		formContent.add(captionText, field.getComponent(), span);
		registerNamedField(field);
	}
	
	// 

	public void text(String text) {
		IComponent label = ClientToolkit.getToolkit().createLabel(text);
		formContent.add(label);
	}

	public void addTitle(String text) {
		IComponent label = ClientToolkit.getToolkit().createTitle(text);
		formContent.add(label);
	}

	//

	/**
	 * Declares that if the property with fromKey changes all
	 * the properties with toKey could change. This is normally used
	 * if the to property is a getter that calculates something that
	 * depends on the fromKey in a simple way.
	 * 
	 * @param fromKey
	 * @param toKey
	 */
	public void addDependecy(Object fromKey, Object... toKey) {
		PropertyInterface fromProperty = Keys.getProperty(fromKey);
		if (!dependencies.containsKey(fromProperty)) {
			dependencies.put(fromProperty, new ArrayList<PropertyInterface>());
		}
		List<PropertyInterface> list = dependencies.get(fromProperty);
		for (Object key : toKey) {
			list.add(Keys.getProperty(key));
		}
	}

	/**
	 * Declares that if the property with fromKey changes the specified
	 * updater should be called and after its return the toKey property
	 * could have changed.<p>
	 * 
	 * This is used if there is a more complex relation between two properities.
	 * 
	 * @param fromKey
	 * @param updater
	 * @param toKey
	 */
	@SuppressWarnings("rawtypes")
	public <FROM, TO> void addDependecy(FROM fromKey, PropertyUpdater<FROM, TO, T> updater, TO toKey) {
		PropertyInterface fromProperty = Keys.getProperty(fromKey);
		if (!propertyUpdater.containsKey(fromProperty)) {
			propertyUpdater.put(fromProperty, new HashMap<PropertyInterface, PropertyUpdater>());
		}
		PropertyInterface toProperty = Keys.getProperty(toKey);
		propertyUpdater.get(fromProperty).put(toProperty, updater);
		addDependecy(fromKey, toKey);
	}

	/**
	 * 
	 * @param <FROM> The new value of the property that has changed
	 * @param <EDIT_OBJECT> The current object of the  This reference should <b>not</b> be changed.
	 * It should be treated as a read only version or a copy of the object.
	 * It's probably not a real copy as it is to expensive to copy the object for every call. 
	 * @return <TO> The new value the updater wants to set to the toKey property
	 */
	public interface PropertyUpdater<FROM, TO, EDIT_OBJECT> {
		public TO update(FROM input, EDIT_OBJECT copyOfEditObject);
	}

	//

	private void registerNamedField(FormField<?> field) {
		fields.put(field.getProperty(), field);
		if (field instanceof EditField<?>) {
			EditField<?> editField = (EditField<?>) field;
			editField.setChangeListener(formPanelChangeListener);
		}
		if (field.getComponent() instanceof TextField) {
			TextField textField = (TextField) field.getComponent();
			textField.setCommitListener(formPanelActionListener);
		}
	}

	@Override
	public final void fillWithDemoData() {
		changeFromOutsite = true;
		fillWithDemoData(object);
		readValueFromObject();
		changeListener.changed();
		changeFromOutsite = false;
	}

	protected void fillWithDemoData(T object) {
		for (FormField field : fields.values()) {
			PropertyInterface property = field.getProperty();
			if (field instanceof DemoEnabled) {
				DemoEnabled demoEnabledField = (DemoEnabled) field;
				demoEnabledField.fillWithDemoData();
				property.setValue(object, ((EditField<?>) field).getObject());
			}
		}		
	}
	
	//

	private String caption(FormField<?> field) {
		return Resources.getObjectFieldName(resourceBundle, field.getProperty());
	}

	//
	
	/**
	 * 
	 * @return Collection provided by a LinkedHashMap so it will be a ordered set
	 */
	private Collection<PropertyInterface> getProperties() {
		return fields.keySet();
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void set(PropertyInterface property, Object value) {
		FormField formField = fields.get(property);
		try {
			formField.setObject(value);
		} catch (Exception x) {
			throw new LoggingRuntimeException(x, logger, "Failed to set " + property.getFieldPath());
		}
	}

	private void setValidationMessage(PropertyInterface property, List<String> validationMessages) {
		FormField<?> field = fields.get(property);
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
			updatePropertyValidation(property, propertyValue);
		}
		updateEnable();
		updateValidation();
	}
	
	private String getName(FormField<?> field) {
		PropertyInterface property = field.getProperty();
		return property.getFieldName();
	}
	
	public void setChangeListener(FormChangeListener<T> changeListener) {
		if (changeListener == null) throw new IllegalArgumentException("Listener on Form must not be null");
		if (this.changeListener != null) throw new IllegalStateException("Listener on Form cannot be changed");
		this.changeListener = changeListener;
	}
	
	public interface FormChangeListener<S> {

		public void validate(S object, List<ValidationMessage> validationResult);

		public void indicate(List<ValidationMessage> validationMessages, boolean allUsedFieldsValid);

		public void changed();

		public void commit();

	}

	private class FormPanelChangeListener implements EditFieldListener {

		@Override
		public void changed(EditField changedField) {
			if (changeFromOutsite) return;
			if (changeListener == null) {
				logger.severe("Editable Form must have a listener");
				return;
			}
			
			logger.fine("ChangeEvent from " + getName(changedField));
			
			PropertyInterface property = changedField.getProperty();
			Object newValue = changedField.getObject();

			// Call updaters before set the new value  (so they also can read the old value)
			executeUpdater(property, newValue);
			refreshDependendFields(property);
			
			property.setValue(object, newValue);
			
			// update enable/disable fields
			updateEnable();
			
			updatePropertyValidation(property, newValue);
			updateValidation();

			changeListener.changed();
		}


		@SuppressWarnings({ "unchecked", "rawtypes" })
		private void refreshDependendFields(PropertyInterface property) {
			if (dependencies.containsKey(property)) {
				List<PropertyInterface> dependendProperties = dependencies.get(property);
				for (PropertyInterface dependendProperty : dependendProperties) {
					Object newDependedValue = dependendProperty.getValue(object);
					((FormField) fields.get(dependendProperty)).setObject(newDependedValue);
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
		for (Map.Entry<PropertyInterface, FormField<?>> field : fields.entrySet()) {
			PropertyInterface property = field.getKey();
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
					if (field.getValue() instanceof Enable) {
						((Enable) field.getValue()).setEnabled(e ^ invert);
					} else {
						if (editable) {
							logger.severe("field " + property.getFieldPath() + " should implement Enable");
						} else {
							logger.fine("field " + property.getFieldPath() + " should maybe implement Enable");
						}
					}
				} catch (Exception x) {
					String fieldName = property.getFieldName();
					if (!fieldName.equals(property.getFieldPath())) {
						fieldName += " (" + property.getFieldPath() + ")";
					}
					logger.log(Level.SEVERE, "Update enable of " + fieldName + " failed" , x);
				}
			}
		}
	}
	
	private Object findParentObject(PropertyInterface property) {
		Object result = object;
		String fieldPath = property.getFieldPath();
		while (fieldPath.indexOf(".") > -1) {
			int pos = property.getFieldPath().indexOf(".");
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

	// Validation
	
	private void updateValidation() {
		List<ValidationMessage> validationMessages = new ArrayList<>();
		if (object instanceof Validation) {
			((Validation) object).validate(validationMessages);
		}
		for (Map.Entry<PropertyInterface, String> entry : propertyValidations.entrySet()) {
			validationMessages.add(new ValidationMessage(entry.getKey(), entry.getValue()));
		}
		validateForEmpty(validationMessages);
		validateForInvalid(validationMessages);
		if (changeListener != null) {
			changeListener.validate(object, validationMessages);
		}
		indicate(validationMessages);
	}

	private void validateForEmpty(List<ValidationMessage> validationMessages) {
		for (PropertyInterface property : getProperties()) {
			if (property.getAnnotation(Required.class) != null) {
				EmptyValidator.validate(validationMessages, object, property);
			}
		}
	}

	private void validateForInvalid(List<ValidationMessage> validationMessages) {
		for (PropertyInterface property : getProperties()) {
			Object value = property.getValue(object);
			if (InvalidValues.isInvalid(value)) {
				String caption = Resources.getObjectFieldName(Resources.getResourceBundle(), property);
				validationMessages.add(new ValidationMessage(property, caption + " ungültig"));
			}
		}
	}

	private void updatePropertyValidation(PropertyInterface property, Object value) {
		propertyValidations.remove(property);
		if (value instanceof Validatable) {
			String validationMessage = ((Validatable) value).validate();
			if (validationMessage != null) {
				propertyValidations.put(property, validationMessage);
			}
		}
	}

	private void indicate(List<ValidationMessage> validationMessages) {
		for (PropertyInterface property : getProperties()) {
			List<String> filteredValidationMessages = ValidationMessage.filterValidationMessage(validationMessages, property);
			setValidationMessage(property, filteredValidationMessages);
		}
		
		if (changeListener != null) {
			changeListener.indicate(validationMessages, allUsedFieldsValid(validationMessages));
		}
		
	}
	
	private boolean allUsedFieldsValid(List<ValidationMessage> validationMessages) {
		for (ValidationMessage validationMessage : validationMessages) {
			if (getProperties().contains(validationMessage.getProperty())) {
				return false;
			} else {
				if (showWarningIfValidationForUnsuedField) {
					logger.warning("There is a validation message for " + validationMessage.getProperty().getFieldName() + " but the field is not used in the form");
					logger.warning("The message is: " + validationMessage.getFormattedText());
					logger.fine("This can be ok if at some point not all validations in a object have to be ok");
					logger.fine("But you have to make sure to get valid data in database");
					logger.fine("You can avoid these warnings if you set showWarningIfValidationForUnsuedField to false");
				}
			}
		}
		return true;
	}

	public void setShowWarningIfValidationForUnsuedField(boolean b) {
		this.showWarningIfValidationForUnsuedField = b;
	}

}