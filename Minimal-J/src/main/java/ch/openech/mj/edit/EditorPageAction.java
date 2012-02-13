package ch.openech.mj.edit;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import ch.openech.mj.page.Page;
import ch.openech.mj.page.PageContext;
import ch.openech.mj.resources.ResourceHelper;
import ch.openech.mj.resources.Resources;

public class EditorPageAction extends AbstractAction {
	private final Class<? extends Editor<?>> editor;
	private final String[] args;
	private final PageContext pageContext;
	
	// Das Problem ist, dass für weitere Aufrufe ein neuer Editor erstellt werden sollte
	
	public EditorPageAction(PageContext pageContext, Class<? extends Editor<?>> editor, String... args) {
		this.pageContext = pageContext;
		this.editor = editor;
		this.args = args;

		String actionName = editor.getSimpleName();
		ResourceHelper.initProperties(this, Resources.getResourceBundle(), actionName);
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		try {
			doActionPerformed(e);
		} catch (Exception x) {
			// TODO show correct error message in EditorPageAction
			x.printStackTrace();
		}
	}
	
	private void doActionPerformed(ActionEvent e) throws Exception {
		// TODO Elegantere Kopiererei der Argument in EditorPageAction
		String[] strings = new String[args.length + 1];
		strings[0] = editor.getName();
		for (int i = 0; i<args.length; i++) {
			strings[i+1] = args[i];
		}
		pageContext.show(Page.link(EditorPage.class, strings));
	}
	
}
