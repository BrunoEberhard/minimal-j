package ch.openech.mj.edit;

import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;

import ch.openech.mj.edit.form.FormVisual;
import ch.openech.mj.edit.validation.Indicator;
import ch.openech.mj.edit.validation.ValidationMessage;
import ch.openech.mj.resources.ResourceAction;
import ch.openech.mj.resources.ResourceHelper;
import ch.openech.mj.resources.Resources;

public abstract class Wizard<T> extends Editor<T> {

	private WizardPage<?> currentPage;
	private int currentPageIndex = 0;
	
	protected final Action prevAction;
	protected final Action nextAction;
	protected final Action demoAction;
	private SwitchFormVisual<?> switchFormVisual;
	private final Indicator indicator;
	
	protected Wizard() {
		nextAction = createNextAction();
		prevAction = createPrevAction();
		demoAction = new FillWithDemoDataAction();
		indicator = new WizardIndicator();
	}

	protected abstract WizardPage<?> getFirstPage();
	
	@Override
	public Action[] getActions() {
		return new Action[]{demoAction, cancelAction, prevAction, nextAction, saveAction};
	}

	protected int getCurrentPageIndex() {
		return currentPageIndex;
	}
	
	protected Action createNextAction() {
		Action action = new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				currentPage.save();
				currentPageIndex++;
				setCurrentPage(currentPage.getNextPage());
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
	protected final FormVisual createForm() {
		switchFormVisual = new SwitchFormVisual<Object>();
		setCurrentPage(getFirstPage());
		return switchFormVisual;
	}
	
	@Override
	public void finish() {
		currentPage.finish();
		super.finish();
	}

	private void setCurrentPage(WizardPage<?> page) {
		currentPage = page;
		currentPage.setIndicator(indicator);
		switchFormVisual.setFormVisual(currentPage.startEditor());
		prevAction.setEnabled(currentPageIndex > 0);
	}

	@Override
	protected abstract T load();

	@Override
	protected void validate(T object, List resultList) {
		// not used
	}

	@Override
	protected abstract boolean save(T object);
	
	private class WizardIndicator implements Indicator {

		@Override
		public void setValidationMessages(List<ValidationMessage> validationMessages) {
			boolean noErrors = validationMessages.isEmpty();
			nextAction.setEnabled(noErrors);
			saveAction.setEnabled(noErrors && currentPage.canFinish());
		}
	}
	
	 private class FillWithDemoDataAction extends ResourceAction {
	 @Override
		public void actionPerformed(ActionEvent arg0) {
			// boolean generateData = PreferencesHelper.preferences().getBoolean("generateData", false);
			if (true) {
				fillWithDemoData();
			}
		}
	}

	/* (non-Javadoc)
	 * @see ch.openech.mj.edit.Editor#fillWithDemoData()
	 */
	@Override
	public void fillWithDemoData() {
		currentPage.fillWithDemoData();
	}
	 
}
