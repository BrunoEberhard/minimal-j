package org.minimalj.security;

import java.util.List;

import org.minimalj.model.annotation.AnnotationUtil;
import org.minimalj.transaction.Role;

public class Authorization {

	public static boolean hasAccess(Subject subject, Object object) {
		if (object instanceof AccessControl) {
			return ((AccessControl) object).hasAccess(subject);
		}
		return true;
	}

	public static Boolean hasAccessByAnnotation(Subject subject, Class<?> clazz) {
		Role role = AnnotationUtil.getAnnotationOfClassOrPackage(clazz, Role.class);
		if (role != null) {
			if (subject != null) {
				return hasAccess(subject.getRoles(), role.value());
			} else {
				return false;
			}
		}
		return null;
	}

	private static boolean hasAccess(List<String> currentRoles, String[] roles) {
		if (roles != null) {
			for (String allowingRole : roles) {
				if (currentRoles.contains(allowingRole)) {
					return true;
				}
			}
			return false;
		}
		return true;
	}

}