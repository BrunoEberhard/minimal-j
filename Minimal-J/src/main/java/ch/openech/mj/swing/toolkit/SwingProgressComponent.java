package ch.openech.mj.swing.toolkit;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JProgressBar;

import ch.openech.mj.toolkit.ProgressListener;

public class SwingProgressComponent extends JPanel implements ProgressListener {
	private final JProgressBar progressBar;
	
	public SwingProgressComponent() {
		super(new BorderLayout());
		
		progressBar = new JProgressBar();
		progressBar.setMinimum(0);
		add(progressBar, BorderLayout.CENTER);
	}
	
	@Override
	public void showProgress(int value, int maximum) {
		progressBar.setMaximum(maximum);
		progressBar.setValue(value);
	}

}
