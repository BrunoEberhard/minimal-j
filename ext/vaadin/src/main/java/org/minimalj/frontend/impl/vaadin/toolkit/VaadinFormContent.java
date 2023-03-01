package org.minimalj.frontend.impl.vaadin.toolkit;

import java.util.ArrayList;
import java.util.List;

import org.minimalj.frontend.Frontend.FormContent;
import org.minimalj.frontend.Frontend.IComponent;
import org.minimalj.frontend.form.element.FormElementConstraint;
import org.minimalj.frontend.impl.vaadin.toolkit.VaadinFrontend.HasCaption;
import org.minimalj.frontend.impl.vaadin.toolkit.VaadinFrontend.HasComponent;
import org.minimalj.util.StringUtils;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.HasValidation;
import com.vaadin.flow.component.KeyNotifier;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Label;

public class VaadinFormContent extends FormLayout implements FormContent, VaadinComponentWithWidth {
	private static final long serialVersionUID = 1L;
	
	private final int columns;
	private final int dialogWidth;

	private boolean first = true;
	private KeyNotifier lastField = null;
	
    public VaadinFormContent(int columns, int columnWidthPercentage) {
		addClassName("form");
		dialogWidth = columns * getColumnWidthInEx(columnWidthPercentage);
		setMaxWidth(dialogWidth * 140 / 100 + "ex");
		setResponsiveSteps(columns, columnWidthPercentage);
		
		this.columns = columns;
	}

	private int getColumnWidthInEx(int size) {
		return size / 3;
	}

	private void setResponsiveSteps(int columns, int columnWidthPercentage) {
		List<ResponsiveStep> steps = new ArrayList<>();
		steps.add(new ResponsiveStep("0px", 1));
		for (int i = 2; i <= columns; i++) {
			if (columns % i == 0) {
				steps.add(new ResponsiveStep(i * getColumnWidthInEx(columnWidthPercentage) + "ex", i));
			}
		}
		setResponsiveSteps(steps);
	}

	@Override
	public int getDialogWidth() {
		return dialogWidth;
	}

	@Override
	public void add(String caption, IComponent field, FormElementConstraint constraint, int span) {
        Component component = field instanceof HasComponent ? ((HasComponent) field).getComponent() : (Component) field;
        if (component instanceof HasSize) {
            ((HasSize) component).setWidthFull();
        }

        if (first && component != null && !(component instanceof VaadinReadOnlyTextField)) {
            ((Component) component).getElement().setAttribute("autofocus", "true");
            first = false;
        }

        if (component instanceof KeyNotifier) {
			lastField = (KeyNotifier) component;
        }

        if (span < 1) {
            span = columns;
        }

        if (field instanceof HasCaption) {
            if (!StringUtils.isEmpty(caption)) {
                ((HasCaption) field).setCaption(caption);
            }
        }
        
        if (constraint != null) {
        	
        }
        if (component != null) {
        	super.add(component, span);
        } else {
        	super.add(new Label(), span);
        }
	}

	public KeyNotifier getLastField() {
		return lastField;
	}

	@Override
	public void setValidationMessages(IComponent field, List<String> validationMessages) {
		if (field instanceof HasValidation) {
			VaadinIndication.setValidationMessages(validationMessages, (HasValidation) field);
		} 
	}

}
