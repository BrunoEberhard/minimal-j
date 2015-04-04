package org.minimalj.frontend.edit;

import org.minimalj.frontend.edit.Editor.EditorListener;
import org.minimalj.frontend.page.Page;
import org.minimalj.frontend.toolkit.Action;
import org.minimalj.frontend.toolkit.ClientToolkit;
import org.minimalj.frontend.toolkit.IDialog;

/**
 * An Action that shows a given Editor in a dialog if executed.
 * Dialog means the editor covers only the needed part of the screen
 * and blocks the rest of it.
 * 
 */
public class EditorAction extends Action {
	public static final String REFRESH = "refresh";
	
	private final Editor<?> editor;
	
	public EditorAction(Editor<?> editor) {
		this(editor, editor.getClass().getSimpleName());
	}
	
	public EditorAction(Editor<?> editor, String actionName) {
		super(actionName);
		this.editor = editor;
	}
	
	@Override
	public void action() {
		editor.startEditor();
		
		// TODO this should be replaced by show(Editor) in the ClientToolkit to be
		// symetrical with the method show(Page)
		
		final IDialog dialog = ClientToolkit.getToolkit().createDialog(editor.getTitle(), editor.getContent(), editor.getActions());
		
		dialog.setCloseListener(new IDialog.CloseListener() {
			@Override
			public boolean close() {
				editor.checkedClose();
				return editor.isFinished();
			}
		});
		
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
		});
		dialog.openDialog();
	}
}
