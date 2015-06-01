package org.minimalj.frontend.swing;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.LayoutManager;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.lang.reflect.InvocationTargetException;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;

import org.minimalj.frontend.page.Page;
import org.minimalj.frontend.swing.component.EditablePanel;
import org.minimalj.frontend.swing.component.History;
import org.minimalj.frontend.swing.component.History.HistoryListener;
import org.minimalj.frontend.swing.toolkit.SwingSwitchContent;
import org.minimalj.frontend.toolkit.ClientToolkit.IContent;
import org.minimalj.frontend.toolkit.ClientToolkit.ITable;

public class SwingTab extends EditablePanel {
	private static final long serialVersionUID = 1L;
	
	final SwingFrame frame;
	final Action previousAction, nextAction, refreshAction;
	final Action closeTabAction;

	private final SwingToolBar toolBar;
	private final SwingMenuBar menuBar;
	private final SwingSwitchContent switchContent;
	private final JSplitPane splitPane;
	private final JPanel verticalPanel;
	
	private final History<Page> history;
	private final SwingPageContextHistoryListener historyListener;

	private Page page;

	public SwingTab(SwingFrame frame) {
		super();
		this.frame = frame;

		historyListener = new SwingPageContextHistoryListener();
		history = new History<>(historyListener);

		previousAction = new PreviousPageAction();
		nextAction = new NextPageAction();
		refreshAction = new RefreshAction();

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
		
		splitPane = new JSplitPane();
		verticalPanel = new JPanel(new VerticalLayoutManager());
		splitPane.setRightComponent(verticalPanel);
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
		} else {
			previousAction.setEnabled(false);
			nextAction.setEnabled(false);
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
			
			IContent content = page.getContent();
			if (content instanceof ITable) {
				switchContent.show((JComponent) content);
			} else if (content != null) {
				splitPane.setLeftComponent(null);
				verticalPanel.removeAll();
				verticalPanel.add((JComponent) content, "");
			} else {
				switchContent.show((JComponent) null);
			}
			
			SwingTab.this.onHistoryChanged();
		}
	}

//	private void updateContent() {
//		if (pages.isEmpty()) {
//			switchContent.show((JComponent) null);
//			return;
//		}
//		
//		Page firstPage = pages.get(0);
//		IContent firstContent = firstPage.getContent();
//
//		if (pages.size() == 1 && firstContent == null) {
//			switchContent.show((JComponent) null);
//			return;
//		}
//		
//		JScrollPane firstComponent = new JScrollPane(new ScrollablePanel((JComponent) firstContent));
//		firstComponent.setBorder(null);
//		
//		if (pages.size() == 1 && firstContent instanceof ITable) {
//			switchContent.show(firstComponent);
//			return;
//		}
//		
//		JSplitPane splitPane = new JSplitPane();
//		splitPane.setBorder(BorderFactory.createEmptyBorder());
//		
//		if (pages.size() == 1) {
//			splitPane.setLeftComponent(new JPanel());
//			splitPane.setRightComponent(firstComponent);
//		} else {
//			boolean firstContentIsTable = firstContent instanceof ITable;
//			if (firstContentIsTable) {
//				splitPane.setLeftComponent(firstComponent);
//			} else {
//				splitPane.setLeftComponent(new JPanel());
//			}
//			JPanel rightComponent = new JPanel(new VerticalLayoutManager());
//			for (int i = firstContentIsTable ? 1 : 0; i<pages.size(); i++) {
//				IContent content = pages.get(i).getContent();
//				rightComponent.add((Component) content, "");
//			}
//			JScrollPane rightScrollPane = new JScrollPane(rightComponent);
//			rightScrollPane.setBorder(null);
//			splitPane.setRightComponent(rightScrollPane);
//		}
//		switchContent.show(splitPane);
//
//	}
	
	
	private class VerticalLayoutManager implements LayoutManager {

		private Dimension size;
		private Rectangle lastParentBounds = null;
		
		public VerticalLayoutManager() {
		}

		@Override
		public Dimension preferredLayoutSize(Container parent) {
			return new Dimension(100, 100);
		}

		@Override
		public Dimension minimumLayoutSize(Container parent) {
			return new Dimension(100, 100);
		}

		@Override
		public void layoutContainer(Container parent) {
			if (lastParentBounds != null && lastParentBounds.equals(parent.getBounds())) return;
			lastParentBounds = parent.getBounds();
			
			int y = 4;
			int x = 1;
			int width = parent.getWidth();
			int widthWithoutIns = width - x;
			for (Component component : parent.getComponents()) {
				int height = component.getPreferredSize().height;
				component.setBounds(x, y, widthWithoutIns, height);
				y += height;
			}
			size = new Dimension(width, y);
		}

		@Override
		public void addLayoutComponent(String name, Component comp) {
			lastParentBounds = null;
		}

		@Override
		public void removeLayoutComponent(Component comp) {
			lastParentBounds = null;
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

	public void show(Page page) {
		show(page, true);
	}
	
	public void show(Page page, boolean asTopPage) {
		if (!SwingUtilities.isEventDispatchThread()) {
			try {
				SwingUtilities.invokeAndWait(new Runnable() {
					@Override
					public void run() {
						show(page, asTopPage);
					};
				});
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}
		} else {
			if (asTopPage) {
				history.add(page);
			} else {
				Component firstComponent = switchContent.getComponent(0);
				if (firstComponent != splitPane) {
					splitPane.setLeftComponent(firstComponent);
					switchContent.show(splitPane);
				}
				
				verticalPanel.removeAll();
				verticalPanel.add((JComponent) page.getContent(), "");
				verticalPanel.revalidate();
			}
		}
	}
	
	public boolean tryToClose() {
		return tryToCloseDialogs();
	}

}
