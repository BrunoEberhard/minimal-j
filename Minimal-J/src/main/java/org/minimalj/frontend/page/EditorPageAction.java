package org.minimalj.frontend.page;

import org.minimalj.frontend.edit.Editor;
import org.minimalj.frontend.toolkit.IAction;
import org.minimalj.frontend.toolkit.IComponent;

public class EditorPageAction implements IAction {

	private final Editor<?> editor;
	private boolean enabled = true;
	private ActionChangeListener changeListener;
	
	public EditorPageAction(Editor<?> editor) {
		this.editor = editor;
	}

	@Override
	public String getName() {
		return editor.getTitle();
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
	public void action(IComponent context) {
		((PageContext) context).show(editor);
	}

	@Override
	public String getDescription() {
		return null;
	}

	@Override
	public void setChangeListener(ActionChangeListener changeListener) {
		this.changeListener = changeListener;
	}
	
}
