package ch.openech.mj.edit.form;

import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.Action;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import ch.openech.mj.autofill.DemoEnabled;
import ch.openech.mj.db.model.AccessorInterface;
import ch.openech.mj.db.model.BooleanFormat;
import ch.openech.mj.db.model.Code;
import ch.openech.mj.db.model.Constants;
import ch.openech.mj.db.model.DateFormat;
import ch.openech.mj.db.model.Format;
import ch.openech.mj.db.model.Formats;
import ch.openech.mj.db.model.IntegerFormat;
import ch.openech.mj.db.model.PlainFormat;
import ch.openech.mj.edit.ChangeableValue;
import ch.openech.mj.edit.fields.CheckBoxField;
import ch.openech.mj.edit.fields.CheckBoxStringField;
import ch.openech.mj.edit.fields.CodeEditField;
import ch.openech.mj.edit.fields.DateField;
import ch.openech.mj.edit.fields.EditField;
import ch.openech.mj.edit.fields.FormField;
import ch.openech.mj.edit.fields.NumberEditField;
import ch.openech.mj.edit.fields.TextEditField;
import ch.openech.mj.edit.fields.TextFormField;
import ch.openech.mj.edit.fields.TypeUnknownField;
import ch.openech.mj.edit.validation.Indicator;
import ch.openech.mj.edit.validation.Validatable;
import ch.openech.mj.edit.validation.ValidationMessage;
import ch.openech.mj.edit.value.PropertyAccessor;
import ch.openech.mj.edit.value.Required;
import ch.openech.mj.resources.Resources;
import ch.openech.mj.toolkit.ClientToolkit;
import ch.openech.mj.toolkit.GridFormLayout;
import ch.openech.mj.toolkit.IComponent;
import ch.openech.mj.toolkit.IComponentDelegate;
import ch.openech.mj.util.GenericUtils;
import ch.openech.mj.util.StringUtils;

public class AbstractFormVisual<T> implements IComponentDelegate, FormVisual<T>, DemoEnabled {
	private static Logger logger = Logger.getLogger(AbstractFormVisual.class.getName());

	protected final boolean editable;
	private final ResourceBundle resourceBundle;
	
	private final int columns;
	private final GridFormLayout layout;
	
	private final List<FormField<?>> fields = new ArrayList<FormField<?>>();
	private final List<EditField<?>> mandatoryFields = new ArrayList<EditField<?>>();

	private KeyListener keyListener;
	private final ChangeListener formPanelChangeListener = new FormPanelChangeListener();
	
	private ChangeListener changeListener;
	private Action saveAction;
	
	private T object;
	private final Class<T> objectClass;
	private boolean resizable = false;

	protected AbstractFormVisual() {
		this(true);
	}

	protected AbstractFormVisual(boolean editable) {
		this(editable, 1);
	}
	
	protected AbstractFormVisual(boolean editable, int columns) {
		this(null, Resources.getResourceBundle(), editable, columns);
	}
	
	public AbstractFormVisual(Class<T> objectClass, ResourceBundle resourceBundle, boolean editable) {
		this(objectClass, resourceBundle, editable, 1);
	}

	public AbstractFormVisual(Class<T> objectClass, ResourceBundle resourceBundle, boolean editable, int columns) {
		this.objectClass = objectClass != null ? objectClass : getObjectOfFormClass();
		this.resourceBundle = resourceBundle;
		this.editable = editable;
		this.columns = columns;
		this.layout = ClientToolkit.getToolkit().createGridLayout(columns, 1);
	}
	
	@SuppressWarnings("unchecked")
	protected Class<T> getObjectOfFormClass() {
		return (Class<T>) GenericUtils.getGenericClass(this.getClass());
	}

	// Variante des Demo-Fillers mit dem Accelerator - Mechanismus
	// Nachteil: Funktioniert bei Textfeldern nicht, da die Felder das Zeichen
	// selber abfangen.
	// 
	// private void installFillWithDemoDataAction() {
	// installFillWithDemoDataAction(this);
	// }
	//	
	// private void installFillWithDemoDataAction(JComponent component) {
	// Object object = "§";
	// getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke('§'),
	// object);
	// getActionMap().put(object, new FillWithDemoDataAction());
	// }
	
	// Methods to create the form

	@Override
	public Object getComponent() {
		return layout;
	}

	@Override
	public void setSaveAction(Action saveAction) {
		// TODO sollte noch für ctrl - S verwendet werden.
		this.saveAction = saveAction;
	}

