@Role("pkgRole")
@Role(transaction = ReadTransaction.class, value = "ReadPkgRole")
@Role(transaction = UpdateTransaction.class, value = "UpdatePkgRole")
package org.minimalj.security.permissiontest.pkgrole;

import org.minimalj.transaction.Role;
import org.minimalj.transaction.persistence.ReadTransaction;
import org.minimalj.transaction.persistence.UpdateTransaction;
