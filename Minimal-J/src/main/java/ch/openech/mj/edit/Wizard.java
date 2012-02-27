package ch.openech.mj.edit;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;

import ch.openech.mj.edit.form.FormVisual;
import ch.openech.mj.resources.ResourceHelper;
import ch.openech.mj.resources.Resources;

public abstract class Wizard<T> extends Editor<T> {

	private WizardPage<?> currentPage;
	
	protected final Action prevAction;
	protected final Action nextAction;
//	protected final Action finishAction;
	private SwitchFormVisual<?> switchFormVisual;
	
	protected Wizard() {
		nextAction = createNextAction();
		prevAction = createPrevAction();
	}

	protected abstract WizardPage<?> getFirstPage();
	
	@Override
	public Action[] getActions() {
		return new Action[]{cancelAction, prevAction, nextAction, saveAction};
	}

	protected Action createNextAction() {
		Action action = new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				currentPage.save();
				setCurrentPage(currentPage.getNextPage());
			}
		};
		ResourceHelper.initProperties(action, Resources.getResourceBundle(), "NextWizardPage<?>Action");
		return action;
	}

	protected Action createPrevAction() {
		Action action = new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				setCurrentPage(currentPage.getPreviousPage());
			}
		};
		ResourceHelper.initProperties(action, Resources.getResourceBundle(), "PreviousWizardPage<?>Action");
		return action;
	}

	@Override
	protected final FormVisual createForm() {
		switchFormVisual = new SwitchFormVisual<Object>();
		setCurrentPage(getFirstPage());
		return switchFormVisual;
	}
	
	private void setCurrentPage(WizardPage<?> page) {
		this.currentPage = page;
		switchFormVisual.setFormVisual(currentPage.startEditor());
	}

	@Override
	protected abstract T load();

	@Override
	protected void validate(T object, List resultList) {
		// not used
	}

	@Override
	protected abstract boolean save(T object);
	
}
