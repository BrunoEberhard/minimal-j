package ch.openech.mj.edit.form;

import java.awt.event.KeyListener;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Logger;

import javax.swing.Action;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.joda.time.LocalDate;

import ch.openech.mj.autofill.DemoEnabled;
import ch.openech.mj.db.model.Constants;
import ch.openech.mj.db.model.PropertyInterface;
import ch.openech.mj.edit.ChangeableValue;
import ch.openech.mj.edit.fields.BigDecimalEditField;
import ch.openech.mj.edit.fields.CheckBoxStringField;
import ch.openech.mj.edit.fields.CodeEditField;
import ch.openech.mj.edit.fields.CodeFormField;
import ch.openech.mj.edit.fields.DateField;
import ch.openech.mj.edit.fields.EditField;
import ch.openech.mj.edit.fields.FormField;
import ch.openech.mj.edit.fields.IntegerEditField;
import ch.openech.mj.edit.fields.NumberFormField;
import ch.openech.mj.edit.fields.TextEditField;
import ch.openech.mj.edit.fields.TextFormField;
import ch.openech.mj.edit.fields.TextFormatField;
import ch.openech.mj.model.annotation.AnnotationUtil;
import ch.openech.mj.model.annotation.PartialDate;
import ch.openech.mj.model.annotation.StringLimitation;
import ch.openech.mj.resources.Resources;
import ch.openech.mj.toolkit.Caption;
import ch.openech.mj.toolkit.ClientToolkit;
import ch.openech.mj.toolkit.GridFormLayout;
import ch.openech.mj.toolkit.IComponent;

public class Form<T> implements IForm<T>, DemoEnabled {
	private static Logger logger = Logger.getLogger(Form.class.getName());

	protected final boolean editable;
	private final ResourceBundle resourceBundle;
	
	private final int columns;
	private final GridFormLayout layout;
	
	private final LinkedHashMap<PropertyInterface, FormField<?>> fields = new LinkedHashMap<PropertyInterface, FormField<?>>();
	private final Map<PropertyInterface, Caption> indicators = new HashMap<PropertyInterface, Caption>();
	
	private KeyListener keyListener;
	private final FormPanelChangeListener formPanelChangeListener = new FormPanelChangeListener();
	
	private ChangeListener changeListener;
	private Action saveAction;
	
	private boolean resizable = false;

	protected Form() {
		this(true);
	}

	protected Form(boolean editable) {
		this(editable, 1);
	}
	
	protected Form(boolean editable, int columns) {
		this(null, null, editable, columns);
	}
	
	public Form(Class<T> objectClass, ResourceBundle resourceBundle, boolean editable) {
		this(objectClass, resourceBundle, editable, 1);
	}

	public Form(Class<T> objectClass, ResourceBundle resourceBundle, boolean editable, int columns) {
		this.resourceBundle = resourceBundle != null ? resourceBundle : Resources.getResourceBundle();
		this.editable = editable;
		this.columns = columns;
		this.layout = ClientToolkit.getToolkit().createGridLayout(columns, getColumnWidthPercentage());
	}
	
	protected int getColumnWidthPercentage() {
		return 100;
	}

	protected int getAreaHeightPercentage() {
		return 100;
	}
	
	// Methods to create the form

	@Override
	public IComponent getComponent() {
		return layout;
	}

	@Override
	public void setSaveAction(Action saveAction) {
		// TODO sollte noch für ctrl - S verwendet werden.
		this.saveAction = saveAction;
	}

	public FormField<?> createField(Object key) {
		FormField<?> field;
		if (key == null) {
			throw new NullPointerException("Key must not be null");
		} else if (key instanceof FormField) {
			field = (FormField<?>) key;
			if (field.getProperty() == null) {
				throw new IllegalArgumentException(IComponent.class.getSimpleName() + " has no key");
			}
		} else if (key instanceof StringLimitation) {
			PropertyInterface property = Constants.getProperty(key);
			field = createTextFormatField((StringLimitation) key, property);
		} else {
			PropertyInterface property = Constants.getProperty(key);
			if (property != null) {
				field = createField(property);
			} else {
				throw new IllegalArgumentException("" + key);
			}
		}
		return field;
	}

