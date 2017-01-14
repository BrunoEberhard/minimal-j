package org.minimalj.security;

import java.util.Collections;
import java.util.List;

import org.minimalj.transaction.Role;
import org.minimalj.transaction.Transaction;

/**
 * The 
 *
 */
public class Authorization {

	public final void check(Transaction<?> transaction) {
		if (!isAllowed(transaction)) {
			throw new IllegalStateException(transaction + " forbidden");
		}
	}
	
	public boolean isAllowed(Transaction<?> transaction) {
		return isAllowed(getCurrentRoles(), transaction);
	}
	
	public static boolean isAllowed(List<String> currentRoles, Transaction<?> transaction) {
		Role role = transaction.getRole();
		if (role != null) {
			for (String allowingRole : role.value()) {
				if (currentRoles.contains(allowingRole)) {
					return true;
				}
			}
			return false;
		}
		return true;
	}
	
	protected static List<String> getCurrentRoles() {
		Subject subject = Subject.getCurrent();
		return subject != null ? subject.getRoles() : Collections.emptyList();
	}
	
}