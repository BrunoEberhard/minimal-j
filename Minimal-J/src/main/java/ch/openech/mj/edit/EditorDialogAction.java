package ch.openech.mj.edit;

import ch.openech.mj.edit.Editor.EditorListener;
import ch.openech.mj.edit.form.IForm;
import ch.openech.mj.toolkit.ClientToolkit;
import ch.openech.mj.toolkit.IComponent;
import ch.openech.mj.toolkit.IDialog;
import ch.openech.mj.toolkit.ResourceAction;

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
		IForm<?> form = editor.startEditor();
		final IDialog dialog = ClientToolkit.getToolkit().createDialog(context, editor.getTitle(), form.getComponent(), editor.getActions());
		
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
