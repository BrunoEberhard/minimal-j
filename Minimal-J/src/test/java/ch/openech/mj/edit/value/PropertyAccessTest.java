package ch.openech.mj.edit.value;

import junit.framework.Assert;

import org.junit.Test;

public class PropertyAccessTest {

	@Test
	public void accessString() {
		TestClass1 object1 = new TestClass1();
		
		object1.s1 = "Test";
		Assert.assertEquals("Get should return the value of the public field", object1.s1, PropertyAccessor.get(object1, "s1"));
	}

	@Test
	public void accessSubString() {
		TestClass2 testObject2 = new TestClass2();

		testObject2.testClass1.s1 = "TestS1";
		Assert.assertEquals("Get should return the value of referenced Objects", testObject2.testClass1.s1, PropertyAccessor.get(testObject2, "testClass1.s1"));
	}

	@Test
	public void accessViaGetter() {
		TestClass2 testObject2 = new TestClass2();

		testObject2.setS3("access5");
		Assert.assertEquals("If private, get should use the getter method", testObject2.getS3(), PropertyAccessor.get(testObject2, "s3"));
		
		PropertyAccessor.set(testObject2, "s3", "access5a");
		Assert.assertEquals("If private, get should use the setter method to change value", testObject2.getS3(), PropertyAccessor.get(testObject2, "s3"));
	}
	
	@Test
	public void accessViaIs() {
		TestClass2 testObject2 = new TestClass2();

		testObject2.setB1(true);
		Assert.assertEquals("For private boolean, get should use the isXy method", testObject2.isB1(), PropertyAccessor.get(testObject2, "b1"));
		
		PropertyAccessor.set(testObject2, "b1", Boolean.FALSE);
		Assert.assertEquals("For private boolean, set should use the setXy method", testObject2.isB1(), PropertyAccessor.get(testObject2, "b1"));
	}
	
	@Test
	public void accessChildViaIs() {
		TestClass2 testObject2 = new TestClass2();
		
		testObject2.testClass1.b2 = true;
		Assert.assertEquals("For private boolean, get should use the isXy method, even for related objects", testObject2.testClass1.isB2(), PropertyAccessor.get(testObject2, "testClass1.b2"));

		testObject2.testClass1.b2 = false; 
		Assert.assertEquals("For private boolean, get should use the isXy method, even for related objects", testObject2.testClass1.isB2(), PropertyAccessor.get(testObject2, "testClass1.b2"));
	}
	
	public static class TestClass1 {
		public String s1;
		private boolean b2;
		
		public boolean isB2() {
			return b2;
		}
	}

	public static class TestClass2 {
		public String s2;
		private String s3;
		private boolean b1;
		public final TestClass1 testClass1 = new TestClass1();
		public final TestClass1 tc1 = new TestClass1();
		
		public String getS3() {
			return s3;
		}
		
		public void setS3(String s3) {
			this.s3 = s3;
		}
		
		public boolean isB1() {
			return b1;
		}
		
		public void setB1(boolean b1) {
			this.b1 = b1;
		}
	}
	
}
