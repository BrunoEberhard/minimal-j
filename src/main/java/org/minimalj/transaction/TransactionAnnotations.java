package org.minimalj.transaction;

import java.lang.annotation.Annotation;

import org.minimalj.backend.repository.EntityTransaction;

public class TransactionAnnotations {

	/**
	 * 
	 * @return the used isolation (for example 'serializable') for transaction
	 */	
	public static Isolation.Level getIsolation(Transaction<?> transaction) {
		Isolation isolation = getAnnotation(transaction, Isolation.class);
		return isolation != null ? isolation.value() : Isolation.Level.SERIALIZABLE;
	}

	/**
	 * 
	 * @return the roles that allow to execute the transaction
	 */
	public static String[] getRoles(Transaction<?> transaction) {
		Role role = getAnnotation(transaction, Role.class);
		return role != null ? role.value() : null;
	}
	
	private static <A extends Annotation> A getAnnotation(Transaction<?> transaction, Class<A> annotationClass) {
		A annotation;
		annotation = transaction.getClass().getAnnotation(annotationClass);
		if (annotation != null) {
			return annotation;
		}
		annotation = transaction.getClass().getPackage().getAnnotation(annotationClass);
		if (annotation != null) {
			return annotation;
		}
		if (transaction instanceof EntityTransaction) {
			EntityTransaction<?, ?> entityTransaction = (EntityTransaction<?, ?>) transaction;
			annotation = entityTransaction.getEntityClazz().getAnnotation(annotationClass);
			if (annotation != null) {
				return annotation;
			}
			annotation = entityTransaction.getEntityClazz().getPackage().getAnnotation(annotationClass);
			if (annotation != null) {
				return annotation;
			}
		}
		return null;
	}
	
}
