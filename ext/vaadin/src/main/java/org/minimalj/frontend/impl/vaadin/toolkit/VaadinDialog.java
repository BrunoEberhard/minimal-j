package org.minimalj.frontend.impl.vaadin.toolkit;

import java.util.Iterator;

import org.minimalj.frontend.action.Action;
import org.minimalj.frontend.page.IDialog;

import com.vaadin.ui.Component;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.Panel;
import com.vaadin.ui.UI;
import com.vaadin.ui.Window;

public class VaadinDialog extends Window implements IDialog {
	private static final long serialVersionUID = 1L;
	
	private CloseListener closeListener;
	private final Action saveAction, closeAction;
	private static int styleCount;
	
	public VaadinDialog(ComponentContainer content, String title, Action saveAction, Action closeAction) {
		super(title, content);
		this.saveAction = saveAction;
		this.closeAction = closeAction;
		
		setModal(true);
		addCloseListener(new VaadinDialogListener());
		
		VaadinComponentWithWidth componentWithWidth = findComponentWithWidth(content);
		if (componentWithWidth != null) {
			setWidth(componentWithWidth.getDialogWidth() + "ex");
		}
		
		UI.getCurrent().addWindow(this);
		VaadinFrontend.focusFirstComponent(getContent());
	}
	
	private class VaadinDialogListener implements com.vaadin.ui.Window.CloseListener {

		private static final long serialVersionUID = 1L;

		@Override
		public void windowClose(CloseEvent e) {
			// if (closeListener == null || closeListener.close()) {
				VaadinDialog.super.close();
				closeAction.action();
			// }
		}
	}
	
	public Action getSaveAction() {
		return saveAction;
	}
	
	@Override
	public void closeDialog() {
		setVisible(false);
	}
	
	private static VaadinComponentWithWidth findComponentWithWidth(Component c) {
		if (c instanceof VaadinComponentWithWidth) {
			return (VaadinComponentWithWidth) c;
		} else if (c instanceof Panel) {
			Panel panel = (Panel) c;
			return findComponentWithWidth(panel.getContent());
		} else if (c instanceof ComponentContainer) {
			ComponentContainer container = (ComponentContainer) c;
			Iterator<Component> componentIterator = container.getComponentIterator();
			while (componentIterator.hasNext()) {
				VaadinComponentWithWidth componentWithWidth = findComponentWithWidth(componentIterator.next());
				if (componentWithWidth != null) {
					return componentWithWidth;
				}
			}
		}
		return null;
	}

	@Override
	public void close() {
		// super.close(); DONT, would always close without ask
		fireClose();
	}

}