	protected FormField<?> createTextFormatField(StringLimitation textFormat, PropertyInterface property) {
		return new TextFormatField(property, textFormat, editable);
	}
	
	protected FormField<?> createField(PropertyInterface property) {
		Class<?> fieldClass = property.getFieldClazz();
		if (editable) {
			if (fieldClass == String.class) {
				int size = AnnotationUtil.getSize(property);
				return new TextEditField(property, size);
			} else if (fieldClass == LocalDate.class) {
				boolean partialAllowed = property.getAnnotation(PartialDate.class) != null;
				return new DateField(property, partialAllowed, editable);
			} else if (Enum.class.isAssignableFrom(fieldClass)) {
				return new CodeEditField(property);
			} else if (fieldClass == Boolean.class) {
				String checkBoxText = Resources.getObjectFieldName(resourceBundle, property, ".checkBoxText");
				CheckBoxStringField field = new CheckBoxStringField(property, checkBoxText, editable);
				return field;
			} else if (fieldClass == Integer.class) {
				int size = AnnotationUtil.getSize(property);
				boolean negative = AnnotationUtil.isNegative(property);
				return new IntegerEditField(property, size, negative);
			} else if (fieldClass == BigDecimal.class) {
				int size = AnnotationUtil.getSize(property);
				int decimal = AnnotationUtil.getDecimal(property);
				boolean negative = AnnotationUtil.isNegative(property);
				return new BigDecimalEditField(property, size, negative);
			} 	// TODO dates
			
		} else {
			if (fieldClass == String.class) return new TextFormField(property);
			else if (fieldClass == LocalDate.class) return new DateField(property, true, editable);
			else if (Enum.class.isAssignableFrom(fieldClass)) return new CodeFormField(property);
			else if (fieldClass == Boolean.class) {
				String checkBoxText = Resources.getObjectFieldName(resourceBundle, property, ".checkBoxText");
				CheckBoxStringField field = new CheckBoxStringField(property, checkBoxText, editable);
				return field;
			} 
			else if (fieldClass == Integer.class) return new NumberFormField.IntegerFormField(property);
			else if (fieldClass == BigDecimal.class) return new NumberFormField.BigDecimalFormField(property);
			// TODO dates
			
		}
		throw new IllegalArgumentException("Unknown Field: " + property.getFieldName());
	}
	
	// 

