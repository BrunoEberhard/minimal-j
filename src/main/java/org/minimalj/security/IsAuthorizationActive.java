package org.minimalj.security;

import org.minimalj.transaction.Transaction;

/**
 * Returns false if no authorization is configured on Backend.
 * Then no Login Dialog should be displayed and the Backend will
 * not check the needed roles for a transaction.
 *
 */
public class IsAuthorizationActive implements Transaction<Boolean> {
	private static final long serialVersionUID = 1L;

	@Override
	public Boolean execute() {
		return Authorization.isActive();
	}

}
