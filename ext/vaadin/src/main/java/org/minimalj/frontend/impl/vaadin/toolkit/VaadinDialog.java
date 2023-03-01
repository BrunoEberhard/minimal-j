package org.minimalj.frontend.impl.vaadin.toolkit;

import org.minimalj.frontend.Frontend.IContent;
import org.minimalj.frontend.action.Action;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.dom.ElementConstants;

public class VaadinDialog extends Dialog {
	private static final long serialVersionUID = 1L;
	
	private final Action saveAction, cancelAction;
	
//	public VaadinDialog(String title, Component component, Action saveAction, Action closeAction, Action... actions) {
	public VaadinDialog(org.minimalj.frontend.page.Page.Dialog dialog) {
		super(new VaadinEditorLayout(dialog.getTitle(), (Component) dialog.getContent(), dialog.getSaveAction(), dialog.getCancelAction(), dialog.getActions()));

		this.saveAction = dialog.getSaveAction();
		this.cancelAction = dialog.getCancelAction();

		setCloseOnEsc(true);
		setCloseOnOutsideClick(false);
        setDraggable(true);
        setResizable(true);

        getElement().executeJs("this.$.overlay.$.overlay.style[$0]=$1", ElementConstants.STYLE_MAX_HEIGHT, "97%");

		if (cancelAction != null) {
			addDialogCloseActionListener(new VaadinDialogListener());
		}

//		TODO: VaadinComponentWithWidth componentWithWidth = findComponentWithWidth(content);
		IContent component = dialog.getContent();
        if (component instanceof VaadinFormContent) {
            VaadinFormContent form = (VaadinFormContent) component;

			setWidth((form.getDialogWidth() + 10) + "ex");
			getElement().executeJs("this.$.overlay.$.overlay.style[$0]=$1", ElementConstants.STYLE_MIN_WIDTH, "50ex");
		}

		open();
	}
	
	private class VaadinDialogListener implements ComponentEventListener<DialogCloseActionEvent> {

		private static final long serialVersionUID = 1L;

		@Override
		public void onComponentEvent(DialogCloseActionEvent event) {
			cancelAction.run();
		}
	}
	
	public Action getSaveAction() {
		return saveAction;
	}
	
//	@Override
//	public Action getCancelAction() {
//		return cancelAction;
//	}
	
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
