package org.minimalj.backend;

import org.junit.Assert;
import org.junit.Test;
import org.minimalj.backend.repository.DeleteEntityTransaction;
import org.minimalj.backend.repository.EntityTransaction;
import org.minimalj.backend.repository.WriteTransaction;

public class BackendTest {

	@Test
	public void testExpectedTransactionHierarchie() {
		Assert.assertTrue("Expected Hierarchie in method handleCodeCache", EntityTransaction.class.isAssignableFrom(WriteTransaction.class));
		Assert.assertTrue("Expected Hierarchie in method handleCodeCache", EntityTransaction.class.isAssignableFrom(DeleteEntityTransaction.class));
	}
}
