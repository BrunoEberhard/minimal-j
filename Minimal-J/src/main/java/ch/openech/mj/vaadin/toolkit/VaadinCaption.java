package ch.openech.mj.vaadin.toolkit;

import java.util.List;

import ch.openech.mj.edit.validation.Indicator;
import ch.openech.mj.edit.validation.ValidationMessage;
import ch.openech.mj.toolkit.IComponent;
import ch.openech.mj.toolkit.IComponentDelegate;

import com.vaadin.ui.AbstractComponent;

class VaadinCaption implements IComponentDelegate, Indicator {
	private final IComponent c;

	VaadinCaption(IComponent c, String caption) {
		this.c = c;
		AbstractComponent component = (AbstractComponent) getComponent();
		component.setCaption(caption);
	}

	@Override
	public Object getComponent() {
		if (c instanceof IComponentDelegate) {
			return ((IComponentDelegate) c).getComponent();
		} else {
			return c;
		}
	}

	@Override
	public void setValidationMessages(List<ValidationMessage> validationMessages) {
		VaadinIndication.setValidationMessages(validationMessages, (AbstractComponent) getComponent());
	}
}