package org.minimalj.frontend.form.element;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.Frontend.IComponent;
import org.minimalj.frontend.Frontend.SwitchComponent;
import org.minimalj.frontend.editor.Validator.IndexProperty;
import org.minimalj.frontend.form.Form;
import org.minimalj.model.Keys;
import org.minimalj.model.properties.ChainedProperty;
import org.minimalj.model.properties.PropertyInterface;
import org.minimalj.model.validation.ValidationMessage;

public class PositionListFormElement<T> extends AbstractFormElement<List<T>> {
	private static final Logger logger = Logger.getLogger(PositionListFormElement.class.getName());

	private final SwitchComponent switchComponent;
	private final boolean editable;
	private List<T> object;
	private final List<Form<?>> lineForms = new ArrayList<>();
	protected final List<PropertyInterface> properties;
//	private final PositionListModel model;

	public PositionListFormElement(List<T> key, List<Object> columns, boolean editable) {
		this(Keys.getProperty(key), columns, editable);
	}

	public PositionListFormElement(PropertyInterface property, List<Object> columns, boolean editable) {
		super(property);
		this.editable = editable;
		this.properties = convert(columns);
//		this.model = createModel();
		this.switchComponent = Frontend.getInstance().createSwitchComponent();
	}

	private static List<PropertyInterface> convert(List<Object> keys) {
		List<PropertyInterface> properties = new ArrayList<>(keys.size());
		for (Object key : keys) {
			PropertyInterface property = Keys.getProperty(key);
			if (property != null) {
				properties.add(property);
			} else {
				logger.log(Level.WARNING, "Key not a property: " + key);
			}
		}
		if (properties.size() == 0) {
			logger.log(Level.SEVERE, "list without valid keys");
		}
		return Collections.unmodifiableList(properties);
	}

//	private PositionListModel createModel() {
//		return new BasePositionListModel();
//	}
//
//	protected class BasePositionListModel implements PositionListModel {
//
//		public BasePositionListModel() {
//		}
//
//		@Override
//		public int getColumnCount() {
//			return properties.size();
//		}
//
//		@Override
//		public int getWidth(int column) {
//			return 100;
//		}
//
//		@Override
//		public int getRowCount() {
//			return object.size();
//		}
//
//		@Override
//		public boolean canAdd() {
//			return editable;
//		}
//
//		@Override
//		public void addRow(int beforeRow) {
//			T newRow = CloneHelper.newInstance(clazz);
//			if (beforeRow >= 0 && beforeRow <= object.size()) {
//				object.add(beforeRow, newRow);
//			} else {
//				object.add(newRow);
//			}
//			handleChange();
//		}
//
//		@Override
//		public boolean canDelete() {
//			return editable;
//		}
//
//		@Override
//		public void deleteRow(int row) {
//			object.remove(row);
//			handleChange();
//		}
//
//		@Override
//		public boolean canMove() {
//			return false;
//		}
//
//		@Override
//		public void moveRow(int row, int beforeRow) {
//			// not yet implemented
//		}
//	}

	protected final boolean isEditable() {
		return editable;
	}

	@Override
	public List<T> getValue() {
		return object;
	}

	@Override
	public void setValue(List<T> object) {
		this.object = object;
		handleChange();
	}

	@Override
	public IComponent getComponent() {
		return switchComponent;
	}

	@Override
	public String getCaption() {
		return null;
	}

	protected void handleChange() {
		update();
		super.fireChange();
	}

	private void update() {
		IComponent[] lines = new IComponent[object.size() + 1];
		lineForms.clear();
		lines[0] = Frontend.getInstance().createTitle(super.getCaption());
		for (int index = 0; index < object.size(); index++) {
			Form<T> lineForm = new Form<>(editable, properties.size());
			lineForms.add(lineForm);
			lineForm.setIgnoreCaption(index > 0);
			lineForm.line(properties.toArray());
			lineForm.setChangeListener(form -> super.fireChange());
			lineForm.setObject(object.get(index));
			lines[index + 1] = lineForm.getContent();
		}
		IComponent vertical = Frontend.getInstance().createVerticalGroup(lines);
		switchComponent.show(vertical);
	}

	public void setValidationMessages(List<ValidationMessage> validationMessages) {
		ArrayList<ValidationMessage>[] validationMessagesByline = new ArrayList[lineForms.size()];
		for (int i = 0; i < lineForms.size(); i++) {
			validationMessagesByline[i] = new ArrayList<>();
		}

		for (ValidationMessage message : validationMessages) {
			List<PropertyInterface> chainMessage = ChainedProperty.getChain(message.getProperty());
			List<PropertyInterface> chainProperty = ChainedProperty.getChain(getProperty());

			PropertyInterface unchained = ChainedProperty.buildChain(chainMessage.subList(chainProperty.size() + 1, chainMessage.size()));
			IndexProperty indexProperty = (IndexProperty) chainMessage.get(chainProperty.size());

			message = new ValidationMessage(unchained, message.getFormattedText());
			validationMessagesByline[indexProperty.getIndex()].add(message);

			lineForms.get(indexProperty.getIndex());
		}

		for (int i = 0; i < lineForms.size(); i++) {
			lineForms.get(i).indicate(validationMessagesByline[i]);
		}
	}

}
