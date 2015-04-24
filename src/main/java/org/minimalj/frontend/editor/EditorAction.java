package org.minimalj.frontend.editor;

import java.util.List;

import org.minimalj.frontend.editor.Editor.EditorListener;
import org.minimalj.frontend.page.Page;
import org.minimalj.frontend.toolkit.Action;
import org.minimalj.frontend.toolkit.ClientToolkit;
import org.minimalj.frontend.toolkit.ClientToolkit.ConfirmDialogResult;
import org.minimalj.frontend.toolkit.ClientToolkit.ConfirmDialogType;
import org.minimalj.frontend.toolkit.ClientToolkit.DialogListener;
import org.minimalj.frontend.toolkit.IDialog;
import org.minimalj.model.validation.ValidationMessage;

/**
 * An Action that shows a given Editor in a dialog if executed.
 * Dialog means the editor covers only the needed part of the screen
 * and blocks the rest of it.
 * 
 */
public class EditorAction extends Action {
	public static final String REFRESH = "refresh";
	
	private final Editor<?> editor;
	
	private IDialog dialog;
	private List<ValidationMessage> validationMessages;
	
	public EditorAction(Editor<?> editor) {
		this(editor, editor.getClass().getSimpleName());
	}
	
	public EditorAction(Editor<?> editor, String actionName) {
		super(actionName);
		this.editor = editor;
	}
	
	@Override
	public void action() {
		editor.setEditorListener(new EditorListener() {
			@Override
			public void saved(Object result) {
				dialog.closeDialog();
				if (result instanceof Page) {
					ClientToolkit.getToolkit().show((Page) result);
				} else {
					ClientToolkit.getToolkit().refresh();
				}
			}

			@Override
			public void canceled() {
				dialog.closeDialog();
			}

			@Override
			public void setValidationMessages(List<ValidationMessage> validationMessages) {
				EditorAction.this.validationMessages = validationMessages;
			}
		});
		editor.startEditor();
		dialog = ClientToolkit.getToolkit().showDialog(editor.getTitle(), editor.getContent(), new EditorCloseAction(), editor.getActions());
	}
	
	public class EditorCloseAction extends Action {

		@Override
		public void action() {
			checkedClose();
		}

		public void checkedClose() {
			if (!editor.isUserEdited()) {
				editor.cancel();
			} else if (editor.isSaveable()) {
				DialogListener listener = new DialogListener() {
					@Override
					public void close(ConfirmDialogResult answer) {
						if (answer == ConfirmDialogResult.YES) {
							// finish will be called at the end of save
							editor.save();
						} else if (answer == ConfirmDialogResult.NO) {
							editor.cancel();
						} // else do nothing (dialog will not close)
					}
				};
				ClientToolkit.getToolkit().showConfirmDialog("Sollen die aktuellen Eingaben gespeichert werden?", "Schliessen",
						ConfirmDialogType.YES_NO_CANCEL, listener);

			} else {
				DialogListener listener = new DialogListener() {
					@Override
					public void close(ConfirmDialogResult answer) {
						if (answer == ConfirmDialogResult.YES) {
							editor.cancel();
						} else { // No or Close
							// do nothing
						}
					}
				};
				
				ClientToolkit.getToolkit().showConfirmDialog("Die momentanen Eingaben sind nicht gültig\nund können daher nicht gespeichert werden.\n\nSollen sie verworfen werden?",
						"Schliessen", ConfirmDialogType.YES_NO, listener);
			}
		}

	}
	
}
