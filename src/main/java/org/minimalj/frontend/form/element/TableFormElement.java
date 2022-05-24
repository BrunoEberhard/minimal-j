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

public class TableFormElement<T> extends AbstractFormElement<List<T>> {
	private final SwitchComponent switchComponent;
	private final boolean editable;
	private List<T> object;
	private final List<Form<?>> rowForms = new ArrayList<>();
//	protected final List<Object> columns;
	private final TableRowFormFactory<T> formFactory;
	private final List<PropertyInterface> propertyAsChain;

//	private final PositionListModel model;

	public TableFormElement(List<T> key, List<Object> columns, boolean editable) {
		this(Keys.getProperty(key), columns, editable);
	}

	public TableFormElement(PropertyInterface property, List<Object> columns, boolean editable) {
		this(property, new SimpleTableRowFormFactory<T>(columns), editable);
	}

	public TableFormElement(List<T> key, TableRowFormFactory<T> formFactory, boolean editable) {
		this(Keys.getProperty(key), formFactory, editable);
	}
	
	public TableFormElement(PropertyInterface property, TableRowFormFactory<T> formFactory, boolean editable) {
		super(property);
		this.editable = editable;
		this.formFactory = formFactory;
//		this.model = createModel();
		this.switchComponent = Frontend.getInstance().createSwitchComponent();
		this.propertyAsChain = ChainedProperty.getChain(getProperty());
	}

	public static interface TableRowFormFactory<T> {
		
		public Form<T> create(T object, int row, boolean editable);
		
	}
	
	protected static class SimpleTableRowFormFactory<T> implements TableRowFormFactory<T> {
		private final List<Object> columns;
		
		public SimpleTableRowFormFactory(List<Object> columns) {
			this.columns = Collections.unmodifiableList(columns);
		}
		
		@Override
		public Form<T> create(T object, int row, boolean editable) {
			Form<T> rowForm = new Form<>(editable, columns.size());
			rowForm.line(columns.toArray());
			return rowForm;
		}
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
		IComponent[] rows = new IComponent[object.size()];
		rowForms.clear();
		for (int index = 0; index < object.size(); index++) {
			var rowObject = object.get(index);
			Form<T> rowForm = formFactory.create(rowObject, index, editable);
			rowForms.add(rowForm);
			rowForm.setChangeListener(form -> super.fireChange());
			rowForm.setObject(rowObject);
			rows[index] = rowForm.getContent();
		}
		IComponent vertical = Frontend.getInstance().createVerticalGroup(rows);
		switchComponent.show(vertical);
	}

	public void setValidationMessages(List<ValidationMessage> validationMessages) {
		ArrayList<ValidationMessage>[] validationMessagesByRow = new ArrayList[rowForms.size()];

		for (ValidationMessage message : validationMessages) {
			List<PropertyInterface> messagePropertyAsChain = ChainedProperty.getChain(message.getProperty());

			PropertyInterface unchained = ChainedProperty.buildChain(messagePropertyAsChain.subList(propertyAsChain.size() + 1, messagePropertyAsChain.size()));
			IndexProperty indexProperty = (IndexProperty) messagePropertyAsChain.get(propertyAsChain.size());
			var index = indexProperty.getIndex();

			message = new ValidationMessage(unchained, message.getFormattedText());
			if (validationMessagesByRow[index] == null) {
				validationMessagesByRow[index] = new ArrayList<>();
			}
			validationMessagesByRow[index].add(message);
		}

		for (int i = 0; i < rowForms.size(); i++) {
			rowForms.get(i).indicate(validationMessagesByRow[i] != null ? validationMessagesByRow[i] : Collections.emptyList());
		}
	}

}
