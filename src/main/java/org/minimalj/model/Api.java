package org.minimalj.model;

import java.util.List;

import org.minimalj.model.annotation.Comment;
import org.minimalj.transaction.Transaction;

/**
 * An application can implement this interface to explicitly define its
 * Transactions. This can be useful for external interfaces. For internal
 * frontend/backend communication this is not needed.
 * 
 */
public interface Api extends Model {

	public static class TransactionDefinition {
		public final Class<? extends Transaction<?>> clazz;
		public final Class<?> request;
		public final Class<?> response;
		public final boolean listResponse;
		public final String comment;

		/*
		 * Could maybe all derived from the transaction clazz. But very complicated
		 * reflection stuff.
		 */
		public TransactionDefinition(Class<? extends Transaction<?>> clazz, Class<?> request, Class<?> response, boolean listResponse) {
			super();
			this.clazz = clazz;
			this.request = request;
			this.response = response;
			this.listResponse = listResponse;
			Comment commentAnnotation = clazz.getAnnotation(Comment.class);
			this.comment = commentAnnotation != null ? commentAnnotation.value() : null;
		}
	};

	public List<TransactionDefinition> getTransactions();

	public default boolean canCreate(Class<?> clazz) {
		return true;
	}

	public default boolean canUpdate(Class<?> clazz) {
		return true;
	}

	public default boolean canDelete(Class<?> clazz) {
		return true;
	}
}
