package org.minimalj.frontend.impl.vaadin.toolkit;

import java.util.List;

import org.minimalj.frontend.Frontend.FormContent;
import org.minimalj.frontend.Frontend.IComponent;
import org.minimalj.frontend.form.element.FormElementConstraint;
import org.minimalj.frontend.impl.vaadin.toolkit.VaadinFrontend.HasCaption;
import org.minimalj.frontend.impl.vaadin.toolkit.VaadinFrontend.HasComponent;
import org.minimalj.util.StringUtils;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasElement;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.HasValidation;
import com.vaadin.flow.component.KeyNotifier;
import com.vaadin.flow.component.formlayout.FormLayout;

public class VaadinGridFormLayout extends FormLayout implements FormContent, VaadinComponentWithWidth {
	private static final long serialVersionUID = 1L;
	
	private final int columns;
	private final int columnWidth;
	private final int dialogWidth;

	private boolean first = true;
	private KeyNotifier lastField = null;
	
	public VaadinGridFormLayout(int columns, int columnWidthPercentage) {
		addClassName("form");
		
		this.columns = columns;
		columnWidth = columnWidthPercentage / 2; // Math.max(columnWidthPercentage, 60 / columns);
		dialogWidth = columnWidth * columns;
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

        if (first && component instanceof HasElement && !(component instanceof VaadinReadOnlyTextField)) {
            ((HasElement) field).getElement().setAttribute("autofocus", "true");
            first = false;
        }

        if (component instanceof KeyNotifier) {
            lastField = (KeyNotifier) field;
        }

        if (span < 1) {
            span = columns;
        }

        if (field instanceof HasCaption) {
            if (!StringUtils.isEmpty(caption)) {
                ((HasCaption) field).setLabel(caption);
            }
        }
        super.add(component, span);
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
