package org.minimalj.security.permissiontest.pkgrole;

import org.minimalj.transaction.Role;
import org.minimalj.transaction.persistence.UpdateTransaction;

@Role(transaction = UpdateTransaction.class, value = "UpdateClassRole")
public class H {

	public H() {
	}

}
