package org.minimalj.frontend.page;

/**
 * If an page implements Parts the user can click through parts of the content.
 * For example the search results could be split in several parts.
 *
 */
public interface Parts {

	/**
	 * 
	 * @return the current part number (starting from 0)
	 */
	public abstract int getCurrentPart();

	/**
	 * 
	 * @return the number of part. A return value <code>1</code> should be handled special.
	 */
	public abstract int getPartCount();

	/**
	 * 
	 * @param number
	 */
	public abstract void setCurrentPart(int number);

}
