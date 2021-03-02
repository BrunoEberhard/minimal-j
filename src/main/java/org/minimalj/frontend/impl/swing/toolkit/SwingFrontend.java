package org.minimalj.frontend.impl.swing.toolkit;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FocusTraversalPolicy;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.util.EventObject;
import java.util.List;
import java.util.Optional;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.FocusManager;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.text.JTextComponent;

import org.minimalj.application.Application;
import org.minimalj.application.Application.AuthenticatonMode;
import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.action.Action;
import org.minimalj.frontend.action.Action.ActionChangeListener;
import org.minimalj.frontend.action.ActionGroup;
import org.minimalj.frontend.impl.swing.FrameManager;
import org.minimalj.frontend.impl.swing.Swing;
import org.minimalj.frontend.impl.swing.SwingFrame;
import org.minimalj.frontend.impl.swing.SwingTab;
import org.minimalj.frontend.impl.swing.component.QueryLayout;
import org.minimalj.frontend.impl.swing.component.QueryLayout.QueryLayoutConstraint;
import org.minimalj.frontend.impl.swing.component.SwingHtmlContent;
import org.minimalj.frontend.page.IDialog;
import org.minimalj.frontend.page.Page;
import org.minimalj.frontend.page.PageManager;
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
			setToolTipText(action.getDescription());

			setForeground(Color.BLUE);
			setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					run(e, action);
				}
			});
		}
	}

