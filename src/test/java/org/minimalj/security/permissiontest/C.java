package org.minimalj.security.permissiontest;

import org.minimalj.transaction.Role;
import org.minimalj.transaction.persistence.UpdateTransaction;

@Role(value = "ReadRole")
@Role(transaction = UpdateTransaction.class, value = "UpdateRole")
public class C {

	public C() {
	}

}
