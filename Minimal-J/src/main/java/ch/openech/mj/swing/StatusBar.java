package ch.openech.mj.swing;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;

import ch.openech.mj.resources.ResourceHelper;
import ch.openech.mj.resources.Resources;
import ch.openech.mj.util.StringUtils;


/**
 * A StatusBar panel that tracks a TaskMonitor. Although one could certainly create a more elaborate StatusBar class, this one is sufficient for the
 * examples that need one.
 * <p>
 * This class loads resources from the ResourceBundle called {@code resources.StatusBar}.
 * 
 */
public class StatusBar extends JPanel implements PropertyChangeListener {
	public static final String STATUS_BAR = "StatusBar";
	
	private final Insets zeroInsets = new Insets(0, 0, 0, 0);
	private final JLabel messageLabel;
	private final JLabel helpLabel;
	private final JProgressBar progressBar;
	private final JLabel statusAnimationLabel;
	private final Timer busyIconTimer;
	private final Icon idleIcon;
	private final Icon[] busyIcons = new Icon[15];
	private final int busyAnimationRate;
	private int busyIconIndex = 0;

	/**
	 * Constructs a panel that displays messages/progress/state properties of the {@code taskMonitor's} foreground task.
	 * 
	 * @param taskMonitor  the {@code TaskMonitor} whose {@code PropertyChangeEvents} {@code this StatusBar} will track.
	 */
	public StatusBar() {
		super(new GridBagLayout());
		setBorder(new EmptyBorder(4, 0, 4, 0)); // top, left, bottom, right
		messageLabel = new JLabel();
		helpLabel = new JLabel();
		progressBar = new JProgressBar(0, 100);
		statusAnimationLabel = new JLabel();

		busyAnimationRate = 10; // ResourceHelper.getInteger(ApplicationController.resourceBundle(),"busyAnimationRate");
		idleIcon = ResourceHelper.getIcon(Resources.getResourceBundle(), "idleIcon");
		for (int i = 0; i < busyIcons.length; i++) {
			busyIcons[i] = ResourceHelper.getIcon(Resources.getResourceBundle(),"busyIcons[" + i + "]");
		}
		busyIconTimer = new Timer(busyAnimationRate, new UpdateBusyIcon());
		progressBar.setEnabled(false);
		statusAnimationLabel.setIcon(idleIcon);

		GridBagConstraints c = new GridBagConstraints();

//		initGridBagConstraints(c);
//		c.gridwidth = GridBagConstraints.REMAINDER;
//		c.fill = GridBagConstraints.HORIZONTAL;
//		c.weightx = 1.0;
//		add(new JSeparator(), c);

		initGridBagConstraints(c);
		c.insets = new Insets(6, 6, 0, 3); // top, left, bottom, right;
		c.weightx = 1.0;
		c.fill = GridBagConstraints.HORIZONTAL;
		add(messageLabel, c);
		add(helpLabel, c);
		
		initGridBagConstraints(c);
		c.insets = new Insets(6, 3, 0, 3); // top, left, bottom, right;
		add(progressBar, c);

		initGridBagConstraints(c);
		c.insets = new Insets(6, 3, 0, 6); // top, left, bottom, right;
		add(statusAnimationLabel, c);
	}

	public void setMessage(Icon icon, String s) {
		messageLabel.setIcon(icon);
		messageLabel.setText((s == null) ? "" : s);
	}
	
	public void setHelp(String help) {
		if (StringUtils.isBlank(help)) {
			helpLabel.setText(null);
		} else {
			helpLabel.setText("F1: Hilfe verfügbar");
		}
	}
	
	public void setMessage(String s) {
		setMessage(null, s);
	}

	private void initGridBagConstraints(GridBagConstraints c) {
		c.anchor = GridBagConstraints.CENTER;
		c.fill = GridBagConstraints.NONE;
		c.gridwidth = 1;
		c.gridheight = 1;
		c.gridx = GridBagConstraints.RELATIVE;
		c.gridy = GridBagConstraints.RELATIVE;
		c.insets = zeroInsets;
		c.ipadx = 0;
		c.ipady = 0;
		c.weightx = 0.0;
		c.weighty = 0.0;
	}

	private class UpdateBusyIcon implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			busyIconIndex = (busyIconIndex + 1) % busyIcons.length;
			statusAnimationLabel.setIcon(busyIcons[busyIconIndex]);
		}
	}

	public void showBusyAnimation() {
		if (!busyIconTimer.isRunning()) {
			statusAnimationLabel.setIcon(busyIcons[0]);
			busyIconIndex = 0;
			busyIconTimer.start();
		}
	}

	public void stopBusyAnimation() {
		busyIconTimer.stop();
		statusAnimationLabel.setIcon(idleIcon);
	}

	/**
	 * The TaskMonitor (constructor arg) tracks a "foreground" task; this method is called each time a foreground task property changes.
	 */
	@Override
	public void propertyChange(PropertyChangeEvent e) {
		String propertyName = e.getPropertyName();
		if ("started".equals(propertyName)) {
			showBusyAnimation();
			progressBar.setEnabled(true);
			progressBar.setIndeterminate(true);
		} else if ("done".equals(propertyName)) {
			stopBusyAnimation();
			progressBar.setIndeterminate(false);
			progressBar.setEnabled(false);
			progressBar.setValue(0);
		} else if ("message".equals(propertyName)) {
			String text = (String) (e.getNewValue());
			setMessage(text);
		} else if ("progress".equals(propertyName)) {
			int value = (Integer) (e.getNewValue());
			progressBar.setEnabled(true);
			progressBar.setIndeterminate(false);
			progressBar.setValue(value);
		}
	}

	public static void showMessage(Component component, Icon icon, String message) {
		StatusBar statusBar = null;
		Component c = component;
		while (statusBar == null && c != null) {
			if (c instanceof JComponent) {
				JComponent jComponent = (JComponent)c;
				statusBar = (StatusBar)jComponent.getClientProperty(StatusBar.STATUS_BAR);
			}
			c = c.getParent();
		}
		if (statusBar != null) {
			statusBar.setMessage(icon, message);
		} else {
			JOptionPane.showMessageDialog(component, message);
		}
	}
	
//	public static Icon getIcon(Severity severity) {
//		if (severity != null && severity != Severity.OK) {
//			return ResourceHelper.getIcon(ApplicationController.resourceBundle(), severity.name() + ".icon");
//		} else {
//			return null;
//		}
//	}
}
