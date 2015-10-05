package org.minimalj.security.permissiontest.pkgrole;

import org.minimalj.transaction.Role;
import org.minimalj.transaction.Transaction.TransactionType;

@Role(transaction = TransactionType.UPDATE, value = {"UpdateClassRole"})
public class H {

	public H() {
	}

}
