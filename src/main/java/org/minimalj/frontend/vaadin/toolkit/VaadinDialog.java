package org.minimalj.frontend.vaadin.toolkit;

import java.util.Iterator;

import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.action.Action;
import org.minimalj.frontend.page.IDialog;
import org.minimalj.frontend.vaadin.VaadinWindow;

import com.vaadin.ui.Component;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Window;

public class VaadinDialog extends Window implements IDialog {
	private static final long serialVersionUID = 1L;
	
	private final Action closeAction;
	
	public VaadinDialog(Window parentWindow, String title, ComponentContainer content, Action closeAction) {
		super(title, content);
		this.closeAction = closeAction;
		
		setModal(true);
		addListener(new VaadinDialogListener());
		parentWindow.addWindow(this);
		
		VaadinComponentWithWidth componentWithWidth = findComponentWithWidth(content);
		if (componentWithWidth != null) {
			setWidth((componentWithWidth.getDialogWidth() + 1) + "ex");
		}
		
		setVisible(true);
		VaadinFrontend.focusFirstComponent(getContent());
	}
	
	private class VaadinDialogListener implements com.vaadin.ui.Window.CloseListener {

		private static final long serialVersionUID = 1L;

		@Override
		public void windowClose(CloseEvent e) {
			if (closeAction != null) {
				closeAction.action();
			} else {
				closeDialog();
			}
		}
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
	protected void close() {
		// super.close(); DONT, would always close without ask

		Frontend.setBrowser((VaadinWindow) getWindow());
		fireClose();
		Frontend.setBrowser(null);
	}

}
