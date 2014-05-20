package org.minimalj.frontend.vaadin.toolkit;

import java.util.Iterator;

import org.minimalj.frontend.toolkit.IDialog;

import com.vaadin.ui.Component;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Window;

public class VaadinDialog extends Window implements IDialog {
	private static final long serialVersionUID = 1L;
	
	private final Window parentWindow;
	private org.minimalj.frontend.toolkit.IDialog.CloseListener closeListener;
	
	public VaadinDialog(Window parentWindow, ComponentContainer content, String title) {
		super(title, content);
		this.parentWindow = parentWindow;
		
		setModal(true);
		addListener(new VaadinDialogListener());
		parentWindow.addWindow(this);
		
		VaadinComponentWithWidth componentWithWidth = findComponentWithWidth(content);
		if (componentWithWidth != null) {
			setWidth((componentWithWidth.getDialogWidth() + 1) + "ex");
		}
	}
	
	private class VaadinDialogListener implements com.vaadin.ui.Window.CloseListener {

		private static final long serialVersionUID = 1L;

		@Override
		public void windowClose(CloseEvent e) {
			if (closeListener == null || closeListener.close()) {
				parentWindow.removeWindow(VaadinDialog.this);
			}
		}
	}
	
	@Override
	public void setCloseListener(org.minimalj.frontend.toolkit.IDialog.CloseListener closeListener) {
		this.closeListener = closeListener;
	}
	
	@Override
	public void openDialog() {
		setVisible(true);
		VaadinClientToolkit.focusFirstComponent(getContent());
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
		fireClose();
	}

}
