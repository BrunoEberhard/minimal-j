package org.minimalj.frontend.edit;

import org.minimalj.frontend.edit.Editor.EditorListener;
import org.minimalj.frontend.page.ObjectPage;
import org.minimalj.frontend.toolkit.ClientToolkit;
import org.minimalj.frontend.toolkit.IDialog;
import org.minimalj.frontend.toolkit.ResourceAction;

/**
 * An Action that shows a given Editor in a dialog if executed.
 * Dialog means the editor covers only the needed part of the screen
 * and blocks the rest of it.<p>
 * 
 */
public class EditorAction extends ResourceAction {
	private final Editor<?> editor;
	private boolean enabled = true;
	private ActionChangeListener changeListener;
	private ObjectPage objectPage;
	
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
			public void saved(Object savedObject) {
				dialog.closeDialog();
				if (objectPage != null) {
					objectPage.updateObject(savedObject);
				}
			}

			@Override
			public void canceled() {
				dialog.closeDialog();
			}
		});
		dialog.openDialog();
	}
	
	@Override
	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
		if (changeListener != null) {
			changeListener.change();
		}
	}
	
	@Override
	public void setChangeListener(ActionChangeListener changeListener) {
		this.changeListener = changeListener;
	}
	
	public void setObjectViewPage(ObjectPage objectPage) {
		this.objectPage = objectPage;
	}
	
}
