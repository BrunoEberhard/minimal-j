package ch.openech.mj.vaadin.toolkit;

import ch.openech.mj.util.ProgressListener;

import com.vaadin.ui.ProgressIndicator;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

public class VaadinProgressDialog extends Window implements ProgressListener {

	private final ProgressIndicator progressIndicator;
	
	public VaadinProgressDialog(Window parentWindow, String title) {
		super(title);
		
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
		} else {
			progressIndicator.setIndeterminate(false);
			progressIndicator.setValue(((float) value) / ((float) maximum));
		}
	}

}
