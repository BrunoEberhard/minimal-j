package org.minimalj.security.permissiontest;

import org.minimalj.transaction.Role;
import org.minimalj.transaction.Transaction.TransactionType;

@Role(value = "ReadRole")
@Role(transaction = TransactionType.UPDATE, value = {"UpdateRole"})
public class C {

	public C() {
	}

}
