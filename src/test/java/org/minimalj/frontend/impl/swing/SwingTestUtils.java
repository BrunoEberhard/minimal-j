package org.minimalj.frontend.impl.swing;

import java.awt.Component;
import java.awt.Container;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RunnableFuture;
import java.util.function.Predicate;

import javax.swing.AbstractButton;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import org.junit.Assert;
import org.minimalj.frontend.impl.swing.component.SwingCaption;
import org.minimalj.util.StringUtils;
import org.minimalj.util.resources.Resources;

public class SwingTestUtils {

	public static void run(Runnable r) {
		try {
			SwingUtilities.invokeAndWait(r);
		} catch (InvocationTargetException | InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	public static <T> T call(Callable<T> callable) {
		RunnableFuture<T> task = new FutureTask<>(callable);
		SwingUtilities.invokeLater(task);
		try {
			return task.get();
		} catch (InterruptedException | ExecutionException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static void setText(Component c, String resourceName, String text) {
		run(() -> {
			findTextField(c, Resources.getString(resourceName)).setText(text);
		});
	}
	
	public static void click(Component c, String resourceName) {
		String caption = Resources.getString(resourceName);
		run(() -> {
			AbstractButton button = findButton(c, caption);
			click(button);
		});
	}
	
	public static void click(Component target) {
		Point point = new Point(0, 0);
		SwingUtilities.convertPointToScreen(point, target);

		long time = System.currentTimeMillis();
		target.dispatchEvent(new MouseEvent(target, MouseEvent.MOUSE_PRESSED, time, 0, 0, 0, point.x, point.y, 1, false, MouseEvent.BUTTON1));
		target.dispatchEvent(new MouseEvent(target, MouseEvent.MOUSE_RELEASED, time, 0, 0, 0, point.x, point.y, 1, false, MouseEvent.BUTTON1));
		target.dispatchEvent(new MouseEvent(target, MouseEvent.MOUSE_CLICKED, time, 0, 0, 0, point.x, point.y, 1, false, MouseEvent.BUTTON1));
	}	
	
	private static AbstractButton findButton(Component c, String caption) {
		Component button = SwingTestUtils.getComponent(c, caption);
		Assert.assertNotNull("No found: " + caption, button);
		return (AbstractButton) button;
	}

	private static JTextField findTextField(Component c, String caption) {
		Component input = SwingTestUtils.getComponent(c, caption);
		Assert.assertNotNull("No found: " + caption, input);
		return (JTextField) input;
	}
	
	public static Component getComponent(Component c, String caption) {
		if (c instanceof JLabel) {
			JLabel label = (JLabel) c;
			if (StringUtils.equals(caption, label.getText())) {
				if (label.getParent() instanceof SwingCaption) {
					SwingCaption swingCaption = (SwingCaption) label.getParent();
					return swingCaption.getComponents()[1];
				} else {
					return label;
				}
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

	public static <T extends Component> T getComponent(Component c, Class<T> clazz) {
		return (T) getComponent(c, c2 -> clazz.isAssignableFrom(c2.getClass()));
	}

	public static Component getComponent(Component c, Predicate<Component> predicate) {
		if (predicate.test(c)) {
			return c;
		} else if (c instanceof Container) {
			Container container = (Container) c;
			for (Component child : container.getComponents()) {
				Component result = getComponent(child, predicate);
				if (result != null) {
					return result;
				}
			}
		}
		return null;
	}

}
