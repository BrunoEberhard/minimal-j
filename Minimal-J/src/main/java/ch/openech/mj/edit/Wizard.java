package ch.openech.mj.edit;

import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;

import ch.openech.mj.edit.form.FormVisual;
import ch.openech.mj.resources.ResourceHelper;
import ch.openech.mj.resources.Resources;

public abstract class Wizard<T> extends Editor<T> {

	protected final Action prevAction;
	protected final Action nextAction;
//	protected final Action finishAction;
	private Editor<?> actualEditor;
	private SwitchFormVisual<?> switchFormVisual;
	
	/**
	 * 
	 * @return the next editor (page)
	 */
	protected abstract Editor<?> getNextEditor();
	
	protected abstract Editor<?> getPrevEditor();

	@Override
	protected Action[] getActions() {
		return new Action[]{cancelAction, prevAction, nextAction, saveAction};
	}

	protected Wizard() {
		nextAction = createNextAction();
		prevAction = createPrevAction();
	}
	

	protected Action createNextAction() {
		Action action = new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				actualEditor.save();
				setActualEditor(getNextEditor());
			}
		};
		ResourceHelper.initProperties(action, Resources.getResourceBundle(), "NextWizardPageAction");
		return action;
	}

	protected Action createPrevAction() {
		Action action = new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				setActualEditor(getPrevEditor());
			}
		};
		ResourceHelper.initProperties(action, Resources.getResourceBundle(), "PreviousWizardPageAction");
		return action;
	}

	@Override
	protected final FormVisual createForm() {
		switchFormVisual = new SwitchFormVisual<Object>();
		setActualEditor(getNextEditor());
		return switchFormVisual;
	}
	
	protected void setActualEditor(Editor<?> editor) {
		this.actualEditor = editor;
		switchFormVisual.setFormVisual(actualEditor.startEditor());
	}

	@Override
	protected abstract T load();

	@Override
	protected void validate(T object, List resultList) {
		// TODO Auto-generated method stub
	}

	@Override
	protected abstract boolean save(T object);
	
}
