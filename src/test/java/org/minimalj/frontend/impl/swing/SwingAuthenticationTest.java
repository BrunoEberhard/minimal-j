package org.minimalj.frontend.impl.swing;

import java.awt.Component;
import java.awt.Container;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RunnableFuture;

import javax.swing.AbstractButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import org.junit.After;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.minimalj.application.Application;
import org.minimalj.application.Application.AuthenticatonMode;
import org.minimalj.application.Configuration;
import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.Frontend.IContent;
import org.minimalj.frontend.action.Action;
import org.minimalj.frontend.impl.swing.component.SwingCaption;
import org.minimalj.frontend.page.Page;
import org.minimalj.security.Subject;
import org.minimalj.security.UserPasswordAuthentication;
import org.minimalj.security.model.User;
import org.minimalj.test.TestUtil;
import org.minimalj.util.StringUtils;
import org.minimalj.util.resources.Resources;

@Ignore("Not working on travis")
public class SwingAuthenticationTest {

	@After
	public void shutdown() {
		TestUtil.shutdown();
	}

	@Test
	public void testAuthenticatonModeRequired() throws InterruptedException, InvocationTargetException {
		Swing.start(new TestApplication(AuthenticatonMode.REQUIRED));

		JDialog dialog = getDialog();
		Assert.assertEquals("Anmeldung", dialog.getTitle());

		setText(dialog, "UserPassword.user", "test");
		setText(dialog, "UserPassword.password", "test");

		click(dialog, "LoginAction");

		SwingFrame frame = getFrame();
		Assert.assertEquals("test", frame.getSubject().getName());

		Assert.assertNotNull(getComponent(frame, Resources.getString("ReloginAction")));
		Assert.assertNull(getComponent(frame, Resources.getString("LogoutAction")));

		click(frame, "Menu.window");
		click(frame, "ReloginAction");

		dialog = getDialog();
		Assert.assertEquals("Anmeldung", dialog.getTitle());
		AbstractButton button = findButton(dialog, Resources.getString("OpenNewWindow"));
	}

	@Test
	public void testAuthenticatonModeSuggested() throws InterruptedException, InvocationTargetException {
		Swing.start(new TestApplication(AuthenticatonMode.SUGGESTED));

		JDialog dialog = getDialog();
		Assert.assertEquals("Anmeldung", dialog.getTitle());

		setText(dialog, "UserPassword.user", "test");
		setText(dialog, "UserPassword.password", "test");

		click(dialog, "LoginAction");

		SwingFrame frame = getFrame();
		Assert.assertEquals("test", frame.getSubject().getName());

		Assert.assertNotNull(getComponent(frame, Resources.getString("ReloginAction")));
		Assert.assertNotNull(getComponent(frame, Resources.getString("LogoutAction")));

		SwingTab tab = frame.getVisibleTab();
		Page page = tab.getVisiblePage();
		Assert.assertTrue(page instanceof TestPage);

		NavigationTree navigationTree = getComponent(frame, NavigationTree.class);
		Assert.assertNotNull(navigationTree);
		// click(frame, "Menu.window");
		// click(frame, "NewWindowAction");
	}

	private void swing(Runnable r) {
		try {
			SwingUtilities.invokeAndWait(r);
		} catch (InvocationTargetException | InterruptedException e) {
			throw new RuntimeException(e);
		}
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

	private void setText(Component c, String resourceName, String text) {
		swing(() -> {
			findTextField(c, Resources.getString(resourceName)).setText(text);
		});
	}

	private void click(Component c, String resourceName) {
		swing(() -> {
			findButton(c, Resources.getString(resourceName)).doClick();
		});
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

	private JTextField findTextField(Component c, String caption) {
		Component input = getComponent(c, caption);
		Assert.assertNotNull("No found: " + caption, input);
		return (JTextField) input;
	}

	private AbstractButton findButton(Component c, String caption) {
		Component button = getComponent(c, caption);
		Assert.assertNotNull("No found: " + caption, button);
		return (AbstractButton) button;
	}

	private Component getComponent(Component c, String caption) {
		if (c instanceof JLabel) {
			JLabel label = (JLabel) c;
			if (StringUtils.equals(caption, label.getText())) {
				SwingCaption swingCaption = (SwingCaption) label.getParent();
				return swingCaption.getComponents()[1];
			}
		} else if (c instanceof AbstractButton) {
			AbstractButton button = (AbstractButton) c;
			if (StringUtils.equals(caption, button.getText())) {
				return button.isVisible() ? button : null;
			}
		} else if (c instanceof Container) {
			Container container = (Container) c;
			for (Component child : container.getComponents()) {
				Component result = getComponent(child, caption);
				if (result != null) {
					return result;
				}
			}
		}
		if (c instanceof JMenu) {
			JMenu menu = (JMenu) c;
			Component result = getComponent(menu.getPopupMenu(), caption);
			if (result != null) {
				return result;
			}
		}
		return null;
	}

	private <T extends Component> T getComponent(Component c, Class<T> clazz) {
		if (clazz.isAssignableFrom(c.getClass())) {
			return (T) c;
		} else if (c instanceof Container) {
			Container container = (Container) c;
			for (Component child : container.getComponents()) {
				Component result = getComponent(child, clazz);
				if (result != null) {
					return (T) result;
				}
			}
		}
		return null;
	}

	public static class TestApplication extends Application {
		private final AuthenticatonMode authenticatonMode;

		public TestApplication(AuthenticatonMode authenticatonMode) {
			this.authenticatonMode = authenticatonMode;
		}

		@Override
		public void initBackend() {
			Configuration.set("MjAuthentication", TestAuthentication.class.getName());
		}

		@Override
		public AuthenticatonMode getAuthenticatonMode() {
			return authenticatonMode;
		}

		@Override
		public Page createDefaultPage() {
			return new TestPage("TestPage");
		}
		
		@Override
		public List<Action> getNavigation() {
			List<Action> actions = new ArrayList<>();
			if (Subject.getCurrent() != null) {
				actions.add(new Action("Action with login") {
					@Override
					public void run() {
					}
				});
			} else {
				actions.add(new Action("Action without login") {
					@Override
					public void run() {
					}
				});
			}
			return actions;
		}
	}

	public static class TestAuthentication extends UserPasswordAuthentication {
		private static final long serialVersionUID = 1L;

		@Override
		protected User retrieveUser(String userName) {
			if (userName.length() >= 2) {
				User user = new User();
				user.name = userName;
				return user;
			} else {
				return null;
			}
		}

		protected User retrieveUser(String userName, char[] password) {
			return retrieveUser(userName);
		}
	}

	public static class TestPage extends Page {
		private final String text;

		public TestPage(String text) {
			this.text = text;
		}

		@Override
		public String getTitle() {
			return "Title Test Page";
		}

		@Override
		public IContent getContent() {
			String subjectName = Subject.getCurrent() != null ? Subject.getCurrent().getName() : "-";
			return Frontend.getInstance().createHtmlContent("<p>" + text + "</p><p>Subject: " + subjectName + "</p>");
		}
	}

}
