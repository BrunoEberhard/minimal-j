package org.minimalj.frontend.impl.swing.toolkit;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FocusTraversalPolicy;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.MouseInfo;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.io.File;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EventObject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.FocusManager;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.LookAndFeel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.text.JTextComponent;

import org.fife.ui.autocomplete.AutoCompletion;
import org.fife.ui.autocomplete.BasicCompletion;
import org.fife.ui.autocomplete.Completion;
import org.fife.ui.autocomplete.DefaultCompletionProvider;
import org.minimalj.application.Application;
import org.minimalj.application.Application.AuthenticatonMode;
import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.action.Action;
import org.minimalj.frontend.action.Action.ActionChangeListener;
import org.minimalj.frontend.action.ActionGroup;
import org.minimalj.frontend.form.element.ComboBoxFormElement;
import org.minimalj.frontend.impl.swing.FrameManager;
import org.minimalj.frontend.impl.swing.Swing;
import org.minimalj.frontend.impl.swing.SwingFrame;
import org.minimalj.frontend.impl.swing.SwingTab;
import org.minimalj.frontend.impl.swing.component.QueryLayout;
import org.minimalj.frontend.impl.swing.component.QueryLayout.QueryLayoutConstraint;
import org.minimalj.frontend.impl.swing.component.SwingHtmlContent;
import org.minimalj.frontend.page.Page;
import org.minimalj.frontend.page.Page.Dialog;
import org.minimalj.frontend.page.Page.WheelPage;
import org.minimalj.frontend.page.PageManager;
import org.minimalj.model.Rendering;
import org.minimalj.security.Subject;
import org.minimalj.util.resources.Resources;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.formdev.flatlaf.themes.FlatMacDarkLaf;
import com.formdev.flatlaf.util.SystemInfo;

public class SwingFrontend extends Frontend {

	private static final Icon ICON_FIELD_ACTION = new FlatSVGIcon(Swing.class.getPackage().getName().replace(".", "/") + "/fieldAction.svg");
	private static final Icon ICON_FIELD_MENU = new FlatSVGIcon(Swing.class.getPackage().getName().replace(".", "/") + "/fieldMenu.svg");

	private final Map<Dialog, SwingDialog> visibleDialogs = new HashMap<>();
	private double wheelRotation = 0;
	
	public SwingFrontend() {
		FlatLightLaf.setup();
		setUIManagerProperties();

		if (SystemInfo.isMacOS) {
			System.setProperty("apple.laf.useScreenMenuBar", "true");
			System.setProperty("apple.awt.application.name", Application.getInstance().getName());
			System.setProperty("apple.awt.application.appearance", "system");
		}

		if (SystemInfo.isLinux) {
			JFrame.setDefaultLookAndFeelDecorated(true);
			JDialog.setDefaultLookAndFeelDecorated(true);
		}

		UIManager.put("Group.Inset", 8);
		UIManager.put("Group.MarginLeftRight", 16);
		UIManager.put("Group.MarginTopBottom", 8);
		UIManager.put("Group.ArcSize", 12);

		UIManager.put("TableHeader.cellMargins", new Insets(2,1,2,1));
		
		if (Toolkit.getDefaultToolkit().areExtraMouseButtonsEnabled() && MouseInfo.getNumberOfButtons() > 3) {
		    Toolkit.getDefaultToolkit().addAWTEventListener(event -> {
		        if (event instanceof MouseEvent) {
		            MouseEvent mouseEvent = (MouseEvent) event;
		            if (mouseEvent.getID() == MouseEvent.MOUSE_RELEASED && mouseEvent.getButton() > 3) {
		            	Window window = SwingUtilities.getWindowAncestor(mouseEvent.getComponent());
		            	if (window instanceof SwingFrame) {
		            		SwingFrame frame = (SwingFrame) window;
			                if (mouseEvent.getButton() == 4) {
			                	frame.previous();
			                } else if (mouseEvent.getButton() == 5) {
			                	frame.next();
			                }
		            	}
		            }
		        }
		    }, AWTEvent.MOUSE_EVENT_MASK);
		}
	    Toolkit.getDefaultToolkit().addAWTEventListener(event -> {
	        if (event instanceof MouseWheelEvent) {
	        	MouseWheelEvent mouseWheelEvent = (MouseWheelEvent) event;
	        	if (mouseWheelEvent.getScrollType() == MouseWheelEvent.WHEEL_UNIT_SCROLL) {
		        	Window window = SwingUtilities.getWindowAncestor(mouseWheelEvent.getComponent());
	            	if (window instanceof SwingFrame) {
	            		SwingFrame frame = (SwingFrame) window;
	            		Page page = frame.getVisiblePage();
	            		if (page instanceof WheelPage) {
	            			wheelRotation += mouseWheelEvent.getPreciseWheelRotation();
	            			double wheel = wheelRotation > 0 ? Math.floor(wheelRotation) : Math.ceil(wheelRotation);
	            			if (wheel != 0) {
		            			wheelRotation -= wheel;
		            			frame.getVisibleTab().wheel((int) wheel);
	            			}
	            		}
	            	}
	        	}
	        }
	    }, AWTEvent.MOUSE_WHEEL_EVENT_MASK);

	}
	
