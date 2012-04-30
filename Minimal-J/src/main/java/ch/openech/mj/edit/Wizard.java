package ch.openech.mj.edit;

import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;

import ch.openech.mj.edit.form.IForm;
import ch.openech.mj.edit.form.SwitchForm;
import ch.openech.mj.edit.validation.Indicator;
import ch.openech.mj.edit.validation.ValidationMessage;
import ch.openech.mj.page.PageContext;
import ch.openech.mj.resources.ResourceHelper;
import ch.openech.mj.resources.Resources;

public abstract class Wizard<T> extends Editor<T> {

	private WizardPage<?> currentPage;
	private int currentPageIndex = 0;
	
	protected final Action prevAction;
	protected final Action nextAction;
	private SwitchForm<T> switchForm;
	private final Indicator indicator;
	
	protected Wizard() {
		nextAction = createNextAction();
		prevAction = createPrevAction();
		indicator = new WizardIndicator();
	}

	protected abstract WizardPage<?> getFirstPage();
	
	@Override
	public Action[] getActions() {
		return new Action[]{demoDataAction, cancelAction, prevAction, nextAction, saveAction};
	}

	protected int getCurrentPageIndex() {
		return currentPageIndex;
	}
	
	protected Action createNextAction() {
		Action action = new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (currentPage.isSaveable()) {
					currentPage.save();
					currentPageIndex++;
					setCurrentPage(currentPage.getNextPage());
				}
			}
		};
		ResourceHelper.initProperties(action, Resources.getResourceBundle(), "NextWizardPageAction");
		return action;
	}

	protected Action createPrevAction() {
		Action action = new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				currentPage.finish();
				currentPageIndex--;
				setCurrentPage(currentPage.getPreviousPage());
			}
		};
		ResourceHelper.initProperties(action, Resources.getResourceBundle(), "PreviousWizardPageAction");
		return action;
	}

	@Override
	protected final IForm<T> createForm() {
		switchForm = new SwitchForm<T>();
		return switchForm;
	}
	
	@Override
	public IForm<T> startEditor(PageContext context) {
		IForm<T> formVisual = super.startEditor(context);
		setCurrentPage(getFirstPage());
		return formVisual;
	}

	@Override
	public void finish() {
		currentPage.finish();
		super.finish();
	}

	private void setCurrentPage(WizardPage<?> page) {
		currentPage = page;
		currentPage.setIndicator(indicator);
		switchForm.setForm(currentPage.startEditor(context));
		prevAction.setEnabled(currentPageIndex > 0);
	}

	@Override
	protected abstract T load();

	@Override
	protected void validate(T object, List<ValidationMessage> resultList) {
		// not used
	}

	@Override
	protected abstract boolean save(T object);
	
	private class WizardIndicator implements Indicator {

		@Override
		public void setValidationMessages(List<ValidationMessage> validationResult) {
			Wizard.this.indicate(validationResult);
			nextAction.setEnabled(isSaveable());
			saveAction.setEnabled(isSaveable() && currentPage.canFinish());
		}
	}

	@Override
	public void fillWithDemoData() {
		currentPage.fillWithDemoData();
	}
	 
}
