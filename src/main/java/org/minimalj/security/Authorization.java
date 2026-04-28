package org.minimalj.security;

import org.minimalj.model.annotation.AnnotationUtil;
import org.minimalj.transaction.Role;

public class Authorization {

	/**
	 * @param subject the current Subject
	 * @param object  the object to be checked for access. A transaction or a page.
	 * @return true if the subject has one of the annotated roles or if there is no
	 *         annotation.
	 */
	public static boolean hasAccess(Subject subject, Object object) {
		if (object instanceof AccessControl) {
			return ((AccessControl) object).hasAccess(subject);
		}
		return true;
	}

	/**
	 * @param subject the current Subject
	 * @param clazz   the class that should be accessed
	 * @return true if the subject has one of the annotated roles, false if not,
	 *         null if not roles are annotated
	 */
	public static Boolean hasAccessByAnnotation(Subject subject, Class<?> clazz) {
		Role role = AnnotationUtil.getAnnotationOfClassOrPackage(clazz, Role.class);
		if (role != null) {
			String[] roles = role.value();
			if (roles == null || roles.length == 0) {
				throw new IllegalArgumentException("No Roles specified in annotation of " + clazz.getSimpleName());
			}
			if (subject != null) {
				return subject.hasRole(roles);
			} else {
				return false;
			}
		}
		return null;
	}
}