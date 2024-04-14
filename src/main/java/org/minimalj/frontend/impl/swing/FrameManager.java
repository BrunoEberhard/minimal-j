package org.minimalj.frontend.impl.swing;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.swing.JOptionPane;

import org.minimalj.application.Configuration;
import org.minimalj.frontend.impl.swing.toolkit.SwingFrontend;
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
	
	private List<SwingFrame> navigationFrames = new ArrayList<>();
	
	private FrameManager() {
	}
	
	//

	public static FrameManager getInstance() {
		return instance;
	}

	public SwingFrame openFrame() {
		SwingFrame frame = new SwingFrame();
		frame.setVisible(true);
		navigationFrames.add(frame);
		SwingFrontend.frameOpened(frame);
		return frame;
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
		if (Configuration.isDevModeActive()) {
			return true;
		} else {
			int answer = JOptionPane.showConfirmDialog(navigationFrameView, Resources.getString("Exit.confirm"), Resources.getString("Exit.confirm.title"), JOptionPane.YES_NO_OPTION);
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