//	public static final Stack<SwingTab> browserStack = new Stack<>();

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
	public Input<byte[]> createImage(InputComponentListener changeListener) {
		return new SwingImage(changeListener);
	}

	@Override
	public <T> Input<T> createComboBox(List<T> objects, InputComponentListener changeListener) {
		return new SwingComboBox<>(objects, changeListener);
	}

	@Override
	public Input<Boolean> createCheckBox(InputComponentListener changeListener, String text) {
		return new SwingCheckBox(changeListener, text);
	}

	@Override
	public IComponent createHorizontalGroup(IComponent... components) {
		return new SwingHorizontalLayout(components);
	}

	@Override
	public IComponent createVerticalGroup(IComponent... components) {
		return new SwingVerticalGroup(components);
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

	private static class SwingBorderLayoutContent extends JPanel implements IContent {
		private static final long serialVersionUID = 1L;

		private SwingBorderLayoutContent() {
			super(new BorderLayout());
		}
	}

	@Override
	public IContent createFormTableContent(FormContent form, ITable<?> table) {
		SwingBorderLayoutContent content = new SwingBorderLayoutContent();
		if (form != null) {
			JPanel northPanel = new JPanel(new BorderLayout());
			northPanel.add((Component) form, BorderLayout.LINE_START);
			content.add(northPanel, BorderLayout.NORTH);
		}
		if (table != null) {
			content.add((Component) table, BorderLayout.CENTER);
		}
		return content;
	}

	@Override
	public <T> ITable<T> createTable(Object[] keys, boolean multiSelect, TableActionListener<T> listener) {
		return new SwingTable<>(keys, multiSelect, listener);
	}

	@Override
	public <T> IContent createTable(Search<T> search, Object[] keys, boolean multiSelect, TableActionListener<T> listener) {
		return new SwingSearchPanel<>(search, keys, multiSelect, listener);
	}

	public static final String FX_HTML_CLASS = "org.minimalj.frontend.impl.swing.component.FxHtmlContent";

	@Override
	public IContent createHtmlContent(String htmlOrUrl) {
		try {
			Class<?> c = Class.forName(FX_HTML_CLASS);
			@SuppressWarnings("unchecked")
			Constructor<IContent> con = (Constructor<IContent>) c.getConstructor(String.class);
			return con.newInstance(htmlOrUrl);
		} catch (ClassNotFoundException x) {
			// swingfxbrowser not available
			return new SwingHtmlContent(htmlOrUrl);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public IContent createHtmlContent(URL url) {
		try {
			Class<?> c = Class.forName(FX_HTML_CLASS);
			@SuppressWarnings("unchecked")
			Constructor<IContent> con = (Constructor<IContent>) c.getConstructor(URL.class);
			return con.newInstance(url);
		} catch (ClassNotFoundException x) {
			// swingfxbrowser not available
			return new SwingHtmlContent(url);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
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
		field.addActionListener(e -> SwingFrontend.run(e, () -> {
			String query = field.getText();
			Page page = Application.getInstance().createSearchPage(query);
			show(page);
		}));

		return new QueryContent(Resources.getString("Application.queryCaption"), field);
	}

	@Override
	public Input<String> createLookup(Input<String> stringInput, Runnable lookup) {
		return new SwingLookup(stringInput, event -> SwingFrontend.run(event, lookup::run));
	}

	@Override
	public Input<String> createLookup(Input<String> input, ActionGroup actions) {
		((JComponent) input).setComponentPopupMenu(SwingTab.createMenu(actions.getItems()));
		ActionListener actionListener = event -> {
			JPopupMenu popupMenu = ((JComponent) input).getComponentPopupMenu();
			JComponent c = (JComponent) event.getSource();
			popupMenu.show(c, 0, c.getHeight());
		};
		return new SwingLookup(input, actionListener);
	}

	public File showFileDialog(String title, String approveButtonText) {
		JFileChooser chooser = new JFileChooser();
		chooser.setMultiSelectionEnabled(false);
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		chooser.setDialogTitle(title);
		if (JFileChooser.APPROVE_OPTION == chooser.showDialog((Component) getPageManager(), approveButtonText)) {
			return chooser.getSelectedFile();
		} else {
			return null;
		}
	}

	private static class SwingLookup extends JPanel implements Input<String> {
		private static final long serialVersionUID = 1L;

		private final JButton lookupButton;
		private final Input<String> stringInput;

		public SwingLookup(Input<String> stringInput, ActionListener actionListener) {
			super(new BorderLayout());
			this.stringInput = stringInput;

			add((Component) stringInput, BorderLayout.CENTER);

			// TODO momentan der Button einfach quadratisch gemacht
			// es sollte wohl das Form Layout angepasst werden
			this.lookupButton = new JButton(" ... ") {
				private static final long serialVersionUID = 1L;

				@Override
				public Dimension getPreferredSize() {
					Dimension d = super.getPreferredSize();
					d.height = d.width;
					return d;
				}
			};
			lookupButton.setContentAreaFilled(false);
			lookupButton.addActionListener(actionListener);
			lookupButton.setBorder(BorderFactory.createLineBorder(UIManager.getColor("TextField.shadow"), 1));
			JPanel buttonPanel = new JPanel(new BorderLayout());
			buttonPanel.add(lookupButton, BorderLayout.PAGE_START);
			add(buttonPanel, BorderLayout.AFTER_LINE_ENDS);
		}

		@Override
		public void setValue(String value) {
			stringInput.setValue(value);
		}

		@Override
		public String getValue() {
			return stringInput.getValue();
		}

		@Override
		public void setEditable(boolean editable) {
			stringInput.setEditable(editable);
			lookupButton.setVisible(editable);
		}
	}

	public static javax.swing.Action[] adaptActions(Action[] actions) {
		javax.swing.Action[] swingActions = new javax.swing.Action[actions.length];
		for (int i = 0; i < actions.length; i++) {
			swingActions[i] = adaptAction(actions[i]);
		}
		return swingActions;
	}

	public static javax.swing.Action adaptAction(final Action action) {
		final javax.swing.Action swingAction = new AbstractAction(action.getName()) {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				run(e, action);
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

	private static SwingTab pageManager;

	// TODO move to SwingFrame
	public static void run(Object source, Runnable r) {
		if (!hasContext()) {
			try {
				SwingFrame frame = findFrame(source);
				if (frame != null) {
					pageManager = frame.getVisibleTab();
					Subject.setCurrent(frame.getSubject());
				}
				r.run();
			} finally {
				pageManager = null;
				Subject.setCurrent(null);
			}
		} else {
			r.run();
		}
	}

	private static SwingFrame findFrame(Object object) {
		if (object instanceof EventObject) {
			object = ((EventObject) object).getSource();
		}
		while (!(object instanceof SwingFrame) && object != null) {
			if (object instanceof JPopupMenu) {
				object = ((JPopupMenu) object).getInvoker();
			} else if (object instanceof Component) {
				object = ((Component) object).getParent();
			} else {
				throw new IllegalStateException("Not allowed: " + object);
			}
		}
		return (SwingFrame) object;
	}

	public static boolean hasContext() {
		return pageManager != null;
	}

	
	@Override
	public PageManager getPageManager() {
		return pageManager;
	}
	
	@Override
	public void login(Subject subject) {
		SwingFrame frame;
		if (hasContext()) {
			frame = pageManager.getFrame();
		} else {
			frame = FrameManager.getInstance().openFrame();
		}
		frame.setSubject(subject);
	}
	
	@Override
	protected void doShowError(String text) {
		if (hasContext()) {
			super.doShowError(text);
		} else {
			JOptionPane.showMessageDialog(null, text, "Fehler", JOptionPane.ERROR_MESSAGE);
		}
	}
	
	@Override
	protected void doShowMessage(String text) {
		if (hasContext()) {
			super.doShowMessage(text);
		} else {
			JOptionPane.showMessageDialog(null, text, "Information", JOptionPane.INFORMATION_MESSAGE);
		}
	}
	
	@Override
	public Optional<IDialog> showLogin(IContent content, Action loginAction, Action... additionalActions) {
		NoLoginAction noLoginAction = new NoLoginAction();
		Action[] actions;
		if (Application.getInstance().getAuthenticatonMode() != AuthenticatonMode.REQUIRED && !hasContext()) {
			actions = new org.minimalj.frontend.action.Action[] {noLoginAction, loginAction};
		} else {
			actions = new org.minimalj.frontend.action.Action[] {loginAction};
		}
		
		SwingDialog dialog;
		if (!hasContext()) {
			// startup of application. no open jframe yet.
			loginAction = new SwingLoginAction(loginAction, null);
			JComponent contentComponent = new SwingEditorPanel(content, actions);
			dialog = new SwingDialog(null, Resources.getString("Login.title"), contentComponent, loginAction, null);
		} else {
			// user clicked 'Login' menu
			JCheckBox checkBoxNewWindow = new JCheckBox(Resources.getString("OpenNewWindow"));
			JPanel panel = new JPanel(new BorderLayout());
			panel.add((Component) content, BorderLayout.CENTER);
			panel.add(checkBoxNewWindow, BorderLayout.SOUTH);
			loginAction = new SwingLoginAction(loginAction, checkBoxNewWindow);
			JComponent contentComponent = new SwingEditorPanel(panel, actions);
			dialog = new SwingDialog(findFrame(pageManager), Resources.getString("Login.title"), contentComponent, loginAction, null);
		}
		noLoginAction.setDialog(dialog);
		return Optional.of(dialog);
	}
	
	private class NoLoginAction extends Action {
		private SwingDialog dialog;

		public void setDialog(SwingDialog dialog) {
			this.dialog = dialog;
		}
		
		@Override
		public void run() {
			Subject.setCurrent(null);
			Frontend.getInstance().login(null);
			dialog.closeDialog();
		}
	}
	
	public static class SwingLoginAction extends org.minimalj.frontend.action.Action {

		private final org.minimalj.frontend.action.Action action;
		private final JCheckBox checkBoxNewWindow;
		
		public SwingLoginAction(org.minimalj.frontend.action.Action action, JCheckBox checkBoxNewWindow) {
			super(action.getName());
			this.action = action;
			this.checkBoxNewWindow = checkBoxNewWindow;
		}
		
		@Override
		public String getDescription() {
			return action.getDescription();
		}
		
		@Override
		public void run() {
			action.run();
		}
	}
}
