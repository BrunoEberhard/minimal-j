package org.minimalj.frontend.impl.vaadin.toolkit;

import java.text.NumberFormat;
import java.util.Locale;

import org.minimalj.frontend.impl.vaadin.toolkit.VaadinConfirmDialog.Factory;

import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.v7.ui.HorizontalLayout;
import com.vaadin.v7.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.UI;
import com.vaadin.v7.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.CloseEvent;
import com.vaadin.v7.ui.themes.Reindeer;

/**
 * This is the default implementation for confirmation dialog factory.
 *
 * This supports text only content and tries to approximate the the dialog size.
 *
 * TODO: Allow configuration of min and max sizes.
 *
 * @author Sami Ekblad
 *
 */
public class DefaultConfirmDialogFactory implements Factory {

    /** Generated serial UID. */
    private static final long serialVersionUID = -5412321247707480466L;

    // System wide defaults
    protected static final String DEFAULT_CAPTION = "Confirm";
    protected static final String DEFAULT_MESSAGE = "Are You sure?";
    protected static final String DEFAULT_OK_CAPTION = "Ok";
    protected static final String DEFAULT_CANCEL_CAPTION = "Cancel";

    // System wide defaults
    private static final double MIN_WIDTH = 20d;
    private static final double MAX_WIDTH = 40d;
    private static final double MIN_HEIGHT = 1d;
    private static final double MAX_HEIGHT = 30d;
    private static final double BUTTON_HEIGHT = 2.5;

    public VaadinConfirmDialog create(final String caption, final String message,
            final String okCaption, final String cancelCaption,
            final String notOkCaption) {

        final boolean threeWay = notOkCaption != null;
        // Create a confirm dialog
        final VaadinConfirmDialog confirm = new VaadinConfirmDialog();
        confirm.setId(VaadinConfirmDialog.DIALOG_ID);
        confirm.setCaption(caption != null ? caption : DEFAULT_CAPTION);

        // Close listener implementation
        confirm.addCloseListener(new Window.CloseListener() {

            private static final long serialVersionUID = 1971800928047045825L;

            public void windowClose(CloseEvent ce) {

                // Only process if still enabled
                if (confirm.isEnabled()) {
                    confirm.setEnabled(false); // avoid double processing
                    confirm.setConfirmed(false);
                    if (confirm.getListener() != null) {
                        confirm.getListener().onClose(confirm);
                    }
                }
            }
        });

        // Create content
        VerticalLayout c = new VerticalLayout();
        confirm.setContent(c);
        c.setSizeFull();
        c.setSpacing(true);
        c.setMargin(true);

        // Panel for scrolling lengthy messages.
        VerticalLayout scrollContent = new VerticalLayout();
        Panel panel = new Panel(scrollContent);
        c.addComponent(panel);
        panel.setWidth("100%");
        panel.setHeight("100%");
        panel.setStyleName(Reindeer.PANEL_LIGHT);
        panel.addStyleName("borderless"); // valo compatibility
        c.setExpandRatio(panel, 1f);

        // Always HTML, but escape
        Label text = new Label("", com.vaadin.v7.shared.ui.label.ContentMode.HTML);
        text.setId(VaadinConfirmDialog.MESSAGE_ID);
        scrollContent.addComponent(text);
        confirm.setMessageLabel(text);
        confirm.setMessage(message);

        HorizontalLayout buttons = new HorizontalLayout();
        c.addComponent(buttons);
        c.setComponentAlignment(buttons, Alignment.TOP_RIGHT);
        buttons.setSpacing(true);

        final Button cancel = new Button(cancelCaption != null ? cancelCaption
                : DEFAULT_CANCEL_CAPTION);
        cancel.setData(null);
        cancel.setId(VaadinConfirmDialog.CANCEL_ID);
        cancel.setClickShortcut(KeyCode.ESCAPE, null);
        buttons.addComponent(cancel);
        confirm.setCancelButton(cancel);

        Button notOk = null;
        if (threeWay) {
            notOk = new Button(notOkCaption);
            notOk.setData(false);
            notOk.setId(VaadinConfirmDialog.NOT_OK_ID);
            buttons.addComponent(notOk);
            confirm.setCancelButton(notOk);
        }

        final Button ok = new Button(okCaption != null ? okCaption
                : DEFAULT_OK_CAPTION);
        ok.setData(true);
        ok.setId(VaadinConfirmDialog.OK_ID);
        ok.setClickShortcut(KeyCode.ENTER, null);
        ok.setStyleName(Reindeer.BUTTON_DEFAULT);
        ok.focus();
        buttons.addComponent(ok);
        confirm.setOkButton(ok);

        // Create a listener for buttons
        Button.ClickListener cb = new Button.ClickListener() {
            private static final long serialVersionUID = 3525060915814334881L;

            public void buttonClick(ClickEvent event) {
                // Copy the button date to window for passing through either
                // "OK" or "CANCEL". Only process id still enabled.
                if (confirm.isEnabled()) {
                    confirm.setEnabled(false); // Avoid double processing

                    Button b = event.getButton();
                    if (b != cancel)
                        confirm.setConfirmed(b == ok);

                    // We need to cast this way, because of the backward
                    // compatibility issue in 6.4 series.
                    UI parent = confirm.getUI();
                    parent.removeWindow(confirm);

                    // This has to be invoked as the window.close
                    // event is not fired when removed.
                    if (confirm.getListener() != null) {
                        confirm.getListener().onClose(confirm);
                    }
                }

            }

        };
        cancel.addClickListener(cb);
        ok.addClickListener(cb);
        if (notOk != null)
            notOk.addClickListener(cb);

        // Approximate the size of the dialog
        double[] dim = getDialogDimensions(message,
                VaadinConfirmDialog.ContentMode.TEXT_WITH_NEWLINES);
        confirm.setWidth(format(dim[0]) + "em");
        confirm.setHeight(format(dim[1]) + "em");
        confirm.setResizable(false);

        return confirm;
    }

    /**
     * Approximates the dialog dimensions based on its message length.
     *
     * @param message
     *            Message string
     * @return approximate size for the dialog with given message
     */
    protected double[] getDialogDimensions(String message,
            VaadinConfirmDialog.ContentMode style) {

        // Based on Reindeer style:
        double chrW = 0.51d;
        double chrH = 1.5d;
        double length = message != null? chrW * message.length() : 0;
        double rows = Math.ceil(length / MAX_WIDTH);

        // Estimate extra lines
        if (style == VaadinConfirmDialog.ContentMode.TEXT_WITH_NEWLINES) {
            rows += message != null? count("\n", message): 0;
        }

        //System.out.println(message.length() + " = " + length + "em");
        //System.out.println("Rows: " + (length / MAX_WIDTH) + " = " + rows);

        // Obey maximum size
        double width = Math.min(MAX_WIDTH, length);
        double height = Math.ceil(Math.min(MAX_HEIGHT, rows * chrH));

        // Obey the minimum size
        width = Math.max(width, MIN_WIDTH);
        height = Math.max(height, MIN_HEIGHT);

        // Based on Reindeer style:
        double btnHeight = 4d;
        double vmargin = 5d;
        double hmargin = 1d;

        double[] res = new double[] { width + hmargin,
                height + btnHeight + vmargin };
        //System.out.println(res[0] + "," + res[1]);
        return res;
    }

    /**
     * Count the number of needles within a haystack.
     *
     * @param needle
     *            The string to search for.
     * @param haystack
     *            The string to process.
     * @return count of needles within a haystack
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