	public FormField<?> createField(Object keyObject) {
		if (keyObject == null) {
			throw new NullPointerException("Key must not be null");
		} else if (keyObject instanceof FormField) {
			FormField<?> component = (FormField<?>) keyObject;
			if (StringUtils.isBlank(component.getName())) {
				throw new IllegalArgumentException(IComponent.class.getSimpleName() + " has no name");
			}
			return component;
		} else {
			// keyString ist beispielsweise : "nationality" oder "address.zip"
			String keyString = Constants.getConstant(keyObject);
			AccessorInterface accessor = PropertyAccessor.getAccessor(objectClass, keyString);
			FormField<?> field;
			if (accessor.getClazz() == String.class) {
				Format format = Formats.getInstance().getFormat(accessor);
				if (format != null) {
					field = createStringField(keyString, format);
				} else {
					field = new TypeUnknownField(keyString, accessor);
				}
			} else {
				field = createField(keyString, accessor);
			}
			if (accessor.getAnnotation(Required.class) != null && field instanceof EditField<?>) {
				mandatoryFields.add((EditField<?>) field);
			}
			return field;
		}
	}
	
	protected FormField<?> createField(String name, AccessorInterface accessor) {
		throw new IllegalArgumentException("Unknown Field:" + accessor.getName());
	}
	
	protected FormField<?> createStringField(String name, Format format) {
		if (!editable) {
			return new TextFormField(name, format);
		} else {
			if (format instanceof PlainFormat) {
				return new TextEditField(name, format.getSize());
			} else if (format instanceof Code) {
				Code code = (Code) format;
				return new CodeEditField(name, code);
			} else if (format instanceof DateFormat) {
				DateFormat dateFormat = (DateFormat) format;
				return new DateField(name, dateFormat.isPartialAllowed());
			} else if (format instanceof IntegerFormat) {
				IntegerFormat integerformat = (IntegerFormat) format;
				return new NumberEditField(name, String.class, integerformat.getSize(), integerformat.isNonNegative());
			} else if (format instanceof BooleanFormat) {
				BooleanFormat booleanTypeDescription = (BooleanFormat) format;
				return new CheckBoxStringField(name, "", editable);
			}
		}
		
		throw new IllegalArgumentException("Unknown TypeDescription: " + format);
	}

	// 

	public void line(Object key) {
		FormField<?> visual = createField(key);
		add(visual, columns);
	}
	
	public void line(Object... keys) {
		int span = columns / keys.length;
		for (Object key : keys) {
			FormField<?> visual = createField(key);
			add(visual, span);
		}
	}
	
	private void add(FormField<?> c, int span) {
		layout.add(caption(c), c, span);
		registerNamedField(c);
	}
	
	// 

	public void area(Object... keys) {
		int span = columns / keys.length;
		for (Object key : keys) {
			FormField<?> field = createField(key);
			area(field, span);
		}
	}

	private void area(FormField<?> visual, int span) {
		layout.addArea(caption(visual), visual, span);
		registerNamedField(visual);
		resizable = true;
	}
	
	//

	public void text(String text) {
		text(text, 1);
	}
	
	public void text(String text, int span) {
		IComponent label = ClientToolkit.getToolkit().createLabel(text);
		layout.add(null, label, span);
	}

	public void addTitle(String text) {
		IComponent label = ClientToolkit.getToolkit().createTitle(text);
		layout.add(null, label, columns);
	}

	//

	public void setRequired(Object keyObject) {
		setRequired(keyObject, true);
	}
		
	public void setRequired(Object keyObject, boolean required) {
		IComponent component = getField(Constants.getConstant(keyObject));
		if (component == null) {
			throw new IllegalArgumentException("Field not found: " + keyObject);
		} else if (!(component instanceof EditField<?>)) {
			throw new IllegalArgumentException("Only EditFields can set to required. " + keyObject + " is not an EditField but a " + component.getClass());
		}
		if (required) {
			if (!mandatoryFields.contains(component)) {
				mandatoryFields.add((EditField<?>) component);
			}
		} else {
			if (mandatoryFields.contains(component)) {
				mandatoryFields.remove(component);
			}
		}
	}
	
	//
	
	protected FormField<?> getField(Object keyObject) {
		String name = Constants.getConstant(keyObject);
		for (FormField<?> field : fields) {
			if (StringUtils.equals(name, field.getName())) {
				return field;
			}
		}
		return null;
	}
	
	//

	protected void registerNamedField(FormField<?> field) {
		fields.add(field);
		if (field instanceof ChangeableValue<?>) {
			ChangeableValue<?> changeable = (ChangeableValue<?>) field;
			changeable.setChangeListener(formPanelChangeListener);
		}
//		addListeners(field);
	}

//	TODO: Mit diesem Mechanismus wird das fillWithDemoData ausgelöst. Das muss irgendwie unabhängig vom Toolkit implementiert werden
//	private void addListeners(Object component) {
//		component.addKeyListener(getKeyListener());
//		
//		if (component instanceof AbstractComponentContainer) {
//			AbstractComponentContainer container = (AbstractComponentContainer) component;
//			for (Object child : container.getComponents()) {
//				addListeners(child);
//			}
//		}
//	}
//
//	private KeyListener getKeyListener() {
//		if (keyListener == null) {
//			keyListener = new KeyAdapter() {
//				@Override
//				public void keyTyped(KeyEvent e) {
//					boolean generateData = Application.preferences().getBoolean("generateData", false);
//					if (generateData && '§' == e.getKeyChar()) {
//						fillWithDemoData();
//						e.consume();
//					}
//				}
//			};
//		}
//		return keyListener;
//	}

