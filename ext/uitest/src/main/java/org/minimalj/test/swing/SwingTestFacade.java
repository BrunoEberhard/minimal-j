package org.minimalj.test.swing;

import java.awt.Component;
import java.awt.Container;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import javax.swing.AbstractButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import org.junit.Assert;
import org.minimalj.frontend.impl.swing.NavigationTree;
import org.minimalj.frontend.impl.swing.SwingFrame;
import org.minimalj.frontend.impl.swing.SwingTab;
import org.minimalj.frontend.impl.swing.SwingToolBar;
import org.minimalj.frontend.impl.swing.component.SwingHtmlContent;
import org.minimalj.frontend.impl.swing.toolkit.SwingDialog;
import org.minimalj.frontend.impl.swing.toolkit.SwingFrontend.SwingActionText;
import org.minimalj.test.ApplicationTestFacade;
import org.minimalj.test.LoginFrameFacade.UserPasswordLoginTestFacade;
import org.minimalj.test.PageContainerTestFacade;
import org.minimalj.test.PageContainerTestFacade.ActionTestFacade;
import org.minimalj.test.PageContainerTestFacade.DialogTestFacade;
import org.minimalj.test.PageContainerTestFacade.FormTestFacade;
import org.minimalj.test.PageContainerTestFacade.NavigationTestFacade;
import org.minimalj.test.PageContainerTestFacade.PageTestFacade;
import org.minimalj.test.PageContainerTestFacade.TableTestFacade;
import org.minimalj.util.resources.Resources;

public class SwingTestFacade implements ApplicationTestFacade {

	@Override
	public UserPasswordLoginTestFacade getLoginTestFacade() {
		Optional<SwingDialog> loginDialog = Arrays.stream(JDialog.getWindows()).filter(SwingDialog.class::isInstance).map(SwingDialog.class::cast).filter(w -> w.isVisible()).findFirst();
		Assert.assertTrue(loginDialog.isPresent());
		
		return new SwingLoginTestFacade(loginDialog.get());
	}
	
	@Override
	public PageContainerTestFacade getCurrentPageContainerTestFacade() {
		Optional<SwingFrame> frame = Arrays.stream(JFrame.getWindows()).filter(SwingFrame.class::isInstance).map(SwingFrame.class::cast).filter(w -> w.isVisible()).findFirst();
		Assert.assertTrue(frame.isPresent());
		
		return new SwingFrameTestFacade(frame.get());
	}

	private static class SwingLoginTestFacade implements UserPasswordLoginTestFacade {
		private final SwingDialog swingDialog;
		
		public SwingLoginTestFacade(SwingDialog swingDialog) {
			this.swingDialog = swingDialog;
			
			Assert.assertEquals(Resources.getString("Login.title"), swingDialog.getTitle());
		}

		@Override
		public boolean hasSkipLogin() {
			Component button = SwingTestUtils.getComponent(swingDialog, Resources.getString("SkipLoginAction"));
			return button != null;
		}

		@Override
		public boolean hasClose() {
			return true;
		}

		@Override
		public void login() {
			SwingTestUtils.click(swingDialog, "LoginAction");
		}

		@Override
		public void cancel() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void close() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void setUser(String name) {
			SwingTestUtils.setText(swingDialog, "UserPassword.user", name);
			
		}

		@Override
		public void setPassword(String password) {
			SwingTestUtils.setText(swingDialog, "UserPassword.password", password);
		}
	}
	
	@Override
	public void logout() {
		// TODO Auto-generated method stub
	}

	private static class SwingFrameTestFacade implements PageContainerTestFacade {
		private final SwingFrame frame;
		
		public SwingFrameTestFacade(SwingFrame frame) {
			this.frame = frame;
		}

		@Override
		public NavigationTestFacade getNavigation() {
			NavigationTree navigationTree = SwingTestUtils.getComponent(frame, NavigationTree.class);
			return new SwingNavigationTestFacade(navigationTree);
		}

