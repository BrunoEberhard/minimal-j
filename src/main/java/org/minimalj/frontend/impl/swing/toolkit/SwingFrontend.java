package org.minimalj.frontend.impl.swing.toolkit;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.EventQueue;
import java.awt.FocusTraversalPolicy;
import java.awt.SecondaryLoop;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.List;
import java.util.Stack;
import java.util.function.Function;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.FocusManager;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.text.JTextComponent;

import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.action.Action;
import org.minimalj.frontend.action.Action.ActionChangeListener;
import org.minimalj.frontend.impl.swing.SwingFrame;
import org.minimalj.frontend.impl.swing.SwingTab;
import org.minimalj.frontend.impl.swing.component.SwingHtmlContent;
import org.minimalj.frontend.page.IDialog;
import org.minimalj.model.Rendering;
import org.minimalj.model.Rendering.RenderType;

public class SwingFrontend extends Frontend {
	private static final Logger logger = Logger.getLogger(SwingFrontend.class.getName());
	
	@Override
	public IComponent createLabel(String string) {
		return new SwingText(string);
	}
	
	@Override
	public IComponent createLabel(Action action) {
		return new SwingActionLabel(action);
	}

	@Override
	public IComponent createText(Rendering rendering) {
		return new SwingText(rendering);
	}
	
	public static class SwingActionLabel extends JLabel implements IComponent {
		private static final long serialVersionUID = 1L;

