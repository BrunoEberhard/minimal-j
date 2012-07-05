package ch.openech.mj.edit;

import ch.openech.mj.page.PageAction;

/**
 * An Action that shows a given Editor as a page if executed.
 *
 */
public class EditorPageAction extends PageAction {
	
	public EditorPageAction(Class<? extends Editor<?>> editorClass, String... args) {
		super(EditorPage.class, getBaseName(editorClass), arguments(editorClass, args));
	}
	
	private static String[] arguments(Class<? extends Editor<?>> editorClass, String... args) {
		// TODO Elegantere Kopiererei der Argument in EditorPageAction
		String[] strings = new String[args.length + 1];
		strings[0] = editorClass.getSimpleName();
		for (int i = 0; i<args.length; i++) {
			strings[i+1] = args[i];
		}
		return strings;
	}

	private static String getBaseName(Class<? extends Editor<?>> editorClass) {
		// BaseName is used for ResourceBundle
		return editorClass.getSimpleName();
	}
}
