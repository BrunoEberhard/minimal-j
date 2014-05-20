package org.minimalj.frontend.swing.toolkit;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.SwingWorker;

import org.minimalj.frontend.toolkit.ProgressListener;

public class SwingHeavyActionButton extends JButton {
	private static final long serialVersionUID = 1L;

	public SwingHeavyActionButton(Action action) {
		super(action);
	}

	public SwingHeavyActionButton(String text) {
		super(text);
	}

	@Override
    protected void fireActionPerformed(final ActionEvent event) {
		final ProgressListener progress = SwingClientToolkit.showProgress(this, "Waiting");
		SwingWorker<Object, Object> worker = new SwingWorker<Object, Object>() {
			@Override
			protected Object doInBackground() throws Exception {
				// ApplicationContext.setApplicationContext(SwingTab.this.getApplicationContext());
				SwingHeavyActionButton.super.fireActionPerformed(event);
				setProgress(100);
				return null;
			}
		};
		worker.addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				if ("progress".equals(evt.getPropertyName())) {
					progress.showProgress((Integer) evt.getNewValue(), 100);
				}
			}
		});
		worker.execute();
    }
	
}