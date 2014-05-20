package org.minimalj.frontend.swing.toolkit;

import java.awt.BorderLayout;
import java.awt.Window;

import javax.swing.JDialog;
import javax.swing.JProgressBar;

import org.minimalj.frontend.toolkit.ProgressListener;

public class SwingProgressDialog extends JDialog implements ProgressListener {
	private static final long serialVersionUID = 1L;
	
	private final JProgressBar progressBar;
	
	public SwingProgressDialog(Window owner, String text) {
		super(owner);
		setLocationRelativeTo(owner);
		setTitle(text);
		pack();
		
		setLayout(new BorderLayout());
		progressBar = new JProgressBar();
		progressBar.setMinimum(0);
		add(progressBar, BorderLayout.CENTER);
	}
	
	@Override
	public void showProgress(int value, int maximum) {
		if (value == maximum) {
			setVisible(false);
			dispose();
		} else {
			progressBar.setMaximum(maximum);
			progressBar.setValue(value);
		}
	}
	
}
