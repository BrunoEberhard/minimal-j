package ch.openech.mj.edit.value;

import junit.framework.Assert;

import org.junit.Test;

import ch.openech.mj.db.model.Constants;
import ch.openech.mj.db.model.PropertyInterface;

public class PropertyAccessTest {

	@Test
	public void accessString() {
		TestClass1 object1 = new TestClass1();
		
		object1.s1 = "Test";
		PropertyInterface property = Constants.getProperty(TestClass1.KEYS.s1);
		Assert.assertEquals("Get should return the value of the public field", object1.s1, property.getValue(object1));
	}

	@Test
	public void accessSubString() {
		TestClass2 testObject2 = new TestClass2();

		testObject2.testClass1.s1 = "TestS1";
		PropertyInterface property = Constants.getProperty(TestClass2.KEYS.testClass1.s1);
		Assert.assertEquals("Get should return the value of referenced Objects", testObject2.testClass1.s1, property.getValue(testObject2));
	}

	@Test
	public void accessViaGetter() {
		TestClass2 testObject2 = new TestClass2();

		testObject2.setS3("access5");
		PropertyInterface property = Constants.getProperty(TestClass2.KEYS.getS3());
		Assert.assertEquals("If private, get should use the getter method", testObject2.getS3(), property.getValue(testObject2));
		
		property.setValue(testObject2, "access5a");
		Assert.assertEquals("If private, get should use the setter method to change value", testObject2.getS3(), property.getValue(testObject2));
	}
	
	@Test
	public void accessViaIs() {
		TestClass2 testObject2 = new TestClass2();

		testObject2.setB1(true);
		PropertyInterface property = Constants.getProperty(TestClass2.KEYS.getB1());
		Assert.assertEquals("For private boolean, get should use the isXy method", testObject2.getB1(), property.getValue(testObject2));
		
		property.setValue(testObject2, Boolean.FALSE);
		Assert.assertEquals("For private boolean, set should use the setXy method", testObject2.getB1(), property.getValue(testObject2));
	}
	
	@Test
	public void accessChildViaIs() {
		TestClass2 testObject2 = new TestClass2();
		
		testObject2.testClass1.b2 = true;
		testObject2.testClass1b.b2 = false;
		
		PropertyInterface property = Constants.getProperty(TestClass2.KEYS.testClass1.getB2());
		Assert.assertEquals("For private boolean, get should use the isXy method, even for related objects", testObject2.testClass1.getB2(), property.getValue(testObject2));

		PropertyInterface propertyB = Constants.getProperty(TestClass2.KEYS.getTestClass1b().getB2());
		Assert.assertEquals("For private boolean, get should use the isXy method, even for related objects", testObject2.getTestClass1b().getB2(), propertyB.getValue(testObject2));
		testObject2.testClass1b.b2 = true;
		Assert.assertEquals("For private boolean, get should use the isXy method, even for related objects", testObject2.getTestClass1b().getB2(), propertyB.getValue(testObject2));
		
		testObject2.testClass1.b2 = false; 
		Assert.assertEquals("For private boolean, get should use the isXy method, even for related objects", testObject2.testClass1.getB2(), property.getValue(testObject2));
	}
	
	public static class TestClass1 {
		public static final TestClass1 KEYS = Constants.of(TestClass1.class);
		
		public String s1;
		private Boolean b2;
		
		public Boolean getB2() {
			if (Constants.isKeyObject(this)) return Constants.methodOf(this, "b2", Boolean.class);
			
			return b2;
		}
		
		public void setB2(Boolean b2) {
			this.b2 = b2;
		}
	}

	public static class TestClass2 {
		public static final TestClass2 KEYS = Constants.of(TestClass2.class);
		
		public String s2;
		private String s3;
		private Boolean b1;
		public final TestClass1 testClass1 = new TestClass1();
		private final TestClass1 testClass1b = new TestClass1();
		public final TestClass1 tc1 = new TestClass1();
		
		public String getS3() {
			if (Constants.isKeyObject(this)) return Constants.methodOf(this, "s3", String.class);

			return s3;
		}
		
		public void setS3(String s3) {
			this.s3 = s3;
		}
		
		public Boolean getB1() {
			if (Constants.isKeyObject(this)) return Constants.methodOf(this, "b1", Boolean.class);
			
			return b1;
		}
		
		public void setB1(Boolean b1) {
			this.b1 = b1;
		}

		public TestClass1 getTestClass1b() {
			if (Constants.isKeyObject(this)) return Constants.methodOf(this, "testClass1b", TestClass1.class);
			return testClass1b;
		}
		
	}
	
}
