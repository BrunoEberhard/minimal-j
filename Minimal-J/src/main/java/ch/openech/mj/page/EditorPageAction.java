package ch.openech.mj.page;

import ch.openech.mj.edit.Editor;
import ch.openech.mj.resources.Resources;
import ch.openech.mj.toolkit.IAction;
import ch.openech.mj.toolkit.IComponent;

public class EditorPageAction implements IAction {

	private final Editor<?> editor;
	private boolean enabled = true;
	private ActionChangeListener changeListener;
	
	public EditorPageAction(Editor<?> editor) {
		this.editor = editor;
	}

	@Override
	public String getName() {
		return Resources.getString(editor.getClass().getSimpleName() + ".text");
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
