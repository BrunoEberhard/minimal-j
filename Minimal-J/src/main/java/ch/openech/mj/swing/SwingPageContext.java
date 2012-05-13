package ch.openech.mj.swing;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;

import ch.openech.mj.application.ApplicationContext;
import ch.openech.mj.page.Page;
import ch.openech.mj.page.Page.PageListener;
import ch.openech.mj.page.PageContext;
import ch.openech.mj.swing.component.EditablePanel;
import ch.openech.mj.swing.component.History;
import ch.openech.mj.swing.component.History.HistoryListener;
import ch.openech.mj.toolkit.ClientToolkit;
import ch.openech.mj.toolkit.IComponent;
import ch.openech.mj.toolkit.SwitchLayout;

// TODO Swing PageContext should ensure use of EventDispatchThread in every need case
public class SwingPageContext extends EditablePanel implements PageContext, PageListener, IComponent {
	private final SwingFrame frame;
	private final History<Page> history;
	private final SwingPageContextHistoryListener historyListener;
	private final SwitchLayout switchLayout;

	private List<String> pageLinks;
	private int indexInPageLinks;

	private class SwingPageContextHistoryListener implements HistoryListener {
		@Override
		public void onHistoryChanged() {
			show(history.getPresent());
			frame.onHistoryChanged();
		}

		private void show(Page page) {
			switchLayout.show(page.getPanel());
			ClientToolkit.getToolkit().focusFirstComponent(page.getPanel());
		}
	}

	public SwingPageContext(SwingFrame frame) {
		this.frame = frame;

		historyListener = new SwingPageContextHistoryListener();
		history = new History<Page>(historyListener);

		switchLayout = ClientToolkit.getToolkit().createSwitchLayout();
		setContent((JComponent) switchLayout);
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

	public void dropFuture() {
		history.dropFuture();
	}

	@Override
	public PageContext addTab() {
		return frame.addTab();
	}

	@Override
	public void closeTab() {
		if (hasPast()) {
			previous();
			dropFuture();
		} else {
			frame.closeTab(this);
		}
	}

	@Override
	public void show(final String pageLink) {
		if (!SwingUtilities.isEventDispatchThread()) {
			try {
				SwingUtilities.invokeAndWait(new Runnable() {
					@Override
					public void run() {
						show(pageLink);
					};
				});
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}
		} else {
			if (pageLinks != null && !pageLinks.contains(pageLink)) {
				pageLinks = null;
			}
			if (getPresent() != null && getPresent().isExclusive()) {
				PageContext newPageContext = addTab();
				newPageContext.show(pageLink);
			} else {
				Page page = Page.createPage(this, pageLink);
				add(page);
			}
		}
	}

	@Override
	public void show(List<String> pageLinks, int index) {
		this.pageLinks = pageLinks;
		this.indexInPageLinks = index;
		show(pageLinks.get(indexInPageLinks));
	}

	public boolean top() {
		return pageLinks == null || indexInPageLinks == 0;
	}

	public boolean bottom() {
		return pageLinks == null || indexInPageLinks == pageLinks.size() - 1;
	}

	public void up() {
		Page page = Page.createPage(this, pageLinks.get(--indexInPageLinks));
		replace(page);
	}

	public void down() {
		Page page = Page.createPage(this, pageLinks.get(++indexInPageLinks));
		replace(page);
	}

	@Override
	public void onPageTitleChanged(Page page) {
		frame.onPageTitleChanged(this, page);
	}

	@Override
	public IComponent getComponent() {
		return this;
	}

	@Override
	public ApplicationContext getApplicationContext() {
		return SwingApplication.getApplicationContext();
	}

}