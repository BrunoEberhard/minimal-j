@Role("pkgRole")
@Role(transaction = TransactionType.READ, value = {"ReadPkgRole"})
@Role(transaction = TransactionType.UPDATE, value = {"UpdatePkgRole"})
package org.minimalj.security.permissiontest.pkgrole;

import org.minimalj.transaction.Role;
import org.minimalj.transaction.Transaction.TransactionType;
