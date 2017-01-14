package org.minimalj.transaction;

import java.lang.annotation.Annotation;

import org.minimalj.backend.repository.EntityTransaction;

public class TransactionUtil {

	/**
	 * 
	 * @return the used isolation (for example 'serializable') for transaction
	 */	
	public static Isolation getIsolation(Transaction<?> transaction) {
		return getAnnotation(transaction, Isolation.class);
	}

	/**
	 * 
	 * @return the role needed to execute the transaction
	 */
	public static Role getRole(Transaction<?> transaction) {
		return getAnnotation(transaction, Role.class);
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
