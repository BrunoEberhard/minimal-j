package org.minimalj.test.swing;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RunnableFuture;

import javax.swing.JDialog;
import javax.swing.SwingUtilities;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.minimalj.application.Application.AuthenticatonMode;
import org.minimalj.frontend.impl.swing.NavigationTree;
import org.minimalj.frontend.impl.swing.Swing;
import org.minimalj.frontend.impl.swing.SwingFrame;
import org.minimalj.frontend.impl.swing.SwingTab;
import org.minimalj.frontend.page.Page;
import org.minimalj.test.ApplicationTestFacade;
import org.minimalj.test.FrameTestFacade.NavigationTestFacade;
import org.minimalj.test.FrameTestFacade.PageContainerTestFacade;
import org.minimalj.test.FrameTestFacade.PageTestFacade;
import org.minimalj.test.FrameTestFacade.UserPasswordLoginTestFacade;
import org.minimalj.test.TestApplication;
import org.minimalj.test.TestApplication.TestPage;
import org.minimalj.test.TestUtil;
import org.minimalj.util.resources.Resources;

public class SwingAuthenticationTest {

	@After
	public void shutdown() {
		TestUtil.shutdown();
	}

	@Test
	public void testAuthenticatonModeRequired() throws InterruptedException, InvocationTargetException {
		Swing.start(new TestApplication(AuthenticatonMode.REQUIRED));

		ApplicationTestFacade application = new SwingTestFacade();
		
		UserPasswordLoginTestFacade userPasswordLogin = application.getLoginTestFacade();

		userPasswordLogin.setUser("test");
		userPasswordLogin.setPassword("test");

		userPasswordLogin.login();

		PageContainerTestFacade pageContainer = application.getCurrentWindowTestFacade();
		
		NavigationTestFacade navigation = pageContainer.getNavigation();
		
		List<PageTestFacade> pages = pageContainer.getPages();
		Assert.assertEquals(1, pages.size());
		
		PageTestFacade textPage = pages.get(0);
		Assert.assertTrue(textPage.contains("Subject: test"));
		
		
//		Assert.assertNotNull(SwingTestUtils.getComponent(frame, Resources.getString("ReloginAction")));
//		Assert.assertNull(SwingTestUtils.getComponent(frame, Resources.getString("LogoutAction")));
//
//		SwingTestUtils.click(frame, "Menu.window");
//		SwingTestUtils.click(frame, "ReloginAction");
//
//		dialog = getDialog();
//		Assert.assertEquals("Anmeldung", dialog.getTitle());
		
		// AbstractButton button = SwingTestUtils.findButton(dialog, Resources.getString("OpenNewWindow"));
	}

	@Test
	public void testAuthenticatonModeSuggested() throws InterruptedException, InvocationTargetException {
		Swing.start(new TestApplication(AuthenticatonMode.SUGGESTED));

		JDialog dialog = getDialog();
		Assert.assertEquals("Anmeldung", dialog.getTitle());

		SwingTestUtils.setText(dialog, "UserPassword.user", "test");
		SwingTestUtils.setText(dialog, "UserPassword.password", "test");

		SwingTestUtils.click(dialog, "LoginAction");

		SwingFrame frame = getFrame();
		Assert.assertEquals("test", frame.getSubject().getName());

		Assert.assertNotNull(SwingTestUtils.getComponent(frame, Resources.getString("ReloginAction")));
		Assert.assertNotNull(SwingTestUtils.getComponent(frame, Resources.getString("LogoutAction")));

		SwingTab tab = frame.getVisibleTab();
		Page page = tab.getVisiblePage();
		Assert.assertTrue(page instanceof TestPage);

		NavigationTree navigationTree = SwingTestUtils.getComponent(frame, NavigationTree.class);
		Assert.assertNotNull(navigationTree);
		// click(frame, "Menu.window");
		// click(frame, "NewWindowAction");
	}
	
	@Test
	public void testAuthenticatonModeOptional() throws InterruptedException, InvocationTargetException {
		Swing.start(new TestApplication(AuthenticatonMode.OPTIONAL));
		
		ApplicationTestFacade application = new SwingTestFacade();
		PageContainerTestFacade pageContainer = application.getCurrentWindowTestFacade();
		
		NavigationTestFacade navigation = pageContainer.getNavigation();
		Assert.assertNotNull(navigation.get("ActionWithoutLogin"));
	}

	private <T> T swing(Callable<T> callable) {
		RunnableFuture<T> task = new FutureTask<>(callable);
		SwingUtilities.invokeLater(task);
		try {
			return task.get();
		} catch (InterruptedException | ExecutionException e) {
			throw new RuntimeException(e);
		}
	}

	private JDialog getDialog() {
		return swing(() -> {
			Assert.assertEquals("There should be 1 open dialog", 1, Arrays.stream(JDialog.getWindows()).filter(w -> w instanceof JDialog).filter(w -> w.isVisible()).count());
			return (JDialog) Arrays.stream(JDialog.getWindows()).filter(w -> w instanceof JDialog).filter(w -> w.isVisible()).findFirst().get();
		});
	}

	private SwingFrame getFrame() {
		return swing(() -> {
			Assert.assertEquals("There should be 1 open frame", 1, Arrays.stream(JDialog.getWindows()).filter(w -> w instanceof SwingFrame).filter(w -> w.isVisible()).count());
			return (SwingFrame) Arrays.stream(JDialog.getWindows()).filter(w -> w instanceof SwingFrame).filter(w -> w.isVisible()).findFirst().get();
		});
	}

}