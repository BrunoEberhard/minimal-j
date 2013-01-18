package ch.openech.mj.edit;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import ch.openech.mj.edit.Editor.EditorFinishedListener;
import ch.openech.mj.edit.form.IForm;
import ch.openech.mj.page.PageContext;
import ch.openech.mj.page.PageContextHelper;
import ch.openech.mj.resources.ResourceHelper;
import ch.openech.mj.resources.Resources;
import ch.openech.mj.toolkit.ClientToolkit;
import ch.openech.mj.toolkit.IComponent;
import ch.openech.mj.toolkit.ProgressListener;
import ch.openech.mj.toolkit.VisualDialog;
import ch.openech.mj.toolkit.VisualDialog.CloseListener;

/**
 * An Action that shows a given Editor in a dialog if executed.
 * Dialog means the editor covers only the needed part of the screen.
 * If the Editor should cover all of the screen use EditorPageAction.
 *
 */
public class EditorDialogAction extends AbstractAction {
	private final Editor<?> editor;

	public EditorDialogAction(Editor<?> editor) {
		this(editor, editor.getClass().getSimpleName());
	}
	
	public EditorDialogAction(Editor<?> editor, String actionName) {
		this.editor = editor;
		ResourceHelper.initProperties(this, Resources.getResourceBundle(), actionName);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		try {
			PageContext pageContext = PageContextHelper.findContext(e.getSource());
			showDialogOn(pageContext);
		} catch (Exception x) {
			// TODO show dialog
			x.printStackTrace();
		}
	}

	public void showDialogOn(final PageContext context) {
		IForm<?> form = editor.startEditor();
		IComponent layout = ClientToolkit.getToolkit().createEditorLayout(form.getComponent(), editor.getActions());
		final VisualDialog dialog = ClientToolkit.getToolkit().openDialog(context, layout, editor.getTitle());
		dialog.setResizable(form.isResizable());
		
		dialog.setCloseListener(new CloseListener() {
			@Override
			public boolean close() {
				editor.checkedClose();
				return editor.isFinished();
			}
		});
		
		editor.setEditorFinishedListener(new EditorFinishedListener() {
			private ProgressListener progressListener;
			
			@Override
			public void finished(String followLink) {
				if (progressListener != null) {
					progressListener.showProgress(100, 100);
				}
				if (followLink != null) {
					context.show(followLink);
				}
				dialog.closeDialog();
			}

			@Override
			public void progress(int value, int maximum) {
				if (progressListener == null) {
					progressListener = ClientToolkit.getToolkit().showProgress(context, "Save");
				}
				progressListener.showProgress(value, maximum);
			}
		});
		dialog.openDialog();
		ClientToolkit.getToolkit().focusFirstComponent(form.getComponent());
	}
	
}
