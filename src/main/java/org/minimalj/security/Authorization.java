package org.minimalj.security;

import java.util.Collections;
import java.util.List;

import org.minimalj.application.DevMode;
import org.minimalj.backend.Backend;
import org.minimalj.transaction.Role;

public class Authorization {

	public static void checkAuthorization(Class<?> clazz) {
		boolean skipAuthorization = DevMode.isActive() && !Backend.getInstance().isAuthenticationActive();
		
		if (!skipAuthorization) {
			List<String> currentRoles = getCurrentRoles();
			checkAuthorization(currentRoles, clazz);
		}
	}

	public static void checkAuthorization(List<String> currentRoles, Class<?> clazz) {
		Role role = getRole(clazz);
		if (role != null) {
			for (String allowingRole : role.value()) {
				if (currentRoles.contains(allowingRole)) {
					return;
				}
			}
			throw new IllegalStateException(clazz.getSimpleName() + " forbidden");
		}
	}
	
	public static Role getRole(Class<?> clazz) {
		Role role = clazz.getAnnotation(Role.class);
		if (role != null) {
			return role;
		}
		role = clazz.getPackage().getAnnotation(Role.class);
		return role;
	}
	
	protected static List<String> getCurrentRoles() {
		Subject subject = Subject.getCurrent();
		return subject != null ? subject.getRoles() : Collections.emptyList();
	}
	
}