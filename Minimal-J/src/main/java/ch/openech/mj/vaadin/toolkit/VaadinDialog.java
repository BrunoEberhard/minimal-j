package ch.openech.mj.vaadin.toolkit;

import java.util.Iterator;

import ch.openech.mj.toolkit.IDialog;

import com.vaadin.ui.Component;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Window;

public class VaadinDialog extends Window implements IDialog {
	private static final long serialVersionUID = 1L;
	
	private final Window parentWindow;
	private ch.openech.mj.toolkit.IDialog.CloseListener closeListener;
	
	public VaadinDialog(Window parentWindow, ComponentContainer content, String title) {
		super(title, decorateWithScrollPanel(content));
		this.parentWindow = parentWindow;
		
		setModal(true);
		addListener(new VaadinDialogListener());
		parentWindow.addWindow(this);
		
		VaadinGridFormLayout formLayout = findFormLayout(content);
		if (formLayout != null) {
			setWidth(formLayout.getDialogWidth() + "ex");
//			if (formLayout.isVerticallyGrowing()) {
//				setHeight((parentWindow.getBrowserWindowHeight() - 50) + "px");
//			}
		}
	}

	private static Panel decorateWithScrollPanel(ComponentContainer content) {
		Panel scrollablePanel = new Panel(content);
		scrollablePanel.setScrollable(true);
		scrollablePanel.setSizeFull();
		return scrollablePanel;
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
	public void setCloseListener(ch.openech.mj.toolkit.IDialog.CloseListener closeListener) {
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
	
	private static VaadinGridFormLayout findFormLayout(Component c) {
		if (c instanceof VaadinGridFormLayout) {
			return (VaadinGridFormLayout) c;
		} else if (c instanceof Panel) {
			Panel panel = (Panel) c;
			return findFormLayout(panel.getContent());
		} else if (c instanceof ComponentContainer) {
			ComponentContainer container = (ComponentContainer) c;
			Iterator<Component> componentIterator = container.getComponentIterator();
			while (componentIterator.hasNext()) {
				VaadinGridFormLayout formLayout = findFormLayout(componentIterator.next());
				if (formLayout != null) {
					return formLayout;
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
