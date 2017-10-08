package org.minimalj.frontend.impl.vaadin.toolkit;

import java.io.Serializable;

import com.vaadin.server.JsonPaintTarget;
import com.vaadin.ui.Button;
import com.vaadin.ui.Label;
import com.vaadin.ui.UI;
import com.vaadin.ui.Window;

/**
 * Based on https://github.com/samie/Vaadin-ConfirmDialog/tree/vaadin7
 *
 */
public class VaadinConfirmDialog extends Window {

    private static final long serialVersionUID = -2363125714643244070L;

    public interface Factory extends Serializable {
    	VaadinConfirmDialog create(String windowCaption, String message,
                String okTitle, String cancelTitle, String notOKCaption);
    }

    /* Test IDs for elements */
    public static final String DIALOG_ID = "confirmdialog-window";
    public static final String MESSAGE_ID = "confirmdialog-message";
    public static final String OK_ID = "confirmdialog-ok-button";
    public static final String NOT_OK_ID = "confirmdialog-not-ok-button";
    public static final String CANCEL_ID = "confirmdialog-cancel-button";

    public enum ContentMode {
        TEXT_WITH_NEWLINES, TEXT, PREFORMATTED, HTML
    };

    /**
     * Listener for dialog close events. Implement and register an instance of
     * this interface to dialog to receive close events.
     *
     * @author Sami Ekblad
     *
     */
    public interface Listener extends Serializable {
        void onClose(VaadinConfirmDialog dialog);
    }

    /**
     * Default dialog factory.
     *
     */
    private static VaadinConfirmDialog.Factory factoryInstance;

    /**
     * Get the VaadinConfirmDialog.Factory used to create and configure the dialog.
     *
     * By default the {@link DefaultVaadinConfirmDialogFactory} is used.
     *
     * @return the currently used VaadinConfirmDialog.Factory
     */
    public static VaadinConfirmDialog.Factory getFactory() {
        if (factoryInstance == null) {
            factoryInstance = new DefaultConfirmDialogFactory();
        }
        return factoryInstance;
    }

    /**
     * Set the VaadinConfirmDialog.Factory used to create and configure the dialog.
     *
     * By default the {@link DefaultVaadinConfirmDialogFactory} is used.
     * 
     * @param newFactory the VaadinConfirmDialog factory to be used
     *
     */
    public static void setFactory(final VaadinConfirmDialog.Factory newFactory) {
        factoryInstance = newFactory;
    }

    /**
     * Show a modal VaadinConfirmDialog in a window.
     *
     * @param ui the UI in which the dialog is to be show
     * @param listener the listener to be notified
     * @return the VaadinConfirmDialog instance created
     */
    public static VaadinConfirmDialog show(final UI ui, final Listener listener) {
        return show(ui, null, null, null, null, listener);
    }

    /**
     * Show a modal VaadinConfirmDialog in a window.
     *
     * @param ui the UI in which the dialog is to be show
     * @param message the message shown in the dialog
     * @param listener the listener to be notified
     * @return the VaadinConfirmDialog instance created
     */
    public static VaadinConfirmDialog show(final UI ui, final String message,
            final Listener listener) {
        return show(ui, null, message, null, null, listener);
    }

    /**
     * Show a modal three way (eg. yes/no/cancel) VaadinConfirmDialog in a window.
     * 
     * @param ui
     *            UI
     * @param windowCaption
     *            Caption for the confirmation dialog window.
     * @param message
     *            Message to display as window content.
     * @param okCaption
     *            Caption for the ok button.
     * @param cancelCaption
     *            Caption for cancel button.
     * @param notOKCaption
     *            Caption for notOK button.
     * @param listener
     *            Listener for dialog result.
     * @return the VaadinConfirmDialog instance created
     */
    public static VaadinConfirmDialog show(final UI ui,
            final String windowCaption, final String message,
            final String okCaption, final String cancelCaption,
            final String notOKCaption, final Listener listener) {
        VaadinConfirmDialog d = getFactory().create(windowCaption, message,
                okCaption, cancelCaption, notOKCaption);
        d.show(ui, listener, true);
        return d;
    }

    /**
     * Show a modal VaadinConfirmDialog in a window.
     *
     * @param ui
     *            Main level UI.
     * @param windowCaption
     *            Caption for the confirmation dialog window.
     * @param message
     *            Message to display as window content.
     * @param okCaption
     *            Caption for the ok button.
     * @param cancelCaption
     *            Caption for cancel button.
     * @param listener
     *            Listener for dialog result.
     * @return the VaadinConfirmDialog that was instantiated
     */
    public static VaadinConfirmDialog show(final UI ui,
            final String windowCaption, final String message,
            final String okCaption, final String cancelCaption,
            final Listener listener) {
        VaadinConfirmDialog d = getFactory().create(windowCaption, message,
                okCaption, cancelCaption, null);
        d.show(ui, listener, true);
        return d;
    }

