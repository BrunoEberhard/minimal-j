package org.minimalj.frontend.vaadin.toolkit;

import java.text.NumberFormat;
import java.util.Locale;

import javax.swing.UIManager;

import org.minimalj.frontend.toolkit.ClientToolkit.ConfirmDialogType;
import org.minimalj.frontend.toolkit.ClientToolkit.DialogListener;
import org.minimalj.frontend.toolkit.ClientToolkit.DialogListener.ConfirmDialogResult;

import com.vaadin.event.Action;
import com.vaadin.event.ShortcutAction;
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.terminal.gwt.server.WebApplicationContext;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.NativeButton;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.Reindeer;

/**
 * Based on https://vaadin.com/directory#addon/confirmdialog
 *
 */
public class VaadinConfirmDialog extends Window {
    private static final long serialVersionUID = 1L;

	private static final double MIN_WIDTH = 20d;
    private static final double MAX_WIDTH = 40d;
    private static final double MIN_HEIGHT = 1d;
    private static final double MAX_HEIGHT = 30d;
    private static final double BUTTON_HEIGHT = 2.5;

	private final DialogListener listener;
	private Button buttonYes, buttonNo, buttonOk, buttonCancel;
	private int focusIndex;
	
	public VaadinConfirmDialog(Window window, String message, String title, ConfirmDialogType type, DialogListener listener) {
		this.listener = listener;
		
   	    WebApplicationContext context = (WebApplicationContext) window.getApplication().getContext();
        Locale locale = Locale.getDefault(); // TODO: context.getBrowser().getLocale(); does not compile with jdk, but with eclipse
		
		setCaption(title);
		
        addListener(new Window.CloseListener() {
            private static final long serialVersionUID = 1L;

			@Override
			public void windowClose(CloseEvent ce) {
                if (isEnabled()) {
                    setEnabled(false); // avoid double processing
                    VaadinConfirmDialog.this.listener.close(ConfirmDialogResult.CANCEL);
                }
            }
        });

        // Create content
        VerticalLayout c = (VerticalLayout) getContent();
        c.setSizeFull();
        c.setSpacing(true);

        // Panel for scrolling lengthty messages.
        Panel scroll = new Panel(new VerticalLayout());
        scroll.setScrollable(true);
        c.addComponent(scroll);
        scroll.setWidth("100%");
        scroll.setHeight("100%");
        scroll.setStyleName(Reindeer.PANEL_LIGHT);
        c.setExpandRatio(scroll, 1f);

        // Always HTML, but escape
        Label text = new Label("", Label.CONTENT_RAW);
        scroll.addComponent(text);
        text.setValue(message);
        
        final HorizontalLayout buttons = new HorizontalLayout();
        c.addComponent(buttons);
        buttons.setSpacing(true);

        buttons.setHeight(format(BUTTON_HEIGHT) + "em");
        buttons.setWidth("100%");
        Label spacer = new Label("");
        buttons.addComponent(spacer);
        spacer.setWidth("100%");
        buttons.setExpandRatio(spacer, 1f);

        if (type == ConfirmDialogType.YES_NO_CANCEL || type == ConfirmDialogType.YES_NO) {
        	buttonYes = new NativeButton(UIManager.getString("OptionPane.yesButtonText", locale));
        	buttonYes.setData(true);
        	buttonYes.setClickShortcut(KeyCode.ENTER, null);
            buttons.addComponent(buttonYes);
            buttons.setComponentAlignment(buttonYes, Alignment.MIDDLE_RIGHT);
            buttonYes.addListener(new ConfirmDialogButtonListener(ConfirmDialogResult.YES));
            
            buttonNo = new NativeButton(UIManager.getString("OptionPane.noButtonText", locale));
        	buttonNo.setData(true);
        	buttonNo.setClickShortcut(KeyCode.ESCAPE, null);
            buttons.addComponent(buttonNo);
            buttons.setComponentAlignment(buttonNo, Alignment.MIDDLE_RIGHT);
            buttonNo.addListener(new ConfirmDialogButtonListener(ConfirmDialogResult.NO));
        }
        
        /* not used at the moment 
        if (type == ConfirmDialogType.OK_CANCEL) {
            buttonOk = new NativeButton(UIManager.getString("OptionPane.okButtonText", locale));
            buttonOk.setData(true);
            buttonOk.setClickShortcut(KeyCode.ENTER, null);
            buttons.addComponent(buttonOk);
            buttons.setComponentAlignment(buttonOk, Alignment.MIDDLE_RIGHT);
            buttonOk.addListener(new ConfirmDialogButtonListener(JOptionPane.OK));
        }
        */
        
        if (type == ConfirmDialogType.YES_NO_CANCEL /* || type == ConfirmDialogType.OK_CANCEL */) {
            buttonCancel = new NativeButton(UIManager.getString("OptionPane.cancelButtonText", locale));
            buttonCancel.setData(false);
            buttonCancel.setClickShortcut(KeyCode.ESCAPE, null);
            buttons.addComponent(buttonCancel);
            buttons.setComponentAlignment(buttonCancel, Alignment.MIDDLE_RIGHT);
            buttonCancel.addListener(new ConfirmDialogButtonListener(ConfirmDialogResult.CANCEL));
        }
        
        // Keyboard support
        final int buttonCount = buttons.getComponentCount() - 1;
        Action.Handler ah = new Action.Handler() {
			private static final long serialVersionUID = 1L;
			private final ShortcutAction LEFT = new ShortcutAction("left", ShortcutAction.KeyCode.ARROW_LEFT, null);
			private final ShortcutAction RIGHT = new ShortcutAction("right", ShortcutAction.KeyCode.ARROW_RIGHT, null);
			private final ShortcutAction NEXT = new ShortcutAction("next", ShortcutAction.KeyCode.TAB, null);
			private final ShortcutAction PREV = new ShortcutAction("prev", ShortcutAction.KeyCode.TAB,
					new int[] { ShortcutAction.ModifierKey.SHIFT });

			private final ShortcutAction[] acs = new ShortcutAction[] { LEFT, RIGHT, NEXT, PREV };

            @Override
			public void handleAction(Action action, Object sender, Object target) {
            	if (action == RIGHT || action == NEXT) {
            		focusIndex++;
            	} else {
            		focusIndex--;
            		if (focusIndex < 0) {
            			focusIndex = buttonCount - 1;
            		}
            	}
            	focusIndex = focusIndex % buttonCount;
            	((Button) buttons.getComponent(focusIndex + 1)).focus();
            }

            @Override
			public Action[] getActions(Object target, Object sender) {
                return acs;
            }
        };
        addActionHandler(ah);
        
        
        // Approximate the size of the dialog
        double[] dim = getDialogDimensions(message);
        setWidth(format(dim[0]) + "em");
        setHeight(format(dim[1]) + "em");
        setResizable(false);
        
        setModal(true);
        center();
        window.addWindow(this);
        
        focusIndex = buttonCount - 1;
        ((Button) buttons.getComponent(focusIndex + 1)).focus();
	}

