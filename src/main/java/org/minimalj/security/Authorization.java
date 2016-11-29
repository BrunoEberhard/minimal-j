package org.minimalj.security;

import java.util.Collections;
import java.util.List;

import org.minimalj.model.annotation.Grant;
import org.minimalj.model.annotation.Grant.Privilege;
import org.minimalj.transaction.Role;
import org.minimalj.transaction.Transaction;

public class Authorization {

	public static boolean isAllowed(Transaction<?> transaction) {
		Role role = getRole(transaction);
		if (role != null) {
			List<String> currentRoles = getCurrentRoles();
			for (String allowingRole : role.value()) {
				if (currentRoles.contains(allowingRole)) {
					return true;
				}
			}
			return false;
		} else {
			// Transaction specifies no needed role. Every user passes
			return true;
		}
	}
	
	public static Role getRole(Transaction<?> transaction) {
		Role role = transaction.getClass().getAnnotation(Role.class);
		if (role != null) {
			return role;
		}
		role = transaction.getClass().getPackage().getAnnotation(Role.class);
		return role;
	}
	
	public static void checkGrants(Grant.Privilege privilege, Class<?> clazz) {
		List<String> currentRoles = getCurrentRoles();
		@SuppressWarnings("unused")
		boolean allowed = false;
		Grant[] grantsOnClass = clazz.getAnnotationsByType(Grant.class);
		allowed |= isGranted(currentRoles, privilege, clazz, grantsOnClass);
		Grant[] grantsOnPackage = clazz.getPackage().getAnnotationsByType(Grant.class);
		allowed |= isGranted(currentRoles, privilege, clazz, grantsOnPackage);
		allowed |= isGranted(currentRoles, Privilege.ALL, clazz, grantsOnClass);
		allowed |= isGranted(currentRoles, Privilege.ALL, clazz, grantsOnPackage);
	}
	
	protected static boolean isGranted(List<String> currentRoles, Grant.Privilege privilege, Class<?> clazz, Grant[] grants) {
		if (grants != null) {
			for (Grant grant : grants) {
				if (grant.privilege() == privilege) {
					for (String roleGranted : grant.value()) {
						if (currentRoles.contains(roleGranted)) {
							return true;
						}
					}
					throw new IllegalStateException(privilege + " not allowed on " + clazz.getSimpleName());
				}
			}
		}
		return false;
	}

	protected static List<String> getCurrentRoles() {
		Subject subject = Subject.getCurrent();
		return subject != null ? subject.getRoles() : Collections.emptyList();
	}
	
}