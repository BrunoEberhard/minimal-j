package ch.openech.mj.vaadin.toolkit;

import ch.openech.mj.toolkit.VisualDialog;

import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.Window;

public class VaadinDialog extends Window implements VisualDialog {

	private final Window parentWindow;
	private ch.openech.mj.toolkit.VisualDialog.CloseListener closeListener;
	
	public VaadinDialog(Window parentWindow, ComponentContainer content, String title) {
		super(title, content);
		this.parentWindow = parentWindow;
		
		setModal(true);
		addListener(new VaadinDialogListener());
		parentWindow.addWindow(this);
		setVisible(true);
	}

	private class VaadinDialogListener implements com.vaadin.ui.Window.CloseListener {

		@Override
		public void windowClose(CloseEvent e) {
			if (closeListener == null || closeListener.close()) {
				parentWindow.removeWindow(VaadinDialog.this);
			}
		}
	}
	
	@Override
	public void setCloseListener(ch.openech.mj.toolkit.VisualDialog.CloseListener closeListener) {
		this.closeListener = closeListener;
	}

}
