package org.minimalj.frontend.impl.vaadin.toolkit;

import java.util.List;

import org.minimalj.frontend.Frontend.FormContent;
import org.minimalj.frontend.Frontend.IComponent;
import org.minimalj.frontend.form.element.FormElementConstraint;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasElement;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.HasValidation;
import com.vaadin.flow.component.KeyNotifier;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;

public class VaadinGridFormLayout extends FormLayout implements FormContent, VaadinComponentWithWidth {
	private static final long serialVersionUID = 1L;
	
	private final int columns;
	private final int columnWidth;
	private final int dialogWidth;
	private int column, row;
	
	public VaadinGridFormLayout(int columns, int columnWidthPercentage) {
		addClassName("form");
		
		this.columns = columns;
		columnWidth = columnWidthPercentage / 2; // Math.max(columnWidthPercentage, 60 / columns);
		dialogWidth = columnWidth * columns;
		
//		for (int i = 0; i<columns; i++) {
//			setColumnExpandRatio(i, 1.0f / columns);
//		}
//		
//		setSpacing(true);
	}
	
	@Override
	public int getDialogWidth() {
		return dialogWidth;
	}

	@Override
	public void add(String caption, IComponent field, FormElementConstraint constraint, int span) {
		if (field instanceof TextField) {
			((TextField) field).setLabel(caption);
		} else if (field instanceof PasswordField) {
			((PasswordField) field).setLabel(caption);
		} else if (field instanceof Checkbox) {
			((Checkbox) field).setLabel(caption);
		} else if (field instanceof ComboBox) {
			((ComboBox<?>) field).setLabel(caption);
		}
//		if (StringUtils.isBlank(caption)) {
//			((Component) field).addClassName("noCaption");
//		}
		add(field, span);
	}

	boolean first = true;
	KeyNotifier lastField = null;

	public KeyNotifier getLastField() {
		return lastField;
	}

	private void add(IComponent field, int span) {
		Component component = (Component) field;
		((HasSize) component).setWidthFull();

		if (first && field instanceof HasElement) {
			((HasElement) field).getElement().setAttribute("autofocus", "true");
			first = false;
		}
		
		if (field instanceof KeyNotifier) {
			lastField = (KeyNotifier) field;
		}

		if (span < 1) {
			span = columns;
		}

		add(component, span);
//		setRows(row+1); // addComponent with these arguments doesnt auto grow grid
//		addComponent(component, column, row, column + span -1, row);
//		
//		column += span;
//		if (column >= columns) {
//			column = 0;
//			row++;
//		}
	}

	@Override
	public void setValidationMessages(IComponent field, List<String> validationMessages) {
		String errorMessage = validationMessages.isEmpty() ? null : validationMessages.get(0);
		if (field instanceof HasValidation) {
			VaadinIndication.setValidationMessages(validationMessages, (HasValidation) field);
		} 
	}

}
