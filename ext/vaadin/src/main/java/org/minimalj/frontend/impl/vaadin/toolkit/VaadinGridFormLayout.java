package org.minimalj.frontend.impl.vaadin.toolkit;

import java.util.List;

import org.minimalj.frontend.Frontend.FormContent;
import org.minimalj.frontend.Frontend.IComponent;
import org.minimalj.frontend.form.element.FormElementConstraint;
import org.minimalj.util.StringUtils;

import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.Component;
import com.vaadin.ui.GridLayout;

public class VaadinGridFormLayout extends GridLayout implements FormContent, VaadinComponentWithWidth {
	private static final long serialVersionUID = 1L;
	
	private final int columns;
	private final int columnWidth;
	private final int dialogWidth;
	private int column, row;
	
	public VaadinGridFormLayout(int columns, int columnWidthPercentage) {
		super(columns, 1);
		addStyleName("form");
		
		this.columns = columns;
		columnWidth = columnWidthPercentage / 2; // Math.max(columnWidthPercentage, 60 / columns);
		dialogWidth = columnWidth * columns;
		
		for (int i = 0; i<columns; i++) {
			setColumnExpandRatio(i, 1.0f / columns);
		}
		
		setSpacing(true);
	}
	
	@Override
	public int getDialogWidth() {
		return dialogWidth;
	}

	@Override
	public void add(String caption, IComponent field, FormElementConstraint constraint, int span) {
		add(field, span);
		((Component) field).setCaption(caption);
		if (StringUtils.isBlank(caption)) {
			((Component) field).addStyleName("noCaption");
		}
	}

	private void add(IComponent field, int span) {
		Component component = (Component) field;
		component.setWidth(100, Unit.PERCENTAGE);
		
		if (span < 1) {
			span = columns;
		}

		setRows(row+1); // addComponent with these arguments doesnt auto grow grid
		addComponent(component, column, row, column + span -1, row);
		
		column += span;
		if (column >= columns) {
			column = 0;
			row++;
		}
	}

	@Override
	public void setValidationMessages(IComponent component, List<String> validationMessages) {
		AbstractComponent vaadinComponent = (AbstractComponent) component;
		VaadinIndication.setValidationMessages(validationMessages, vaadinComponent);
	}

}
