package org.minimalj.frontend.impl.vaadin.toolkit;

import org.minimalj.frontend.action.Action;
import org.minimalj.frontend.page.IDialog;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.dom.ElementConstants;

public class VaadinDialog extends Dialog implements IDialog {
	private static final long serialVersionUID = 1L;
	
	private final Action saveAction, closeAction;
	
	public VaadinDialog(String title, Component component, Action saveAction, Action closeAction, Action... actions) {
		super(new VaadinEditorLayout(title, component, saveAction, closeAction, actions));

		this.saveAction = saveAction;
		this.closeAction = closeAction;

		setCloseOnEsc(true);
		setCloseOnOutsideClick(false);
        setDraggable(true);
        setResizable(true);

        getElement().executeJs("this.$.overlay.$.overlay.style[$0]=$1", ElementConstants.STYLE_MAX_HEIGHT, "97%");

		if (closeAction != null) {
			addDialogCloseActionListener(new VaadinDialogListener());
		}

//		TODO: VaadinComponentWithWidth componentWithWidth = findComponentWithWidth(content);
        if (component instanceof VaadinFormContent) {
            VaadinFormContent form = (VaadinFormContent) component;
			if (form.getLastField() != null) {
				form.getLastField().addKeyPressListener(Key.ENTER, event -> {
					if (saveAction.isEnabled()) {
						saveAction.run();
					}
				});
			}
			setWidth((form.getDialogWidth() + 10) + "ex");
			getElement().executeJs("this.$.overlay.$.overlay.style[$0]=$1", ElementConstants.STYLE_MIN_WIDTH, "50ex");
		}

		open();
	}
	
	private class VaadinDialogListener implements ComponentEventListener<DialogCloseActionEvent> {

		private static final long serialVersionUID = 1L;

		@Override
		public void onComponentEvent(DialogCloseActionEvent event) {
			closeAction.run();
		}
	}
	
	public Action getSaveAction() {
		return saveAction;
	}
	
	@Override
	public void closeDialog() {
		super.close();
	}
	
//	private static VaadinComponentWithWidth findComponentWithWidth(Component c) {
//		if (c instanceof VaadinComponentWithWidth) {
//			return (VaadinComponentWithWidth) c;
//		} else if (c instanceof Panel) {
//			Panel panel = (Panel) c;
//			return findComponentWithWidth(panel.getContent());
//		} else if (c instanceof ComponentContainer) {
//			ComponentContainer container = (ComponentContainer) c;
//			Iterator<Component> componentIterator = container.getComponentIterator();
//			while (componentIterator.hasNext()) {
//				VaadinComponentWithWidth componentWithWidth = findComponentWithWidth(componentIterator.next());
//				if (componentWithWidth != null) {
//					return componentWithWidth;
//				}
//			}
//		}
//		return null;
//	}
//
//	@Override
//	public void close() {
//		// super.close(); DONT, would always close without ask
//		fireClose();
//	}

}