	private class ConfirmDialogButtonListener implements ClickListener {
		private static final long serialVersionUID = 1L;
		private ConfirmDialogResult result;

		public ConfirmDialogButtonListener(ConfirmDialogResult result) {
			this.result = result;
		}
		
		@Override
		public void buttonClick(ClickEvent event) {
			VaadinClientToolkit.setWindow(event.getComponent().getWindow());
			setVisible(false);
			VaadinConfirmDialog.this.detach(); // mysterious
			listener.close(result);
			VaadinClientToolkit.setWindow(null);
		}
	}
	
    /**
     * Approximates the dialog dimensions based on its message length.
     *
     * @param message
     *            Message string
     * @return
     */
	private double[] getDialogDimensions(String message) {
		// Based on Reindeer style:
		double chrW = 0.5d;
		double chrH = 1.5d;
		double length = chrW * message.length();
		double rows = Math.ceil(length / MAX_WIDTH);

		// Estimate extra lines
		rows += count("\n", message);

		// Obey maximum size
		double width = Math.min(MAX_WIDTH, length);
		double height = Math.ceil(Math.min(MAX_HEIGHT, rows * chrH));

		// Obey the minimum size
		width = Math.max(width, MIN_WIDTH);
		height = Math.max(height, MIN_HEIGHT);

		// Based on Reindeer style:
		double btnHeight = 2.5d;
		double vmargin = 8d;
		double hmargin = 2d;

		double[] res = new double[] { width + hmargin, height + btnHeight + vmargin };
		return res;
	}

	/**
	 * Count the number of needles within a haystack.
	 * 
	 * @param needle
	 *            The string to search for.
	 * @param haystack
	 *            The string to process.
	 * @return
	 */
	private static int count(final String needle, final String haystack) {
		int count = 0;
		int pos = -1;
		while ((pos = haystack.indexOf(needle, pos + 1)) >= 0) {
			count++;
		}
		return count;
	}
    
    /**
     * Format a double single fraction digit.
     *
     * @param n
     * @return
     */
    private String format(double n) {
        NumberFormat nf = NumberFormat.getNumberInstance(Locale.ENGLISH);
        nf.setMaximumFractionDigits(1);
        nf.setGroupingUsed(false);
        return nf.format(n);
    }
    
}