	public static void setUIManagerProperties() {
		LookAndFeel lookAndFeel = UIManager.getLookAndFeel();
		if (lookAndFeel instanceof FlatDarkLaf || lookAndFeel instanceof FlatMacDarkLaf) {
			UIManager.put("Group.Background", new Color(70, 70, 70));
			UIManager.put("Group.BorderColor", new Color(100, 100, 100));
			
			UIManager.put("Table.background", new Color(20, 20, 20));
			UIManager.put("Table.gridColor", new Color(50, 50, 50));
			
			UIManager.put("Action.forground", new Color(100, 120, 255));
		} else {
			UIManager.put("Group.Background", new Color(250, 250, 250));
			UIManager.put("Group.BorderColor", new Color(225, 225, 225));
			
			UIManager.put("Table.background", new Color(250, 250, 250));
			UIManager.put("Table.gridColor", new Color(230, 230, 230));

			UIManager.put("Action.forground", Color.BLUE);
		}
	}
	
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

			setOpaque(false);
			setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					if (e.getButton() == MouseEvent.BUTTON1) {
						run(e, action);
					}
				}
			});
		}
		
		@Override
		public void paint(Graphics g) {
			setForeground(UIManager.getColor("Action.forground"));
			super.paint(g);
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
		SwingTextField textField = new SwingTextField(changeListener, maxLength, allowedCharacters);
		if (suggestionSearch != null) {
			DefaultCompletionProvider acp = new DefaultCompletionProvider() {
				@Override
				public boolean isAutoActivateOkay(JTextComponent tc) {
					return true;
				}				
				
				@Override
				protected List<Completion> getCompletionsImpl(JTextComponent comp) {
					List<Completion> retVal = new ArrayList<>();
					String text = getAlreadyEnteredText(comp);

					if (text != null) {
						List<String> suggestions = suggestionSearch.search(text);
						for (String s : suggestions) {
							retVal.add(new BasicCompletion(this, s));
						}
					}

					return retVal;
				}
			};
//			acp.addCompletion(new BasicCompletion(acp, "Test"));
//			acp.addCompletion(new BasicCompletion(acp, "Test2"));
//			acp.addCompletion(new BasicCompletion(acp, "Fasel"));
			AutoCompletion ac = new AutoCompletion(acp);
			ac.setAutoCompleteEnabled(true);
			ac.setAutoActivationDelay(100);
			ac.setAutoActivationEnabled(true);
			ac.setAutoCompleteSingleChoices(false);
			ac.install(textField);
		}
		return textField;
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
		return createComboBox(objects, ComboBoxFormElement.EMPTY_NULL_STRING, changeListener);
	}

	@Override
	public <T> Input<T> createRadioButtons(List<T> items, InputComponentListener changeListener) {
		return new SwingRadioButtons<>(items, changeListener);
	}
	
	@Override
	public Input<Boolean> createCheckBox(InputComponentListener changeListener, String text) {
		return new SwingCheckBox(changeListener, text);
	}
	
	@Override
	public <T> Input<T> createComboBox(List<T> items, String nullText, InputComponentListener changeListener) {
		return new SwingComboBox<T>(items, nullText, changeListener);
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
	public IContent createFilteredTable(FormContent filter, ITable<?> table, Action search, Action reset) {
		SwingBorderLayoutContent content = new SwingBorderLayoutContent();
		if (filter != null) {
			JPanel northPanel = new JPanel(new GridBagLayout());
			GridBagConstraints c = new GridBagConstraints();
			c.gridx = c.gridy = 0;
			c.anchor = GridBagConstraints.NORTHWEST;
			northPanel.add((Component) filter, c);
			if (search != null) {
				JButton buttonSearch = new JButton(SwingFrontend.adaptAction(search));
				c.gridx = 1;
				c.anchor = GridBagConstraints.SOUTH;
				northPanel.add(buttonSearch, c);
			}
			c.gridx = 2;
			c.weightx = 1;
			northPanel.add(new JPanel(), c);
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

//	@Override
//	public <T> IContent createTable(Search<T> search, Object[] keys, boolean multiSelect, TableActionListener<T> listener) {
//		return new SwingSearchPanel<>(search, keys, multiSelect, listener);
//	}

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
			Application.getInstance().search(query);
		}));

		return new QueryContent(Resources.getString("Application.queryCaption"), field);
	}

	@Override
	public Input<String> createLookup(Input<String> input, Runnable lookup) {
		if (input instanceof JTextField) {
			JButton button = new JButton(ICON_FIELD_ACTION);
			((JComponent) input).putClientProperty(FlatClientProperties.TEXT_FIELD_TRAILING_COMPONENT,
					button);
			button.addActionListener(event -> SwingFrontend.run(event, lookup));
			return input;
		} else if (input instanceof SwingTextAreaField) {
			((SwingTextAreaField) input).addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent event) {
					SwingFrontend.run(event, lookup);
				}
			});
			return input;
		} else {
			return new SwingLookup(input, event -> SwingFrontend.run(event, lookup::run));
		}
	}

	@Override
	public Input<String> createLookup(Input<String> input, ActionGroup actions) {
		JPopupMenu popupMenu = SwingTab.createMenu(actions.getItems());
		Component inputFinal = (Component) input;
		if (input instanceof JTextField) {
			JButton button = new JButton(ICON_FIELD_MENU);
			((JComponent) input).putClientProperty(FlatClientProperties.TEXT_FIELD_TRAILING_COMPONENT,
					button);
			button.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent event) {
					popupMenu.show(inputFinal, event.getX(), event.getY());
				};
			});
			return input;
		} else {
			((JComponent) input).setComponentPopupMenu(popupMenu);
			if (input instanceof SwingTextAreaField) {
				((SwingTextAreaField) input).addMouseListener(new MouseAdapter() {
					public void mouseClicked(MouseEvent event) {
						popupMenu.show(inputFinal, event.getX(), event.getY());
					}
				});				
				return input;
			} else {
				ActionListener actionListener = event -> {
					JComponent c = (JComponent) event.getSource();
					popupMenu.show(c, 0, c.getHeight());
				};
				return new SwingLookup(input, actionListener);
			}
		}
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

	private static class SwingLookup extends JPanel implements Input<String> {
		private static final long serialVersionUID = 1L;

		private final JButton lookupButton;
		private final Input<String> stringInput;

		public SwingLookup(Input<String> stringInput, ActionListener actionListener) {
			super(new BorderLayout());
			this.stringInput = stringInput;
			
			setBorder(UIManager.getBorder("TextField.border"));
			setBackground(UIManager.getColor("TextField.background"));
			Insets margin = UIManager.getInsets("TextField.margin");
			if (margin != null) {
				((JComponent) stringInput).setBorder(BorderFactory.createEmptyBorder(margin.top, margin.left, margin.bottom, margin.right));
			} else {
				((JComponent) stringInput).setBorder(null);
			}
			add((Component) stringInput, BorderLayout.CENTER);

			// TODO momentan der Button einfach quadratisch gemacht
			// es sollte wohl das Form Layout angepasst werden
			this.lookupButton = new JButton(ICON_FIELD_ACTION) {
				private static final long serialVersionUID = 1L;

				@Override
				public Dimension getPreferredSize() {
					Dimension d = super.getPreferredSize();
					d.height = d.width;
					return d;
				}
			};
			lookupButton.addActionListener(actionListener);
			lookupButton.setBorder(null);
			lookupButton.setBackground(UIManager.getColor("TextField.background"));
			JPanel buttonPanel = new JPanel(new BorderLayout());
			buttonPanel.add(lookupButton, BorderLayout.CENTER);
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

	public static javax.swing.Action[] adaptActions(List<org.minimalj.frontend.action.Action> actions) {
		javax.swing.Action[] swingActions = new javax.swing.Action[actions.size()];
		for (int i = 0; i < actions.size(); i++) {
			swingActions[i] = adaptAction(actions.get(i));
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
			if (filename.endsWith(".svg")) {
				return new FlatSVGIcon(Swing.class.getPackage().getName().replace(".", "/") + "/" + filename);
			} else {
				URL url = Swing.class.getResource(filename);
				if (url != null) {
					return new ImageIcon(url);
				}
			}
		}
		return null;
	}

	private static ThreadLocal<SwingTab> pageManager = new ThreadLocal<>();

	public static void runInBackground(Object source, Runnable r) {
		run(source, r, true);
	}

	public static void run(Object source, Runnable r) {
		run(source, r, false);
	}
	
	public static void run(Object source, Runnable r, boolean background) {
		if (!SwingUtilities.isEventDispatchThread()) {
			throw new IllegalStateException();
		}
		
		SwingFrame frame = findFrame(source);
		
		Runnable runnable = () -> {
			SwingTab tab;
			
			SwingTab savedTab = pageManager.get();
			Subject savedSubject = Subject.getCurrent();
			boolean savedEnabled;
			
			if (frame != null) {
				tab = frame.getVisibleTab();
				savedEnabled = tab.isEnabled();
				pageManager.set(tab);
				Subject.setCurrent(frame.getSubject());
				tab.setEnabled(false);
			} else {
				tab = null;
				savedEnabled = true;
			}
			try {
				r.run();
			} finally {
				pageManager.set(savedTab);
				Subject.setCurrent(savedSubject);
				if (tab != null) {
					tab.setEnabled(savedEnabled);
				}
			}
		};
		
		if (background) {
			new Thread(runnable).start();
		} else {
			runnable.run();
		}
	}

	private static SwingFrame findFrame(Object object) {
		if (object instanceof EventObject) {
			object = ((EventObject) object).getSource();
		}
		if (object instanceof ActionEvent) {
			object = ((ActionEvent) object).getSource();
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
		return pageManager.get() != null;
	}

	@Override
	public SwingTab getPageManager() {
		return pageManager.get();
	}
	
	public static void frameOpened(SwingFrame frame) {
		pageManager.set(frame.getVisibleTab());
	}
	
	@Override
	public void login(Subject subject) {
		SwingFrame frame;
		if (hasContext()) {
			frame = getPageManager().getFrame();
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
	public boolean showLogin(Dialog dialog) {
		SkipLoginAction skipLoginAction = new SkipLoginAction();
		List<Action> actions;
		if (Application.getInstance().getAuthenticatonMode() != AuthenticatonMode.REQUIRED && !hasContext()) {
			actions = Arrays.asList(skipLoginAction, dialog.getSaveAction());
		} else {
			actions = Collections.singletonList(dialog.getSaveAction());
		}
		
		SwingDialog swingDialog;
		if (!hasContext()) {
			// startup of application. no open jframe yet.
			SwingLoginAction loginAction = new SwingLoginAction(dialog.getSaveAction(), null);
			JComponent contentComponent = new SwingEditorPanel(dialog.getContent(), actions);
			swingDialog = new SwingDialog(null, dialog, contentComponent, loginAction, null);
		} else {
			// user clicked 'Login' menu
			JCheckBox checkBoxNewWindow = new JCheckBox(Resources.getString("OpenNewWindow"));
			JPanel panel = new JPanel(new BorderLayout());
			panel.add((Component) dialog.getContent(), BorderLayout.CENTER);
			panel.add(checkBoxNewWindow, BorderLayout.SOUTH);
			SwingLoginAction loginAction = new SwingLoginAction(dialog.getSaveAction(), checkBoxNewWindow);
			JComponent contentComponent = new SwingEditorPanel(panel, Collections.singletonList(loginAction));
			swingDialog = new SwingDialog(findFrame(getPageManager()), dialog, contentComponent, loginAction, null);
		}
		skipLoginAction.setDialog(swingDialog);
		return true;
	}
	
	private class SkipLoginAction extends Action {
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
			if (checkBoxNewWindow != null && checkBoxNewWindow.isSelected()) {
				SwingFrame frame = FrameManager.getInstance().openFrame();
				SwingUtilities.invokeLater(() -> {
					SwingFrontend.run(frame, () -> action.run());
				});
			} else {
				action.run();
			}
		}
	}
	
	@Override
	protected void open(Dialog dialog) {
		PageManager pageManager = getPageManager();
		if (pageManager != null) {
			pageManager.showDialog(dialog);
		} else {
			JComponent contentComponent = new SwingEditorPanel(dialog.getContent(), dialog.getActions());
			new SwingFrameDialog(dialog, contentComponent, dialog.getSaveAction(), dialog.getCancelAction());
		}
	}
	
	@Override
	protected void close(Dialog dialog) {
		for (Window window : Window.getWindows()) {
			if (window instanceof SwingDialog) {
				SwingDialog swingDialog = (SwingDialog) window;
				if (swingDialog.getDialog() == dialog) {
					swingDialog.closeDialog();
				}
			} else if (window instanceof SwingFrameDialog) {
				SwingFrameDialog swingDialog = (SwingFrameDialog) window;
				if (swingDialog.getDialog() == dialog) {
					swingDialog.closeDialog();
				}
			}
		}
		visibleDialogs.remove(dialog);
	}
}
