package org.minimalj.repository.sql;

import org.minimalj.application.Application;
import org.minimalj.backend.Backend;
import org.minimalj.model.Keys;
import org.minimalj.model.annotation.Size;
import org.minimalj.repository.query.By;
import org.minimalj.transaction.Isolation;
import org.minimalj.transaction.Isolation.Level;
import org.minimalj.transaction.Transaction;

public class SqlRepositoryTest extends Application {

	@Override
	public Class<?>[] getEntityClasses() {
		return new Class<?>[] {A.class, M.class};
	}

	@Override
	public void initBackend() {
		new Thread() {
			@Override
			public void run() {
				boolean succeed = true;
				int count = 0;
				while (true) {
					long before = Backend.count(A.class, By.all());
					try {
						Backend.execute(new TestTransactionIsolationNone(succeed));
					} catch (Exception e) {
						//
					}
					long after = Backend.count(A.class, By.all());
					if (after != before + 1) {
						System.out.println("NONE ---------------------- " + after + " != " + (before + 1));
					}
					try {
						Thread.sleep(10);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					succeed = !succeed;
					count++;
					if (count % 100 == 0) {
						System.out.println("None: " + count);
					}
				}
			}
		}.start();
		
		new Thread() {
			@Override
			public void run() {
				boolean succeed = true;
				int count = 0;
				while (true) {
					long before = Backend.count(M.class, By.all());
					try {
						Backend.execute(new TestTransactionIsolationSerialiazable(succeed));
					} catch (Exception e) {
						//
					}
					long after = Backend.count(M.class, By.all());
					if (succeed) {
						if (after != before + 1) {
							System.out.println("SER SUCC ---------------------- " + after + " != " + (before + 1));
						}
					} else {
						if (after != before) {
							System.out.println("SER ROLLBACK---------------------- " + after + " != " + before);
						}
					}
					try {
						Thread.sleep(11);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					succeed = !succeed;
					count++;
					if (count % 100 == 0) {
						System.out.println("Serializable: " + count);
					}
				}
			}
		}.start();
	}
	
	
	public static void main(String[] args) {
		Application.setInstance(new SqlRepositoryTest());
		Backend.getInstance();

		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}

//	@Override
//	public List<Action> getNavigation() {
//		Backend.getInstance();
////		System.out.println(Backend.count(A.class, By.ALL));
//		return new ArrayList<>();
//	}
	
	@Isolation(Level.NONE)
	public static class TestTransactionIsolationNone implements Transaction<Void> {
		private static final long serialVersionUID = 1L;

		public final boolean succeed;
		
		public TestTransactionIsolationNone(boolean succeed) {
			this.succeed = succeed;
		}
		
		@Override
		public Void execute() {
			Object a = createEntity();
			repository().insert(a);
			try {
				Thread.sleep((long) (Math.random() * 10));
			} catch (InterruptedException e) {
				//
			}
			if (!succeed) {
				throw new RuntimeException();
			}
			return null;
		}

		protected Object createEntity() {
			return new A("Test " + System.nanoTime());
		}
	}
	
	@Isolation(Level.SERIALIZABLE)
	public static class TestTransactionIsolationSerialiazable extends TestTransactionIsolationNone {
		private static final long serialVersionUID = 1L;
		
		public TestTransactionIsolationSerialiazable(boolean succeed) {
			super(succeed);
		}
		
		@Override
		protected Object createEntity() {
			return new M();
		}
	}

	public static class A {

		public static final A $ = Keys.of(A.class);
		
		public A() {
			// needed for reflection constructor
		}
		
		public A(String aName) {
			this.aName = aName;
		}
		
		public Object id;
		public int version;

		@Size(30)
		public String aName;
	}
	
	public static class M {
		public static final M $ = Keys.of(M.class);
		
		public Object id;
		
		public byte[] bytes;
		
	}


}