    /**
     * Shows a modal VaadinConfirmDialog in given window and executes Runnable if OK
     * is chosen.
     *
     * @param ui
     *            Main level UI.
     * @param windowCaption
     *            Caption for the confirmation dialog window.
     * @param message
     *            Message to display as window content.
     * @param okCaption
     *            Caption for the ok button.
     * @param cancelCaption
     *            Caption for cancel button.
     * @param r
     *            Runnable to be run if confirmed
     * @return the VaadinConfirmDialog that was instantiated
     */
    public static VaadinConfirmDialog show(final UI ui,
            final String windowCaption, final String message,
            final String okCaption, final String cancelCaption, final Runnable r) {
        VaadinConfirmDialog d = getFactory().create(windowCaption, message,
                okCaption, cancelCaption, null);
        d.show(ui, new Listener() {
            private static final long serialVersionUID = 1L;

            public void onClose(VaadinConfirmDialog dialog) {
                if (dialog.isConfirmed()) {
                    r.run();
                }
            }
        }, true);
        return d;
    }

    private Listener confirmListener = null;
    private Boolean isConfirmed = null;
    private Label messageLabel = null;
    private Button okBtn = null;
    private Button cancelBtn = null;
    private String originalMessageText;
    private ContentMode msgContentMode = ContentMode.TEXT_WITH_NEWLINES;

    /**
     * Show confirm dialog.
     *
     * @param ui the UI in which the dialog should be shown
     * @param listener the listener to be notified
     * @param modal true if the dialog should be modal
     */
    public final void show(final UI ui, final Listener listener,
            final boolean modal) {
        confirmListener = listener;
        center();
        setModal(modal);
        ui.addWindow(this);
    }

    /**
     * Did the user confirm the dialog.
     *
     * @return true if user confirmed
     */
    public final boolean isConfirmed() {
        return isConfirmed != null && isConfirmed;
    }

    /**
     * Did the user cancel the dialog.
     * 
     * @return true if the dialog was canceled
     */
    public final boolean isCanceled() {
        return isConfirmed == null;
    }

    public final Listener getListener() {
        return confirmListener;
    }

    protected final void setOkButton(final Button okButton) {
        okBtn = okButton;
    }

    public final Button getOkButton() {
        return okBtn;
    }

    protected final void setCancelButton(final Button cancelButton) {
        cancelBtn = cancelButton;
    }

    public final Button getCancelButton() {
        return cancelBtn;
    }

    protected final void setMessageLabel(final Label message) {
        messageLabel = message;
    }

    public final void setMessage(final String message) {
        originalMessageText = message;
        messageLabel
                .setValue(ContentMode.TEXT_WITH_NEWLINES == msgContentMode ? formatDialogMessage(message)
                        : message);
    }

    public final String getMessage() {
        return originalMessageText;
    }

    public final ContentMode getContentMode() {
        return msgContentMode;
    }

    public final void setContentMode(final ContentMode contentMode) {
        msgContentMode = contentMode;
        com.vaadin.shared.ui.ContentMode labelContentMode = com.vaadin.shared.ui.ContentMode.TEXT;
        switch (contentMode) {
        case TEXT_WITH_NEWLINES:
        case TEXT:
            labelContentMode = com.vaadin.shared.ui.ContentMode.TEXT;
            break;
        case PREFORMATTED:
            labelContentMode = com.vaadin.shared.ui.ContentMode.PREFORMATTED;
            break;
        case HTML:
            labelContentMode = com.vaadin.shared.ui.ContentMode.HTML;
            break;
        }
        messageLabel
                .setContentMode(labelContentMode);
        messageLabel
                .setValue(contentMode == ContentMode.TEXT_WITH_NEWLINES ? formatDialogMessage(originalMessageText)
                        : originalMessageText);
    }

    /**
     * Format the messageLabel by maintaining text only.
     *
     * @param text the text to be formatted
     * @return formatted text
     */
    protected final String formatDialogMessage(final String text) {
        return JsonPaintTarget.escapeXML(text).replaceAll("\n", "<br />");
    }

    /**
     * Set the isConfirmed state.
     *
     * Note: this should only be called internally by the listeners.
     *
     * @param confirmed true if dialog was confirmed
     */
    protected final void setConfirmed(final boolean confirmed) {
        isConfirmed = confirmed;
    }
}
