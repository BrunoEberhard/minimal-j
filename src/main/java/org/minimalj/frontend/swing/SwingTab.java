package org.minimalj.frontend.swing;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import javax.swing.Action;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.minimalj.frontend.page.Page;
import org.minimalj.frontend.swing.component.EditablePanel;
import org.minimalj.frontend.swing.component.History;
import org.minimalj.frontend.swing.component.History.HistoryListener;
import org.minimalj.frontend.swing.toolkit.ScrollablePanel;
import org.minimalj.frontend.swing.toolkit.SwingFormAlignLayoutManager;
import org.minimalj.frontend.swing.toolkit.SwingScrollPane;
import org.minimalj.frontend.swing.toolkit.SwingSwitchContent;
import org.minimalj.frontend.toolkit.ClientToolkit.IContent;
import org.minimalj.frontend.toolkit.FormContent;

public class SwingTab extends EditablePanel {
	private static final long serialVersionUID = 1L;
	
	final SwingFrame frame;
	final Action previousAction, nextAction, refreshAction, upAction, downAction;
	final Action closeTabAction;

	private final SwingToolBar toolBar;
	private final SwingMenuBar menuBar;
	private final SwingSwitchContent switchContent;
	
	private final History<Page> history;
	private final SwingPageContextHistoryListener historyListener;

	private Page page;
	private List<Page> pages;
	private int indexInPageLinks;

	public SwingTab(SwingFrame frame) {
		super();
		this.frame = frame;

		historyListener = new SwingPageContextHistoryListener();
		history = new History<>(historyListener);

		previousAction = new PreviousPageAction();
		nextAction = new NextPageAction();
		refreshAction = new RefreshAction();
		upAction = new UpAction();
		downAction = new DownAction();

		closeTabAction = new CloseTabAction();
		
		toolBar = new SwingToolBar(this);
		menuBar = new SwingMenuBar(this);

		JPanel outerPanel = new JPanel(new BorderLayout());
		outerPanel.add(menuBar, BorderLayout.NORTH);
		JPanel panel = new JPanel(new BorderLayout());
		outerPanel.add(panel, BorderLayout.CENTER);
		panel.add(toolBar, BorderLayout.NORTH);
		switchContent = new SwingSwitchContent();
		panel.add(switchContent, BorderLayout.CENTER);
		setContent(outerPanel);
	}
	
	public static SwingTab getActiveTab() {
		Window w = SwingFrame.getActiveWindow();
		if (w instanceof SwingFrame) {
			return ((SwingFrame) w).getVisibleTab();
		}
		return null;
	}

	public Page getVisiblePage() {
		return page;
	}
	
	void onHistoryChanged() {
		updateActions();
		menuBar.onHistoryChanged();
		toolBar.onHistoryChanged();
		frame.onHistoryChanged();
	}

	protected void updateActions() {
		if (getVisiblePage() != null) {
			previousAction.setEnabled(hasPast());
			nextAction.setEnabled(hasFuture());
			upAction.setEnabled(!top());
			downAction.setEnabled(!bottom());
		} else {
			previousAction.setEnabled(false);
			nextAction.setEnabled(false);
			upAction.setEnabled(false);
			downAction.setEnabled(false);
		}
	}
 
	//
	
	protected class PreviousPageAction extends SwingResourceAction {
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent e) {
			previous();
		}
	}
	
	protected class NextPageAction extends SwingResourceAction {
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent e) {
			next();
		}
	}

	protected class RefreshAction extends SwingResourceAction {
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent e) {
			refresh();
		}
	}
	
	public void refresh() {
		replace(getVisiblePage());
	}

	private class UpAction extends SwingResourceAction {
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent e) {
			up();
		}
	}

	private class DownAction extends SwingResourceAction {
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent e) {
			down();
		}
	}

	private class CloseTabAction extends SwingResourceAction {
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent e) {
			frame.closeTab();
		}
	}
	
	// PageContext
	
	private class SwingPageContextHistoryListener implements HistoryListener {
		@Override
		public void onHistoryChanged() {
			page = history.getPresent();
			show(page);
			SwingTab.this.onHistoryChanged();
		}

		private void show(Page page) {
			IContent content = page.getContent();
			if (content instanceof FormContent) {
				JPanel panel = new JPanel(new SwingFormAlignLayoutManager());
				panel.add((Component)content);
				content = new SwingScrollPane(new ScrollablePanel(panel));
			}
			switchContent.show(content);
		}
	}
	
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

	public void show(final Page page) {
		if (!SwingUtilities.isEventDispatchThread()) {
			try {
				SwingUtilities.invokeAndWait(new Runnable() {
					@Override
					public void run() {
						show(page);
					};
				});
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}
		} else {
			if (pages != null && !pages.contains(page)) {
				pages = null;
			}
			add(page);
		}
	}
	
	public void show(List<Page> pages, int index) {
		this.pages = pages;
		this.indexInPageLinks = index;
		show(pages.get(indexInPageLinks));
	}

	public boolean top() {
		return pages == null || indexInPageLinks == 0;
	}

	public boolean bottom() {
		return pages == null || indexInPageLinks == pages.size() - 1;
	}

	public void up() {
		replace(pages.get(--indexInPageLinks));
	}

	public void down() {
		replace(pages.get(++indexInPageLinks));
	}

	public boolean tryToClose() {
		return tryToCloseDialogs();
	}

}
