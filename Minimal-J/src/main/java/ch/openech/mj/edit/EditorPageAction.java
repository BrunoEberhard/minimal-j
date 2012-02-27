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

public class EditorPageAction extends AbstractAction {
	
	private final Class<? extends Editor<?>> editorClass;
	private final String[] parameter;
	
	@Override
	public void actionPerformed(ActionEvent e) {
		PageContext pageContext = ClientToolkit.getToolkit().findPageContext(e.getSource());
		showPageOn(pageContext);
	}

	private void showPageOn(PageContext context) {
		Editor<?> editor = createEditor(editorClass, parameter);
		
		FormVisual<?> form = editor.startEditor();
		IComponent layout = ClientToolkit.getToolkit().createEditorLayout(editor.getInformation(), form, editor.getActions());
		
		final VisualDialog dialog = ClientToolkit.getToolkit().openDialog(context.getComponent(), layout, editor.getTitle());
		dialog.setResizable(form.isResizable());
		
		editor.setEditorFinishedListener(new EditorFinishedListener() {
			@Override
			public void finished() {
				dialog.closeDialog();
			}
		});
		dialog.openDialog();
	}
	
	static Editor<?> createEditor(Class<?> editorClass, String... arguments) {
		try {
			if (arguments.length > 0) {
				Class<?>[] argumentClasses = new Class[arguments.length];
				for (int i = 0; i<argumentClasses.length; i++) {
					argumentClasses[i] = String.class;
				}
				return (Editor<?>) editorClass.getConstructor(argumentClasses).newInstance(arguments);
			} else {
				return (Editor<?>) editorClass.newInstance();
			}
		} catch (Exception x) {
			throw new RuntimeException("EditorPage Erstellung fehlgeschlagen", x);
		}
	}
	
	public EditorPageAction(Class<? extends Editor<?>> editorClass, String... parameter) {
		this.editorClass = editorClass;
		this.parameter = parameter;
		ResourceHelper.initProperties(this, Resources.getResourceBundle(), getBaseName(editorClass));
	}

	private static String getBaseName(Class<? extends Editor<?>> editorClass) {
		return editorClass.getSimpleName();
	}
	
}
