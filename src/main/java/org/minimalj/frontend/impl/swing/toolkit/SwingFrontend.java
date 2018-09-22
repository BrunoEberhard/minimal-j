package org.minimalj.frontend.impl.swing.toolkit;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.FocusTraversalPolicy;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.Stack;

import javax.swing.AbstractAction;
import javax.swing.FocusManager;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.text.JTextComponent;

import org.minimalj.application.Application;
import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.action.Action;
import org.minimalj.frontend.action.Action.ActionChangeListener;
import org.minimalj.frontend.impl.swing.Swing;
import org.minimalj.frontend.impl.swing.SwingFrame;
import org.minimalj.frontend.impl.swing.SwingTab;
import org.minimalj.frontend.impl.swing.component.QueryLayout;
import org.minimalj.frontend.impl.swing.component.QueryLayout.QueryLayoutConstraint;
import org.minimalj.frontend.impl.swing.component.SwingHtmlContent;
import org.minimalj.frontend.page.Page;
import org.minimalj.model.Rendering;
import org.minimalj.security.Subject;
import org.minimalj.util.resources.Resources;

public class SwingFrontend extends Frontend {
	@Override
	public IComponent createText(String string) {
		return new SwingText(string);
	}
	
	@Override
	public IComponent createText(Action action) {
		return new SwingActionText(action);
	}

	@Override
	public IComponent createText(Rendering rendering) {
		return new SwingText(rendering);
	}
	
	public static class SwingActionText extends JLabel implements IComponent {
		private static final long serialVersionUID = 1L;

