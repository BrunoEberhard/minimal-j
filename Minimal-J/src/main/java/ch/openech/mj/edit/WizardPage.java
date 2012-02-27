package ch.openech.mj.edit;

import java.util.List;

import ch.openech.mj.edit.validation.Indicator;
import ch.openech.mj.edit.validation.ValidationMessage;


/**
 * Interface for a wizard page.
 * <p>
 * The class <code>WizardPage</code> provides an abstract implementation
 * of this interface. However, clients are also free to implement this 
 * interface if <code>WizardPage</code> does not suit their needs.
 * </p>
 * <p>
 * Based on jface implementation
 * </p>
 */
public abstract class WizardPage<T> extends Editor<T> {

    /**
	 * Returns this dialog page's title.
	 *
	 * @return the title of this dialog page, 
	 *  or <code>null</code> if none
	 */
	public abstract String getTitle();

	/**
     * Returns this dialog page's description text.
     *
     * @return the description text for this dialog page, 
     *  or <code>null</code> if none
     */
    public abstract String getDescription();

    /**
     * Returns the current message for this wizard page.
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
	 * Returns whether this page is complete or not.
	 * <p>
	 * This information is typically used by the wizard to decide
	 * when it is okay to finish.
	 * </p>
	 *
	 * @return <code>true</code> if this page is complete, and
	 *  <code>false</code> otherwise
	 */
	public abstract boolean canFinish();

	/**
     * Returns the wizard page that would to be shown if the user was to
     * press the Next button.
     *
     * @return the next wizard page, or <code>null</code> if none
     */
    public abstract WizardPage<?> getNextPage();

    /**
     * Returns the wizard page that would to be shown if the user was to
     * press the Back button.
     *
     * @return the previous wizard page, or <code>null</code> if none
     */
    public abstract WizardPage<?> getPreviousPage();
    
}