	public void line(Object key) {
		FormField<?> visual = createField(key);
		add(key, visual, columns);
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
//					FormField<?> visual = createField(o);
//					registerNamedField(visual);
//					components[index++] = decorateWithCaption(visual);
//				}
//				HorizontalLayout horizontalLayout = ClientToolkit.getToolkit().createHorizontalLayout(components);
//				layout.add(horizontalLayout, i < keys.length - 1 ? span : rest);
//			} else {
//				FormField<?> visual = createField(key);
//				add(visual, i < keys.length - 1 ? span : rest);
//			}
//			rest = rest - span;
//		}
//	}
	
	public void line(Object... keys) {
		int span = columns / keys.length;
		int rest = columns;
		for (int i = 0; i<keys.length; i++) {
			Object key = keys[i];
			FormField<?> visual = createField(key);
			add(key, visual, i < keys.length - 1 ? span : rest);
			rest = rest - span;
		}
	}
	
	private void add(Object key, FormField<?> c, int span) {
		layout.add(decorateWithCaption(c).getComponent(), span);
		registerNamedField(c);
	}
	
	// 

	public void area(Object... keys) {
		int span = columns / keys.length;
		int rest = columns;
		for (int i = 0; i<keys.length; i++) {
			Object key = keys[i];
			FormField<?> visual = createField(key);
			area(key, visual, i < keys.length - 1 ? span : rest);
			rest = rest - span;
		}
	}

	private void area(Object key, FormField<?> visual, int span) {
		layout.addArea(decorateWithCaption(visual).getComponent(), span);
		registerNamedField(visual);
		resizable = true;
	}
	
	private Caption decorateWithCaption(FormField<?> visual) {
		String captionText = caption(visual);
		Caption captioned = ClientToolkit.getToolkit().decorateWithCaption(visual.getComponent(), captionText);
		indicators.put(visual.getProperty(), captioned);
		return captioned;
	}
	
	//

	public void text(String text) {
		text(text, columns);
	}
	
	public void text(String text, int span) {
		IComponent label = ClientToolkit.getToolkit().createLabel(text);
		layout.add(label, span);
	}

	public void addTitle(String text) {
		IComponent label = ClientToolkit.getToolkit().createTitle(text);
		layout.add(label, columns);
	}
	
	//
	
	protected FormField<?> getField(Object key) {
		return fields.get(Constants.getProperty(key));
	}
	
	//

	protected void registerNamedField(FormField<?> field) {
		fields.put(field.getProperty(), field);
		if (field instanceof ChangeableValue<?>) {
			ChangeableValue<?> changeable = (ChangeableValue<?>) field;
			changeable.setChangeListener(formPanelChangeListener);
		}
//		addListeners(field);
		
	}

	@Override
	public void fillWithDemoData() {
		for (FormField<?> field : fields.values()) {
			if (field instanceof DemoEnabled) {
				DemoEnabled demoEnabledField = (DemoEnabled) field;
				demoEnabledField.fillWithDemoData();
			}
		}
	}

	//

	protected String caption(FormField<?> field) {
		return Resources.getObjectFieldName(resourceBundle, field.getProperty());
	}

	//
	
	/**
	 * 
	 * @return Collection provided by a LinkedHashMap so it will be a ordered set
	 */
	public Collection<PropertyInterface> getProperties() {
		return fields.keySet();
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void set(PropertyInterface property, Object value) {
		FormField formField = fields.get(property);
		formField.setObject(value);
	}

	public void setValidationMessage(PropertyInterface property, List<String> validationMessages) {
		indicators.get(property).setValidationMessages(validationMessages);
	}

	@Override
	public void setObject(Object object) {
		for (PropertyInterface property : getProperties()) {
			Object propertyValue = property.getValue(object);
			set(property, propertyValue);
		}
	}
	
	@Override
	public boolean isResizable() {
		return resizable;
	}

	private String getName(FormField<?> field) {
		PropertyInterface property = field.getProperty();
		return property.getFieldName();
	}
	
	// Changeable
	
	@Override
	public void setChangeListener(ChangeListener changeListener) {
		this.changeListener = changeListener;
	}

	private class FormPanelChangeListener implements ChangeListener {
		private boolean adjusting = false;
		
		public void setAdjusting(boolean adjusting) {
			this.adjusting = adjusting;
		}

		@Override
		public void stateChanged(ChangeEvent event) {
			if (adjusting) return;
			
			EditField<?> changedField = (EditField<?>) event.getSource();
			logger.fine("ChangeEvent from " + getName(changedField));

			PropertyInterface property = changedField.getProperty();
			Object value = changedField.getObject();
			
			forwardToDependingFields(changedField);

			if (changeListener != null) {
				FormChangeEvent formChangeEvent = new FormChangeEvent(this, property, value);
				changeListener.stateChanged(formChangeEvent);
			}
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void forwardToDependingFields(EditField<?> changedField) {
		PropertyInterface propertyChangedField = null;
		for (Map.Entry<PropertyInterface, FormField<?>> entry : fields.entrySet()) {
			FormField field = entry.getValue();
			if (field == changedField) {
				propertyChangedField = entry.getKey();
				continue;
			}
			if (propertyChangedField == null) {
				// only fields below the changed field can be depending.
				// know what you do before remove this line!
				continue;
			}
			if (field instanceof DependingOnFieldAbove) {
				DependingOnFieldAbove dependingOnFieldAbove = (DependingOnFieldAbove) field;
				if (propertyChangedField.equals(Constants.getProperty(dependingOnFieldAbove.getKeyOfDependedField()))) {
					try {
						dependingOnFieldAbove.valueChanged(changedField.getObject());
					} catch (Exception x) {
						logger.severe("Could not forward value from " + getName(changedField) + " to " + getName(field) + " (" + x.getLocalizedMessage() + ")");
					}
				}
			}
		}
	}
	
	public static class FormChangeEvent extends ChangeEvent {
		
		private final PropertyInterface property;
		private final Object value;
		
		public FormChangeEvent(Object source, PropertyInterface property, Object value) {
			super(source);
			this.property = property;
			this.value = value;
		}

		public PropertyInterface getProperty() {
			return property;
		}

		public Object getValue() {
			return value;
		}
	}
	

}