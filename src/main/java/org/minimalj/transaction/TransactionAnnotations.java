package org.minimalj.transaction;

import java.lang.annotation.Annotation;

import org.minimalj.backend.repository.EntityTransaction;

public class TransactionAnnotations {

	/**
	 * @param transaction Annotated transaction
	 * @return the used isolation (for example 'serializable') for transaction
	 */	
	public static Isolation.Level getIsolation(Transaction<?> transaction) {
		Isolation isolation = getAnnotation(transaction, Isolation.class);
		return isolation != null ? isolation.value() : Isolation.Level.SERIALIZABLE;
	}

	/**
	 * @param transaction Annotated transaction
	 * @return the roles that allow to execute the transaction
	 */
	public static String[] getRoles(Transaction<?> transaction) {
		Role role = getAnnotation(transaction, Role.class);
		return role != null ? role.value() : null;
	}

	private static <A extends Annotation> A getAnnotation(Transaction<?> transaction, Class<A> annotationClass) {
		A annotation = getAnnotationOfClassOrPackage(transaction.getClass(), annotationClass);
		if (annotation != null) {
			return annotation;
		}
		if (transaction instanceof EntityTransaction) {
			EntityTransaction<?, ?> entityTransaction = (EntityTransaction<?, ?>) transaction;
			Class<?> entityClazz = entityTransaction.getEntityClazz();
			annotation = getAnnotationOfClassOrPackage(entityClazz, annotationClass);
		}
		return annotation;
	}

	/**
	 * @param clazz inspected class
	 * @param annotationClass the annotation to get
	 * @param <A> type
	 * @return the annotation on the class itself or if not available the
	 *         annotation for the (direct) package of the class
	 */
	public static <A extends Annotation> A getAnnotationOfClassOrPackage(Class<?> clazz, Class<A> annotationClass) {
		A annotation = clazz.getAnnotation(annotationClass);
		if (annotation != null) {
			return annotation;
		} else {
			return clazz.getPackage().getAnnotation(annotationClass);
		}
	}
}