		public SwingActionLabel(final Action action) {
			setText(action.getName());
//			label.setToolTipText(Resources.getResourceBundle().getString(runnable.getClass().getSimpleName() + ".description"));
			
			setForeground(Color.BLUE);
			setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					try {
						SwingFrontend.pushContext();
						action.action();
					} finally {
						SwingFrontend.popContext();
					}
				}
			});
		}
	}
	
	private static Stack<SwingTab> browserStack = new Stack<>();
	
	@Override
	public IComponent createTitle(String string) {
		return new SwingTitle(string);

	}

	@Override
	public Input<String> createReadOnlyTextField() {
		return new SwingReadOnlyTextField();
	}

	@Override
	public Input<String> createTextField(int maxLength, String allowedCharacters, InputType inputType, Search<String> suggestionSearch,
			InputComponentListener changeListener) {
		if (suggestionSearch == null) {
			return new SwingTextField(changeListener, maxLength, allowedCharacters);
		} else {
			return new SwingTextFieldAutocomplete(changeListener, suggestionSearch);
		}
	}

	@Override
	public PasswordField createPasswordField(InputComponentListener changeListener, int maxLength) {
		return new SwingPasswordField(changeListener, maxLength);
	}
	
	@Override
	public Input<String> createAreaField(int maxLength, String allowedCharacters, InputComponentListener changeListener) {
		return new SwingTextAreaField(changeListener, maxLength, null);
	}

	@Override
	public Input<byte[]> createImage(Size size, InputComponentListener changeListener) {
		return new SwingImage(size, changeListener);
	}
	
	@Override
	public IList createList(Action... actions) {
		return new SwingList(actions);
	}

	@Override
	public <T> Input<T> createComboBox(List<T> objects, InputComponentListener changeListener) {
		return new SwingComboBox<T>(objects, changeListener);
	}

	@Override
	public Input<Boolean> createCheckBox(InputComponentListener changeListener, String text) {
		return new SwingCheckBox(changeListener, text);
	}

	@Override
	public IComponent createComponentGroup(IComponent... components) {
		return new SwingHorizontalLayout(components);
	}

	@Override
	public SwitchContent createSwitchContent() {
		return new SwingSwitchContent();
	}

	@Override
	public FormContent createFormContent(int columns, int columnWidthPercentage) {
		return new SwingGridFormLayout(columns, columnWidthPercentage);
	}

	public static void focusFirstComponent(JComponent jComponent) {
		if (jComponent.isShowing()) {
			focusFirstComponentNow(jComponent);
		} else {
			focusFirstComponentLater(jComponent);
		}
	}

	private static void focusFirstComponentNow(JComponent component) {
		FocusTraversalPolicy focusPolicy = component.getFocusTraversalPolicy();
		if (component instanceof JTextComponent || component instanceof JComboBox || component instanceof JCheckBox) {
			component.requestFocus();
		} else if (focusPolicy != null && focusPolicy.getFirstComponent(component) != null) {
			focusPolicy.getFirstComponent(component).requestFocus();
		} else {
			FocusManager.getCurrentManager().focusNextComponent(component);
		}
	}

	private static void focusFirstComponentLater(final JComponent component) {
		component.addHierarchyListener(new HierarchyListener() {
			@Override
			public void hierarchyChanged(HierarchyEvent e) {
				component.removeHierarchyListener(this);
				focusFirstComponent(component);
			}
		});
	}

	@Override
	public <T> ITable<T> createTable(Object[] keys, TableActionListener<T> listener) {
		return new SwingTable<T>(keys, listener);
	}
	
	@Override
	public IContent createHtmlContent(String htmlOrUrl) {
		return new SwingHtmlContent(htmlOrUrl);
	}
	
	@Override
	public <T> Input<T> createLookup(InputComponentListener changeListener, Search<T> index, Object[] keys) {
		return new SwingLookup<T>(changeListener, index, keys);
	}
	
	public File showFileDialog(String title, String approveButtonText) {
		JFileChooser chooser = new JFileChooser();
		chooser.setMultiSelectionEnabled(false);
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		chooser.setDialogTitle(title);
		if (JFileChooser.APPROVE_OPTION == chooser.showDialog(getBrowser(), approveButtonText)) {
			return chooser.getSelectedFile();
		} else {
			return null;
		}
	}
	
	private static class SwingLookup<T> extends JPanel implements Input<T> {
		private static final long serialVersionUID = 1L;
		
		private final InputComponentListener changeListener;
		private final Search<T> search;
		private final Object[] keys;
		private final SwingLookupLabel actionLabel;
		private final SwingRemoveLabel removeLabel;
		private IDialog dialog;
		private T selectedObject;
		
		public SwingLookup(InputComponentListener changeListener, Search<T> search, Object[] keys) {
			super(new BorderLayout());
			
			this.changeListener = changeListener;
			this.search = search;
			this.keys = keys;
			
			this.actionLabel = new SwingLookupLabel();
			this.removeLabel = new SwingRemoveLabel();
			add(actionLabel, BorderLayout.CENTER);
			add(removeLabel, BorderLayout.LINE_END);
		}

		@Override
		public void setValue(T value) {
			this.selectedObject = value;
			display();
		}
		
		protected void display() {
			if (selectedObject instanceof Rendering) {
				Rendering rendering = (Rendering) selectedObject;
				actionLabel.setText(rendering.render(RenderType.PLAIN_TEXT));
			} else if (selectedObject != null) {
				actionLabel.setText(selectedObject.toString());
			} else {
				actionLabel.setText("[+]");
			}
		}

		@Override
		public T getValue() {
			return selectedObject;
		}
		
		@Override
		public void setEditable(boolean editable) {
			actionLabel.setEnabled(editable);
			removeLabel.setEnabled(editable);
		}
		
		private class SwingLookupLabel extends JLabel {
			private static final long serialVersionUID = 1L;

			public SwingLookupLabel() {
				setForeground(Color.BLUE);
				setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
				addMouseListener(new MouseAdapter() {
					@Override
					public void mouseClicked(MouseEvent e) {
						dialog = Frontend.showSearchDialog(search, keys, new LookupClickListener());
					}
				});
			}
		}
		
		private class SwingRemoveLabel extends JLabel {
			private static final long serialVersionUID = 1L;

			public SwingRemoveLabel() {
				super("[x]");
				setForeground(Color.BLUE);
				setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
				addMouseListener(new MouseAdapter() {
					@Override
					public void mouseClicked(MouseEvent e) {
						SwingLookup.this.selectedObject = null;
						changeListener.changed(SwingLookup.this);
					}
				});
			}
		}
		
		private class LookupClickListener implements TableActionListener<T> {
			@Override
			public void action(T selectedObject) {
				SwingLookup.this.selectedObject = selectedObject;
				dialog.closeDialog();
				changeListener.changed(SwingLookup.this);
			}
		}
	}

	public static boolean verticallyGrowing(Component component) {
		if (component instanceof SwingList || component instanceof JTable || component instanceof SwingTextAreaField || component instanceof SwingImage) {
			return true;
		}
		if (component instanceof Container) {
			Container container = (Container) component;
			for (Component c : container.getComponents()) {
				if (verticallyGrowing(c)) {
					return true;
				}
			}
		}
		return false;
	}
	
	public static javax.swing.Action[] adaptActions(Action[] actions) {
		javax.swing.Action[] swingActions = new javax.swing.Action[actions.length];
		for (int i = 0; i<actions.length; i++) {
			swingActions[i] = adaptAction(actions[i]);
		}
		return swingActions;
	}

	public static javax.swing.Action adaptAction(final Action action) {
		final javax.swing.Action swingAction = new AbstractAction(action.getName()) {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					pushContext();
					action.action();
				} finally {
					popContext();
				}
			}
		};
		swingAction.putValue(javax.swing.Action.SHORT_DESCRIPTION, action.getDescription());
		action.setChangeListener(new ActionChangeListener() {
			{
				update();
			}
			
			@Override
			public void change() {
				update();
			}

			protected void update() {
				swingAction.setEnabled(action.isEnabled());
				swingAction.putValue(javax.swing.Action.SHORT_DESCRIPTION, action.getDescription());
			}
		});
		return swingAction;
	}
	
	public static void pushContext() {
		browserStack.push(SwingFrame.getActiveWindow().getVisibleTab());
	}
	
	public static void popContext() {
		browserStack.pop();
	}
	
	public static boolean hasContext() {
		return !browserStack.isEmpty() && browserStack.peek() != null;
	}
	
	@Override
	public SwingTab getBrowser() {
		if (hasContext()) {
			return browserStack.peek();
		} else if (SwingFrame.getActiveWindow() != null) {
			return SwingFrame.getActiveWindow().getVisibleTab();
		} else {
			return null;
		}
	}

	@Override
	public <INPUT, RESULT> RESULT executeSync(Function<INPUT, RESULT> function, INPUT input) {
		if (!hasContext()) {
			return function.apply(input);
		}
		
		Toolkit tk = Toolkit.getDefaultToolkit();
		EventQueue eq = tk.getSystemEventQueue();
		SecondaryLoop loop = eq.createSecondaryLoop();

		ExecuteSyncThread<INPUT, RESULT> thread = new ExecuteSyncThread<>(loop, function, input);
		SwingTab pageBrowser = getBrowser();
		try {
			browserStack.push(null);
			pageBrowser.lock();
			thread.start();
			if (!loop.enter()) {
				logger.warning("Could not execute background in second thread");
				return function.apply(input);
			}
			if (thread.getException() != null) {
				throw new RuntimeException(thread.getException());
			}
			return thread.getResult();
		} finally {
			browserStack.pop();
			pageBrowser.unlock();
		}
	}

	private static class ExecuteSyncThread<INPUT, RESULT> extends Thread {
		private final SecondaryLoop loop;
		private final Function<INPUT, RESULT> function;
		private final INPUT input;
		private RESULT result;
		private Exception exception;

		public ExecuteSyncThread(SecondaryLoop loop, Function<INPUT, RESULT> function, INPUT input) {
			this.loop = loop;
			this.function = function;
			this.input = input;
		}

		@Override
		public void run() {
			try {
				result = function.apply(input);
			} catch (Exception x) {
				exception = x;
			} finally {
				loop.exit();
			}
		}

		public RESULT getResult() {
			return result;
		}

		public Exception getException() {
			return exception;
		}
	}
	
}
