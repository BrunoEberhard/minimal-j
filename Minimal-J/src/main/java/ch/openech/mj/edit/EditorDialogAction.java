package ch.openech.mj.edit;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import ch.openech.mj.edit.Editor.EditorFinishedListener;
import ch.openech.mj.edit.form.FormVisual;
import ch.openech.mj.resources.ResourceHelper;
import ch.openech.mj.resources.Resources;
import ch.openech.mj.toolkit.ClientToolkit;
import ch.openech.mj.toolkit.VisualDialog;

public class EditorDialogAction extends AbstractAction {
	private final Editor<?> editor;
	private final Object parentComponent;
	
	public EditorDialogAction(Object parentComponent, Editor<?> editor) {
		this.editor = editor;
		this.parentComponent = parentComponent;
		
		String actionName = editor.getClass().getSimpleName();
		ResourceHelper.initProperties(this, Resources.getResourceBundle(), actionName);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		try {
			doActionPerformed(e);
		} catch (Exception x) {
			// TODO show dialog
			x.printStackTrace();
		}
	}
	
	private void doActionPerformed(ActionEvent e) throws Exception {
		FormVisual<?> form = editor.startEditor();
		Object layout = ClientToolkit.getToolkit().createEditorLayout(editor.getInformation(), form.getComponent(), editor.getActions());
		
		final VisualDialog dialog = ClientToolkit.getToolkit().openDialog(parentComponent, layout, editor.getTitle());
		dialog.setResizable(form.isResizable());
		
		editor.setEditorFinishedListener(new EditorFinishedListener() {
			@Override
			public void finished() {
				dialog.setVisible(false);
			}
		});
		dialog.setVisible(true);
	}
	
}
