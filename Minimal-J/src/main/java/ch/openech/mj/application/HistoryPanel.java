package ch.openech.mj.application;

import ch.openech.mj.page.History;
import ch.openech.mj.page.Page;
import ch.openech.mj.page.History.HistoryListener;
import ch.openech.mj.toolkit.ClientToolkit;
import ch.openech.mj.toolkit.IComponentDelegate;
import ch.openech.mj.toolkit.SwitchLayout;

/**
 * A <code>JPanel<code> displaying the content of the actual page of a History.
 * After a page change the focus is set according the policy of the page.
 * 
 */
public class HistoryPanel implements IComponentDelegate {

	private final HistoryPanelListener listener;
	private final History<Page> history;
	private final SwitchLayout switchLayout;
	
	public HistoryPanel(HistoryPanelListener listener) {
		this.listener = listener;
		
		HistoryPanelHistoryListener historyListener = new HistoryPanelHistoryListener();
		history = new History<Page>(historyListener);
		
		switchLayout = ClientToolkit.getToolkit().createSwitchLayout();
	}

	@Override
	public Object getComponent() {
		return switchLayout;
	}

	//
	
	private class HistoryPanelHistoryListener implements HistoryListener {
		@Override
		public void onHistoryChanged() {
			show(history.getPresent());
			fireHistoryPanelChangedListener();
		}
		
		private void show(Page page) {
			switchLayout.show(page.getPanel());
			ClientToolkit.getToolkit().focusFirstComponent(page.getPanel());
		}
	}

	private void fireHistoryPanelChangedListener() {
		if (listener != null) {
			listener.onHistoryChanged();
		}
	}
	
	public interface HistoryPanelListener {
		public void onHistoryChanged();
	}
	
	// trait history
	
	// in Scala würde History als trait implementiert, hier sind die
	// Methoden über Eclipse generiert
	
	public void add(Page page) {
		history.add(page);
	}

	public void replace(Page page) {
		history.replace(page);
	}

	public Page getPresent() {
		return history.getPresent();
	}

	public boolean hasFuture() {
		return history.hasFuture();
	}

	public boolean hasPast() {
		return history.hasPast();
	}

	public void next() {
		history.next();
	}

	public void previous() {
		history.previous();
	}

	public void dropFuture() {
		history.dropFuture();
	}
}
