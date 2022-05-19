package org.minimalj.frontend.form.element;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
	private final SwitchComponent switchComponent;
	private final boolean editable;
	private List<T> object;
	private final List<Form<?>> lineForms = new ArrayList<>();
	protected final List<Object> columns;
	private final List<PropertyInterface> propertyAsChain;

//	private final PositionListModel model;

	public PositionListFormElement(List<T> key, List<Object> columns, boolean editable) {
		this(Keys.getProperty(key), columns, editable);
	}

	public PositionListFormElement(PropertyInterface property, List<Object> columns, boolean editable) {
		super(property);
		this.editable = editable;
		this.columns = columns;
//		this.model = createModel();
		this.switchComponent = Frontend.getInstance().createSwitchComponent();
		this.propertyAsChain = ChainedProperty.getChain(getProperty());
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
			Form<T> lineForm = new Form<>(editable, columns.size());
			lineForms.add(lineForm);
			lineForm.setIgnoreCaption(index > 0);
			lineForm.line(columns.toArray());
			lineForm.setChangeListener(form -> super.fireChange());
			lineForm.setObject(object.get(index));
			lines[index + 1] = lineForm.getContent();
		}
		IComponent vertical = Frontend.getInstance().createVerticalGroup(lines);
		switchComponent.show(vertical);
	}

	public void setValidationMessages(List<ValidationMessage> validationMessages) {
		ArrayList<ValidationMessage>[] validationMessagesByline = new ArrayList[lineForms.size()];

		for (ValidationMessage message : validationMessages) {
			List<PropertyInterface> messagePropertyAsChain = ChainedProperty.getChain(message.getProperty());

			PropertyInterface unchained = ChainedProperty.buildChain(messagePropertyAsChain.subList(propertyAsChain.size() + 1, messagePropertyAsChain.size()));
			IndexProperty indexProperty = (IndexProperty) messagePropertyAsChain.get(propertyAsChain.size());
			var index = indexProperty.getIndex();

			message = new ValidationMessage(unchained, message.getFormattedText());
			if (validationMessagesByline[index] == null) {
				validationMessagesByline[index] = new ArrayList<>();
			}
			validationMessagesByline[index].add(message);
		}

		for (int i = 0; i < lineForms.size(); i++) {
			lineForms.get(i).indicate(validationMessagesByline[i] != null ? validationMessagesByline[i] : Collections.emptyList());
		}
	}

}
