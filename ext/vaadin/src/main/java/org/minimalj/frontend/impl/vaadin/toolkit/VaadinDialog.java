package org.minimalj.frontend.impl.vaadin.toolkit;

import org.minimalj.frontend.action.Action;
import org.minimalj.frontend.page.IDialog;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.dialog.Dialog;

public class VaadinDialog extends Dialog implements IDialog {
	private static final long serialVersionUID = 1L;
	
	private final Action saveAction, closeAction;
	private static int styleCount;
	
	public VaadinDialog(String title, Component component, Action saveAction, Action closeAction, Action... actions) {
		super(new VaadinEditorLayout(title, component, saveAction, closeAction, actions));

		this.saveAction = saveAction;
		this.closeAction = closeAction;

		setCloseOnEsc(true);
		setCloseOnOutsideClick(false);
		setDraggable(true);
		setResizable(true);

		if (closeAction != null) {
			addDialogCloseActionListener(new VaadinDialogListener());
		}

		if (component instanceof VaadinGridFormLayout) {
			VaadinGridFormLayout form = (VaadinGridFormLayout) component;
			if (form.getLastField() != null) {
				form.getLastField().addKeyPressListener(Key.ENTER, event -> {
					if (saveAction.isEnabled()) {
						saveAction.action();
					}
				});
			}
		}

		open();
		
//		VaadinComponentWithWidth componentWithWidth = findComponentWithWidth(content);
//		if (componentWithWidth != null) {
//			setWidth(componentWithWidth.getDialogWidth() + "ex");
//		}
//		
//		UI.getCurrent().addWindow(this);
//		VaadinFrontend.focusFirstComponent(getContent());
	}
	
	private class VaadinDialogListener implements ComponentEventListener<DialogCloseActionEvent> {

		private static final long serialVersionUID = 1L;

		@Override
		public void onComponentEvent(DialogCloseActionEvent event) {
			closeAction.action();
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