	@Override
	public void fillWithDemoData() {
		for (IComponent field : fields) {
			if (field instanceof DemoEnabled) {
				DemoEnabled demoEnabledField = (DemoEnabled) field;
				demoEnabledField.fillWithDemoData();
			}
		}
	}

	//

	protected String caption(FormField<?> visual) {
		return Resources.getObjectFieldName(resourceBundle, objectClass, visual.getName());
	}
	
	public CheckBoxField createCheckBoxField(Object key) {
		String name = Constants.getConstant(key);
		String text = Resources.getObjectFieldName(resourceBundle, objectClass, name);
		CheckBoxField checkBoxField = new CheckBoxField(name, text);
		return checkBoxField;
	}

	public CheckBoxStringField createCheckBoxStringField(Object key) {
		String name = Constants.getConstant(key);
		String text = Resources.getObjectFieldName(resourceBundle, objectClass, name);
		CheckBoxStringField checkBoxField = new CheckBoxStringField(name, text, true);
		return checkBoxField;
	}

	//

	@Override
	public void setObject(T object) {
		this.object = object;
		writesValueToFields();
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void writesValueToFields() {
		for (IComponent visual : fields) {
			if (visual instanceof FormField) {
				FormField formField = (FormField) visual;
				String name =  formField.getName();
				Object value = PropertyAccessor.get(object, name);
				formField.setObject(value);
			}
		}
		
	}
	
	@Override
	public T getObject() {
		return object;
	}

	// private class FillWithDemoDataAction extends AbstractAction {
	// @Override
	// public void actionPerformed(ActionEvent arg0) {
	// boolean generateData =
	// AbstractApplication.preferences().getBoolean("generateData", false);
	// if (generateData) {
	// fillWithDemoData();
	// }
	// }
	// }

	@Override
	public boolean isResizable() {
		return resizable;
	}

	// Changeable
	
	@Override
	public void setChangeListener(ChangeListener changeListener) {
		this.changeListener = changeListener;
	}

	private class FormPanelChangeListener implements ChangeListener {

		@Override
		public void stateChanged(ChangeEvent event) {
			EditField<?> changedField = (EditField<?>) event.getSource();
			
			logger.fine("ChangeEvent from " + changedField.getName());
			
			Object formFieldValue = changedField.getObject();			
			PropertyAccessor.set(object, changedField.getName(), formFieldValue);
			
			forwardToDependingFields(changedField);

			if (changeListener != null) {
				changeListener.stateChanged(event);
			}
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void forwardToDependingFields(EditField<?> changedField) {
		boolean possibleDepending = false;
		for (IComponent field : fields) {
			if (field == changedField) {
				possibleDepending = true;
				continue;
			}
			if (!possibleDepending) {
				// only fields below the changed field can be depending.
				// know what you do before remove this line!
				continue;
			}
			if (field instanceof DependingOnFieldAbove) {
				DependingOnFieldAbove dependingOnFieldAbove = (DependingOnFieldAbove) field;
				if (StringUtils.equals(changedField.getName(), dependingOnFieldAbove.getNameOfDependedField())) {
					dependingOnFieldAbove.setDependedField(changedField);
				}
			}
		}
	}

	// Validation
	
	@Override
	public void validate(List<ValidationMessage> resultList) {
		for (FormField<?> field : fields) {
			if (field instanceof EditField) {
				EditField<?> editField = (EditField<?>) field;
				if (mandatoryFields.contains(editField) && editField.isEmpty()) {
					resultList.add(new ValidationMessage(field.getName(), "Eingabe erforderlich"));
				}
			}
			if (field instanceof Validatable) {
				Validatable validatable = (Validatable) field;
				validateProtected(resultList, validatable);
			} 
		}
	}

	private static void validateProtected(List<ValidationMessage> resultList, Validatable validatable) {
		try {
			validatable.validate(resultList);
		} catch (Exception x) {
			if (validatable instanceof FormField && !StringUtils.isBlank(((FormField<?>) validatable).getName())) {
				FormField<?> field = (FormField<?>) validatable;
				logger.log(Level.SEVERE, "Exception in validation of " + field.getName(), x);
			} else {
				logger.log(Level.SEVERE, "Exception in validation", x);
			}
		}
	}
	
	// Indicate
	
	@Override
	public void setValidationMessages(List<ValidationMessage> validationMessages) {
		for (FormField<?> field : fields) {
			if (field instanceof Indicator) {
				Indicator indicator = (Indicator) field;
				List<ValidationMessage> filteredValidationMessages = ValidationMessage.filterValidationMessage(validationMessages, field.getName());
				indicator.setValidationMessages(filteredValidationMessages);
			}
		}
	}

}