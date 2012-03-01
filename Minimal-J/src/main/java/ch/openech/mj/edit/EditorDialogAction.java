package ch.openech.mj.edit;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import ch.openech.mj.edit.Editor.EditorFinishedListener;
import ch.openech.mj.edit.form.FormVisual;
import ch.openech.mj.page.PageContext;
import ch.openech.mj.resources.ResourceHelper;
import ch.openech.mj.resources.Resources;
import ch.openech.mj.toolkit.ClientToolkit;
import ch.openech.mj.toolkit.IComponent;
import ch.openech.mj.toolkit.VisualDialog;
import ch.openech.mj.toolkit.VisualDialog.CloseListener;

public class EditorDialogAction extends AbstractAction {
	private final Editor<?> editor;
	
	public EditorDialogAction(Editor<?> editor) {
		this.editor = editor;
		
		String actionName = editor.getClass().getSimpleName();
		ResourceHelper.initProperties(this, Resources.getResourceBundle(), actionName);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		try {
			PageContext pageContext = ClientToolkit.getToolkit().findPageContext(e.getSource());
			showPageOn(pageContext);
		} catch (Exception x) {
			// TODO show dialog
			x.printStackTrace();
		}
	}
	
	private void showPageOn(PageContext context) {
		FormVisual<?> form = editor.startEditor();
		IComponent layout = ClientToolkit.getToolkit().createEditorLayout(editor.getInformation(), form, editor.getActions());
		
		final VisualDialog dialog = ClientToolkit.getToolkit().openDialog(context.getComponent(), layout, editor.getTitle());
		dialog.setResizable(form.isResizable());
		
		dialog.setCloseListener(new CloseListener() {
			@Override
			public boolean close() {
				editor.checkedClose();
				return editor.isFinished();
			}
		});
		
		editor.setEditorFinishedListener(new EditorFinishedListener() {
			@Override
			public void finished() {
				dialog.closeDialog();
			}
		});
		dialog.openDialog();
	}
	
}
