package ch.openech.mj.edit;

/**
 * Named <i>Step</i> so not to be confused with the concept of the pages.
 * Loosly based on jface WizardPages.
 * 
 */
public abstract class WizardStep<T> extends Editor<T> {

    /**
	 * Returns this dialog step's title.
	 *
	 * @return the title of this wizard step, 
	 *  or <code>null</code> if none
	 */
	@Override
	public abstract String getTitle();

	/**
     * Returns this wizard step's description text.
     *
     * @return the description text for this wizard step, 
     *  or <code>null</code> if none
     */
    public abstract String getDescription();

    /**
     * Returns the current message for this wizard step.
     * <p>
     * A message provides instruction or information to the 
     * user, as opposed to an error message which should 
     * describe some error state.
     * </p>
     * 
     * @return the message, or <code>null</code> if none
     */
    public abstract String getMessage();

    /**
	 * Returns whether this step is complete or not.
	 * <p>
	 * This information is typically used by the wizard to decide
	 * when it is okay to finish.
	 * </p>
	 *
	 * @return <code>true</code> if this step is complete, and
	 *  <code>false</code> otherwise
	 */
	public abstract boolean canFinish();

	/**
     * Returns the wizard step that would to be shown if the user was to
     * press the Next button.
     *
     * @return the next wizard step, or <code>null</code> if none
     */
    public abstract WizardStep<?> getNextStep();

    /**
     * Returns the wizard step that would to be shown if the user was to
     * press the Back button.
     *
     * @return the previous wizard step, or <code>null</code> if none
     */
    public abstract WizardStep<?> getPreviousStep();
    
}