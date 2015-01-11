package org.minimalj.frontend.vaadin.toolkit;

import java.util.List;

import org.minimalj.frontend.toolkit.ClientToolkit.IComponent;
import org.minimalj.frontend.toolkit.FlowField;
import org.minimalj.frontend.toolkit.FormContent;

import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.Component;
import com.vaadin.ui.GridLayout;

public class VaadinGridFormLayout extends GridLayout implements FormContent, VaadinComponentWithWidth {
	private static final long serialVersionUID = 1L;
	
	private final int columns;
	private final int columnWidth;
	private final int width;
	private int column, row;
	private boolean isVerticallyGrowing;
	
	public VaadinGridFormLayout(int columns, int columnWidthPercentage) {
		super(columns, 1);
		this.columns = columns;
		this.columnWidth = Math.max(columnWidthPercentage / 3, 60 / columns);
		
		width = columnWidth * columns;
		setWidth(width + "ex");
		
		setSpacing(false);
		setMargin(new MarginInfo(true, false, true, false));
		addStyleName("gridForm");
	}
	
	@Override
	public int getDialogWidth() {
		return width;
	}

	public boolean isVerticallyGrowing() {
		return isVerticallyGrowing;
	}

	@Override
	public void add(IComponent component) {
		add(null, component, columns);
	}

	@Override
	public void add(String caption, IComponent field, int span) {
		GridLayout gridLayout = new GridLayout(1, 1);
		gridLayout.setWidth((columnWidth * span) + "ex");
		gridLayout.setColumnExpandRatio(0, 1.0f);
		gridLayout.setMargin(new MarginInfo(false, column + span >= columns, false, true));
		gridLayout.setSpacing(false);
		
		Component component = (Component) field;
		component.setWidth("100%");
		component.setCaption(caption);
		gridLayout.addComponent(component, 0, 0);
		
		setRows(row+1); // addComponent with these arguments doenst auto grow grid
		addComponent(gridLayout, column, row, column + span -1, row);
		
		column += span;
		if (column >= columns) {
			column = 0;
			row++;
		}
		
		if (field instanceof FlowField) {
			isVerticallyGrowing = true;
		}
	}

	@Override
	public void setValidationMessages(IComponent component, List<String> validationMessages) {
		AbstractComponent vaadinComponent = (AbstractComponent) component;
		VaadinIndication.setValidationMessages(validationMessages, vaadinComponent);
	}

}
