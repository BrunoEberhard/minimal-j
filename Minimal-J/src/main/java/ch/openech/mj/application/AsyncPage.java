package ch.openech.mj.application;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.Icon;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.Timer;

import ch.openech.mj.page.Page;
import ch.openech.mj.page.PageContext;
import ch.openech.mj.resources.ResourceHelper;
import ch.openech.mj.swing.component.PanzerGlassPane;
import ch.openech.mj.swing.toolkit.SwingComponentDelegate;
import ch.openech.mj.toolkit.IComponent;

// TODO migrate AsyncPage
@Deprecated
public abstract class AsyncPage extends Page {

	public AsyncPage(PageContext context) {
		super(context);
	}

	private PanzerGlassPane glassPane;
	private SwingWorker<?, ?> swingWorker;
	private Timer timer;
	private int animationPosition;
	private Icon iconBeforeAnimation;
	
	/**
	 * Use createPanel to define content of Page
	 */
	@Override
	public final IComponent getPanel() {
		if (glassPane == null) {
			glassPane = new PanzerGlassPane();
			// glassPane should be ready on the call of createPanel
			// so don't use glassPane = new PanzerGlassPane(createPanel());
			glassPane.setContent((Component) createPanel());
		}
		return new SwingComponentDelegate(glassPane);
	}

	protected abstract Object createPanel();

	protected void execute(SwingWorker<?, ?> swingWorker, String workName) {
		if (this.swingWorker != null) {
			throw new IllegalStateException("Only one SwingWorker can be at work");
		}
		if (!SwingUtilities.isEventDispatchThread()) {
			throw new IllegalStateException("SwingWork can only be executed from EventDispatchThread");
		}
		this.swingWorker = swingWorker;
		swingWorker.addPropertyChangeListener(new PageSwingWorkerListener());
		glassPane.setBlocked(true);
		startAnimation();
		firePageWorkStart(workName);
		swingWorker.execute();
	}
	
	public boolean isWorking() {
		return glassPane.isBlocked();
	}
	
	private class PageSwingWorkerListener implements PropertyChangeListener {

		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			boolean cancelledOrDone = swingWorker.isCancelled() || swingWorker.isDone();
			glassPane.setBlocked(!cancelledOrDone);
			if (cancelledOrDone) {
				stopAnimation();
				swingWorker.removePropertyChangeListener(this);
				swingWorker = null;
				firePageWorkEnd();
			}
		}
	}
	
	public void stop() {
		if (swingWorker != null) {
			swingWorker.cancel(true);
		}
	}
	
	public void startAnimation() {
		if (timer == null || !timer.isRunning()) {
			if (timer == null) {
				timer = new Timer(60, new ActionListener() {
					
					@Override
					public void actionPerformed(ActionEvent e) {
						animationPosition = (animationPosition + 1) % 15;
						String iconKey = "busy-icon" + animationPosition + ".png";
						Icon icon = ResourceHelper.getIcon(iconKey);
						setTitleIcon(icon);
					}
				});
			}
			iconBeforeAnimation = getTitleIcon();
			timer.start();
		}
	}
	
	public void stopAnimation() {
		if (timer != null && timer.isRunning()) {
			timer.stop();
			setTitleIcon(iconBeforeAnimation);
		}
	}
	
	public interface PageWorkListener {

		public void onPageWorkStart(String workName);
		
		public void onPageWorkEnd();
	}
}
