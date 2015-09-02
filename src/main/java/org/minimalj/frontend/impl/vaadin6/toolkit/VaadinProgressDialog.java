package org.minimalj.frontend.impl.vaadin6.toolkit;

import org.minimalj.frontend.page.ProgressListener;

import com.vaadin.ui.ProgressIndicator;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

public class VaadinProgressDialog extends Window implements ProgressListener {
	private static final long serialVersionUID = 1L;

	private final ProgressIndicator progressIndicator;
	private final Window parentWindow;
	
	public VaadinProgressDialog(Window parentWindow, String title) {
		super(title);
		this.parentWindow = parentWindow;
		progressIndicator = new ProgressIndicator();
		
		VerticalLayout layout = new VerticalLayout();
		layout.addComponent(progressIndicator);
		setContent(layout);
		
		setModal(true);
		parentWindow.addWindow(this);
	}

	
	@Override
	public void showProgress(int value, int maximum) {
		if (value == maximum) {
			setVisible(false);
			parentWindow.requestRepaint();
		} else {
			progressIndicator.setIndeterminate(false);
			progressIndicator.setValue(((float) value) / ((float) maximum));
		}
	}

}
