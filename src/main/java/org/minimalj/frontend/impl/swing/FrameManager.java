package org.minimalj.frontend.impl.swing;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JOptionPane;
import javax.swing.UIManager;

import org.minimalj.application.DevMode;
import org.minimalj.security.LoginAction;
import org.minimalj.security.Subject;
import org.minimalj.util.resources.Resources;

/**
 * Manages:
 * <OL>
 * <LI>The open and closing of Frames
 * <LI>Can refresh all open Frames (reason for being refreshable)
 * </OL>
 * 
 * @author bruno
 * 
 */
public class FrameManager {
	private static Logger logger = Logger.getLogger(FrameManager.class.getName());
	private static FrameManager instance = new FrameManager();
	
	private List<SwingFrame> navigationFrames = new ArrayList<SwingFrame>();
	
	private FrameManager() {
	}
	
	//
	
	/**
	 * Sets the Look and Feel to the system default and sets the
	 * property "apple.laf.useScreenMenuBar" to true (mac specific)
	 */
	public static void setSystemLookAndFeel() {
		try {
			System.setProperty("apple.laf.useScreenMenuBar", "true");
			System.setProperty("com.apple.mrj.application.apple.menu.about.name", Resources.getString(Resources.APPLICATION_TITLE));
			
			String name = UIManager.getSystemLookAndFeelClassName();
			UIManager.setLookAndFeel(name);
		} catch (Exception e) {
			String s = "Couldn't set system LookandFeel";
			logger.log(Level.WARNING, s, e);
		}
	}
	
	public static FrameManager getInstance() {
		return instance;
	}

	public void openNavigationFrame(Subject subject) {
		boolean authorizationAvailable = subject != null;
		final SwingFrame frame = new SwingFrame(authorizationAvailable);
		frame.setVisible(true);
		navigationFrames.add(frame);
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosed(WindowEvent e) {
				navigationFrames.remove(frame);
			}
		});
		if (subject != null && !subject.isValid()) {
			SwingFrame.activeFrameOverride = frame;
			new LoginAction(subject) {
				@Override
				public void cancel() {
					frame.closeWindow();
				};
				@Override
				public String getName() {
					return Resources.getString(Resources.getActionResourceName(LoginAction.class));
				};
			}.action();
			SwingFrame.activeFrameOverride = null;
		} else {
			frame.setSubject(subject);
		}
	}
	
	public List<SwingFrame> getNavigationFrames() {
		return navigationFrames;
	}
	
	// closing
	
	/**
	 * Asks the use about closing and then tries to close the Window
	 * 
	 * @param frameView NavigationFrameView to close
	 */
	public void closeWindowPerformed(SwingFrame frameView) {
		if (!navigationFrames.contains(frameView)) return;
		
		if (navigationFrames.size() == 1 && !askBeforeExit(frameView)) {
			return;
		}
		
		if (frameView.tryToCloseWindow()) {
			removeNavigationFrameView(frameView);
		}
	}
	
	/**
	 * Asks the use about leaving and then tries to close all Windows
	 * 
	 * @param navigationFrameView used as parent of question dialog
	 */
	public void exitActionPerformed(SwingFrame navigationFrameView) {
		if (askBeforeExit(navigationFrameView)) {
			// will not return if last Frame is closed
			closeAllWindows(navigationFrameView);
		}
	}
	
	/**
	 * Closes the NavigationFrameView without further questions. This
	 * is done when the user closes the last tab of a window
	 * 
	 * @param frameView NavigationFrameView to close
	 */
	public void lastTabClosed(SwingFrame frameView) {
		frameView.closeWindow();
		removeNavigationFrameView(frameView);
	}
	
	/**
	 * Ask about leaving the application if there is only one window left
	 * 
	 * @param navigationFrameView used as parent of question dialog
	 * @return true if user wants to leave
	 */
	public boolean askBeforeCloseLastWindow(SwingFrame navigationFrameView) {
		if (navigationFrames.size() == 1) {
			return askBeforeExit(navigationFrameView);
		} else {
			return true;
		}
	}

	/**
	 * Asks about leaving the application
	 * 
	 * @param navigationFrameView used as parent of question dialog
	 * @return true if user wants to leave
	 */
	private boolean askBeforeExit(SwingFrame navigationFrameView) {
		if (DevMode.isActive()) {
			return true;
		} else {
			int answer = JOptionPane.showConfirmDialog(navigationFrameView, "Soll die Applikation beendet werden?", "Applikation beenden", JOptionPane.YES_NO_OPTION);
			return answer == JOptionPane.YES_OPTION;
		}
	}
	
	/**
	 * Tries to close all windows. Stops at first failed attempt.
	 * 
	 * @param navigationFrameView used as parent of question dialogs
	 * @return true if all windows are closed
	 */
	private boolean closeAllWindows(SwingFrame navigationFrameView) {
		// Die umgekehrte Reihenfolge fühlt sich beim Schliessen natürlicher an
		for (int i = navigationFrames.size()-1; i>= 0; i--) {
			SwingFrame frameView = navigationFrames.get(i);
			if (!frameView.tryToCloseWindow()) return false;
			removeNavigationFrameView(frameView);
		}
		return true;
	}

	/**
	 * Removes a frameView from the list and exits if it was the last
	 * 
	 * @param frameView NavigationFrameView to remove from the list
	 */
	private void removeNavigationFrameView(SwingFrame frameView) {
		navigationFrames.remove(frameView);
		if (navigationFrames.size() == 0) {
			System.exit(0);
		}
	}

}