		@Override
		public List<PageTestFacade> getPages() {
			SwingTab tab = frame.getVisibleTab();
			JPanel panel = (JPanel) SwingTestUtils.getComponent(tab, c -> 
			{
				if (c instanceof Container) {
					return ((Container) c).getLayout() instanceof org.minimalj.frontend.impl.swing.SwingTab.VerticalLayoutManager;
				} else {
					return false;
				}
			});
			
			List<PageTestFacade> pages = new ArrayList<>();
			for (Component c : panel.getComponents()) {
				Component content;
				String title;
				if (c instanceof JTabbedPane) {
					content = ((JTabbedPane) c).getComponentAt(0);
					title = ((JTabbedPane) c).getTitleAt(0);
				} else {
					content = c;
					while (!(c instanceof JTabbedPane)) {
						c = c.getParent();
					}
					JTabbedPane container = (JTabbedPane) c;
					title = container.getTitleAt(container.getSelectedIndex());
				}
				if (content instanceof SwingHtmlContent) {
					SwingHtmlContent htmlContent = (SwingHtmlContent) content;
//					pages.add(new TextPageTestFacade(title, htmlContent.getText()));
					pages.add(new SwingPageTestFacade(title, htmlContent));
				}
			}
			
			return pages;
		}

		@Override
		public DialogTestFacade getDialog() {
			Optional<SwingDialog> dialog = Arrays.stream(JFrame.getWindows()).
					filter(SwingDialog.class::isInstance).map(SwingDialog.class::cast).
					filter(d -> d.getOwnedWindows().length == 0).findFirst();
			
			return dialog.isPresent() ? new SwingDialogTestFacade(dialog.get()) : null;
		}
		
		@Override
		public ActionTestFacade getBack() {
			SwingToolBar toolBar = SwingTestUtils.getComponent(frame, SwingToolBar.class);
			AbstractButton button = (AbstractButton) SwingTestUtils.getComponent(toolBar, Resources.getString("PreviousPageAction"));
			return new SwingButtonActionTestFacade(button);
		}

		@Override
		public ActionTestFacade getForward() {
			// TODO Auto-generated method stub
			return null;
		}
	}

	private static class SwingDialogTestFacade implements DialogTestFacade {
		private final SwingDialog swingDialog;
		
		public SwingDialogTestFacade(SwingDialog swingDialog) {
			this.swingDialog = swingDialog;
		}

		@Override
		public FormTestFacade getForm() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public ActionTestFacade getAction(String caption) {
			// TODO Auto-generated method stub
			return null;
		}
	}
	
	private static class SwingNavigationTestFacade implements NavigationTestFacade {
		private final NavigationTree navigationTree;
		
		public SwingNavigationTestFacade(NavigationTree navigationTree) {
			this.navigationTree = navigationTree;
		}

		@Override
		public Runnable get(String resourceName) {
			String caption = Resources.getString(resourceName);
			SwingActionText actionText = (SwingActionText) SwingTestUtils.getComponent(navigationTree, caption);
			if (actionText != null) {
				return () -> SwingTestUtils.click(actionText);
			} else {
				return null;
			}
		}
		
	}
	
	private static class SwingButtonActionTestFacade implements ActionTestFacade {
		private final AbstractButton button;
		
		public SwingButtonActionTestFacade(AbstractButton button) {
			this.button = button;
		}

		@Override
		public void run() {
			SwingTestUtils.click(button);
		}

		@Override
		public boolean isEnabled() {
			return button.isEnabled() && button.isVisible();
		}
	}

	private static class SwingPageTestFacade implements PageTestFacade {
		private final String title;
		private final Component content;
		
		public SwingPageTestFacade(String title, Component content) {
			super();
			this.title = title;
			this.content = content;
		}

		@Override
		public String getTitle() {
			return title;
		}

		@Override
		public NavigationTestFacade getContextMenu() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void executeQuery(String query) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public TableTestFacade getTable() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public FormTestFacade getForm() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public boolean contains(String string) {
			if (content instanceof SwingHtmlContent) {
				SwingHtmlContent htmlContent = (SwingHtmlContent) content;
				return htmlContent.getText().contains(string);
			}
			return false;
		}
	}

}
