package org.minimalj.model;

public interface Model {

	/**
	 * Defines the (root) entities. These are the classes the are used
	 * for a repository. ViewModel classes should not be listed here.
	 * 
	 * @return all the classes used for this Model. These classes will be checked for compliance by the ModelTest .
	 */
	public Class<?>[] getEntityClasses();
}