		public SwingActionText(final Action action) {
			setText(action.getName());
//			label.setToolTipText(Resources.getResourceBundle().getString(runnable.getClass().getSimpleName() + ".description"));
			
			setForeground(Color.BLUE);
			setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					runWithContext(() -> action.action());
				}
			});
		}
	}
	
	public static final Stack<SwingTab> browserStack = new Stack<>();
	
	@Override
	public IComponent createTitle(String string) {
		return new SwingTitle(string);

	}

	@Override
	public Input<String> createReadOnlyTextField() {
		return new SwingReadOnlyTextField();
	}

	@Override
	public Input<String> createTextField(int maxLength, String allowedCharacters, Search<String> suggestionSearch, InputComponentListener changeListener) {
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
	public Input<byte[]> createImage(int size, InputComponentListener changeListener) {
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
		return new SwingSwitch();
	}
	
	@Override
	public SwitchComponent createSwitchComponent() {
		return new SwingSwitch();
	}

	@Override
	public FormContent createFormContent(int columns, int columnWidthPercentage) {
		return new SwingFormContent(columns, columnWidthPercentage);
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
	public <T> ITable<T> createTable(Object[] keys, boolean multiSelect, TableActionListener<T> listener) {
		return new SwingTable<T>(keys, multiSelect, listener);
	}
	
	@Override
	public IContent createHtmlContent(String htmlOrUrl) {
		return new SwingHtmlContent(htmlOrUrl);
	}
	
	private static class QueryContent extends JPanel implements IContent {
		private static final long serialVersionUID = 1L;

		public QueryContent(String caption, JComponent component) {
			super(new QueryLayout());
			add(component, QueryLayoutConstraint.TEXTFIELD);
			JLabel label = new JLabel(caption);
			add(label, QueryLayoutConstraint.CAPTION, 1);
		}
	}
	
	@Override
	public IContent createQueryContent() {
		JTextField field = new JTextField();
		field.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				SwingFrontend.runWithContext(() -> {
					String query = ((JTextField) field).getText();
					Page page = Application.getInstance().createSearchPage(query);
					show(page);
				});
			}
		});

		return new QueryContent(Resources.getString("Application.queryCaption"), (JTextField) field);
	}
	
	@Override
	public Input<String> createLookup(Runnable lookup) {
		return new SwingLabelLookup(lookup);
	}

	@Override
	public Input<String> createLookup(Runnable lookup, InputComponentListener changeListener) {
		return new SwingLookup(lookup, changeListener);
	}
	
	public File showFileDialog(String title, String approveButtonText) {
		JFileChooser chooser = new JFileChooser();
		chooser.setMultiSelectionEnabled(false);
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		chooser.setDialogTitle(title);
		if (JFileChooser.APPROVE_OPTION == chooser.showDialog(getPageManager(), approveButtonText)) {
			return chooser.getSelectedFile();
		} else {
			return null;
		}
	}
	
	private static final Insets EMPTY_INSETS = new Insets(0, 0, 0, 0);

	private static class SwingLookup extends JPanel implements Input<String> {
		private static final long serialVersionUID = 1L;
		
		private final SwingTextField textField;
		private final JButton lookupButton;
		
		public SwingLookup(Runnable runnable, InputComponentListener changeListener) {
			super(new BorderLayout());
			
			this.textField = new SwingTextField(changeListener, Integer.MAX_VALUE);
			add(textField, BorderLayout.CENTER);

			this.lookupButton = new JButton("...");
			lookupButton.setMargin(EMPTY_INSETS);
			lookupButton.addActionListener(event -> runnable.run());
			add(lookupButton, BorderLayout.AFTER_LINE_ENDS);
		}

		@Override
		public void setValue(String value) {
			textField.setText(value);
		}

		@Override
		public String getValue() {
			return textField.getText();
		}
		
		@Override
		public void setEditable(boolean editable) {
			textField.setEditable(editable);
			lookupButton.setVisible(editable);
		}
	}

	private static class SwingLabelLookup extends JPanel implements Input<String> {
		private static final long serialVersionUID = 1L;
		
		private final JLabel label;
		private final JButton lookupButton;

		public SwingLabelLookup(Runnable runnable) {
			super(new BorderLayout());

			this.label = new JLabel();
			add(label, BorderLayout.CENTER);

			this.lookupButton = new JButton("...");
			lookupButton.setMargin(EMPTY_INSETS);
			lookupButton.addActionListener(event -> runnable.run());
			add(lookupButton, BorderLayout.AFTER_LINE_ENDS);
		}

		@Override
		public void setValue(String value) {
			label.setText(value);
		}

		@Override
		public String getValue() {
			return label.getText();
		}

		@Override
		public void setEditable(boolean editable) {
			lookupButton.setVisible(editable);
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
				runWithContext(() -> action.action());
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
	
	public static Icon getIcon(String resourceName) {
		if (Resources.isAvailable(resourceName)) {
			String filename = Resources.getString(resourceName);
			URL url = Swing.class.getResource(filename);
			if (url != null) {
				return new ImageIcon(url);
			}
		}
		return null;
	}
	
	/**
	 * As the current subject can be different for each Swing Window
	 * this method ensure that no wrong subject stays as current subject
	 * 
	 * @param r a runnable (normally a lambda is used)
	 */
	public static void runWithContext(Runnable r) {
		try {
			browserStack.push(SwingFrame.getActiveWindow().getVisibleTab());
			Subject.setCurrent(SwingFrame.getActiveWindow().getSubject());
			r.run();
		} finally {
			Subject.setCurrent(null);
			browserStack.pop();
		}
	}
	
	public static boolean hasContext() {
		return !browserStack.isEmpty() && browserStack.peek() != null;
	}
	
	@Override
	public SwingTab getPageManager() {
		if (hasContext()) {
			return browserStack.peek();
		} else if (SwingFrame.getActiveWindow() != null) {
			return SwingFrame.getActiveWindow().getVisibleTab();
		} else {
			return null;
		}
	}
	
	public static boolean applicationHasRouting() {
		try {
			return Application.getInstance().getClass().getMethod("createPage", new Class<?>[] { String.class }).getDeclaringClass() != Application.class;
		} catch (NoSuchMethodException | SecurityException e) {
			throw new RuntimeException(e);
		}
	}
	
}
