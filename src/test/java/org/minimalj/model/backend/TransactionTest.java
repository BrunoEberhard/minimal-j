package org.minimalj.model.backend;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.minimalj.TestApplication;
import org.minimalj.application.Application;
import org.minimalj.backend.Backend;
import org.minimalj.repository.query.By;
import org.minimalj.transaction.Transaction;

public class TransactionTest {

	@BeforeClass
	public static void initApplication() {
		Application.setInstance(TestApplication.INSTANCE);
	}

	public class TransactionTestApplication extends Application {
		@Override
		public Class<?>[] getEntityClasses() {
			return new Class<?>[] { TestEntityA.class };
		}
	}

	@Before
	public void init() {
		TestApplication.INSTANCE.setCurrentApplication(new TransactionTestApplication());

		Backend.insert(new TestEntityA());
		Assert.assertEquals("Initialy there should be 1 entity", 1, Backend.count(TestEntityA.class, By.ALL));
	}

	@Test
	public void testSameTransaction() {
		try {
			Backend.execute(new TestTransaction());
			Assert.fail("The exception should be passed to the caller of execute");
		} catch (Exception x) {
			Assert.assertEquals("Class of exception should stay same", RuntimeException.class, x.getClass());
			Assert.assertEquals("Message of exception should stay same", "No wonder happend", x.getMessage());
		}

		Assert.assertEquals("Because of the exception the insert should be rollback", 1, Backend.count(TestEntityA.class, By.ALL));
	}

	@Test
	public void testSeparateTransaction() {
		try {
			Backend.execute(new TestTransactionInsertingInSeparateTransaction());
			Assert.fail("The exception should be passed to the caller of execute");
		} catch (Exception x) {
			Assert.assertEquals("Class of exception should stay same", RuntimeException.class, x.getClass());
			Assert.assertEquals("Message of exception should stay same", "No wonder happend", x.getMessage());
		}

		Assert.assertEquals("Because of the separate transaction the insert should be committed", 2, Backend.count(TestEntityA.class, By.ALL));
	}

	public static class TestTransaction implements Transaction<TestEntityA> {
		private static final long serialVersionUID = 1L;

		@Override
		public TestEntityA execute() {
			// insert in repository without separate transaction
			Backend.getInstance().getRepository().insert(new TestEntityA());
			if (Math.random() < 2) {
				throw new RuntimeException("No wonder happend");
			}
			return null;
		}
	}

	public static class TestTransactionInsertingInSeparateTransaction implements Transaction<TestEntityA> {
		private static final long serialVersionUID = 1L;

		@Override
		public TestEntityA execute() {
			// Backend.insert creates a new Transaction
			Backend.insert(new TestEntityA());
			if (Math.random() < 2) {
				throw new RuntimeException("No wonder happend");
			}
			return null;
		}
	}

	public static class TestEntityA {
		public Object id;
		public Integer value;
	}
}
