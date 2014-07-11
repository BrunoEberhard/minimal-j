package org.minimalj.frontend.edit;

import org.minimalj.frontend.edit.Editor.EditorListener;
import org.minimalj.frontend.toolkit.ClientToolkit;
import org.minimalj.frontend.toolkit.IComponent;
import org.minimalj.frontend.toolkit.IDialog;
import org.minimalj.frontend.toolkit.ResourceAction;

/**
 * An Action that shows a given Editor in a dialog if executed.
 * Dialog means the editor covers only the needed part of the screen
 * and blocks the rest of it.<p>
 * 
 * If the Editor should cover all of the screen use EditorPageAction.
 *
 */
public class EditorDialogAction extends ResourceAction {
	private final Editor<?> editor;
	
	public EditorDialogAction(Editor<?> editor) {
		this(editor, editor.getClass().getSimpleName());
	}
	
	public EditorDialogAction(Editor<?> editor, String actionName) {
		super(actionName);
		this.editor = editor;
	}
	
	@Override
	public void action(IComponent context) {
		showDialogOn(context);
	}

	public void showDialogOn(IComponent context) {
		editor.startEditor();
		final IDialog dialog = ClientToolkit.getToolkit().createDialog(context, editor.getTitle(), editor.getComponent(), editor.getActions());
		
		dialog.setCloseListener(new IDialog.CloseListener() {
			@Override
			public boolean close() {
				editor.checkedClose();
				return editor.isFinished();
			}
		});
		
		editor.setEditorListener(new EditorListener() {
			@Override
			public void saved(Object savedObject) {
				dialog.closeDialog();
			}

			@Override
			public void canceled() {
				dialog.closeDialog();
			}
		});
		dialog.openDialog();
	}
	
}
