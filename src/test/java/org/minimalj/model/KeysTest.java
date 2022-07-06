package org.minimalj.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.minimalj.model.properties.PropertyInterface;

public class KeysTest {
	
	@Test
	public void accessString() {
		TestClass1 object1 = new TestClass1();
		
		object1.s1 = "Test";
		PropertyInterface property = Keys.getProperty(TestClass1.$.s1);
		Assert.assertEquals("Get should return the value of the public field", object1.s1, property.getValue(object1));
	}

	@Test
	public void accessSubString() {
		TestClass2 testObject2 = new TestClass2();

		testObject2.testClass1.s1 = "TestS1";
		PropertyInterface property = Keys.getProperty(TestClass2.$.testClass1.s1);
		Assert.assertEquals("Get should return the value of referenced Objects", testObject2.testClass1.s1, property.getValue(testObject2));
	}

	@Test
	public void accessViaGetter() {
		TestClass2 testObject2 = new TestClass2();

		testObject2.setS3("access5");
		PropertyInterface property = Keys.getProperty(TestClass2.$.getS3());
		Assert.assertEquals("If private, get should use the getter method", testObject2.getS3(), property.getValue(testObject2));
		
		property.setValue(testObject2, "access5a");
		Assert.assertEquals("If private, get should use the setter method to change value", testObject2.getS3(), property.getValue(testObject2));
	}
	
	@Test
	public void accessViaIs() {
		TestClass2 testObject2 = new TestClass2();

		testObject2.setB1(true);
		PropertyInterface property = Keys.getProperty(TestClass2.$.getB1());
		Assert.assertEquals("For private boolean, get should use the isXy method", testObject2.getB1(), property.getValue(testObject2));
		
		property.setValue(testObject2, Boolean.FALSE);
		Assert.assertEquals("For private boolean, set should use the setXy method", testObject2.getB1(), property.getValue(testObject2));
	}
	
	@Test
	public void accessChildViaIs() {
		TestClass2 testObject2 = new TestClass2();
		
		testObject2.testClass1.b2 = true;
		testObject2.testClass1b.b2 = false;
		
		PropertyInterface property = Keys.getProperty(TestClass2.$.testClass1.getB2());
		Assert.assertEquals("For private boolean, get should use the isXy method, even for related objects", testObject2.testClass1.getB2(), property.getValue(testObject2));

		PropertyInterface propertyB = Keys.getProperty(TestClass2.$.getTestClass1b().getB2());
		Assert.assertEquals("For private boolean, get should use the isXy method, even for related objects", testObject2.getTestClass1b().getB2(), propertyB.getValue(testObject2));
		testObject2.testClass1b.b2 = true;
		Assert.assertEquals("For private boolean, get should use the isXy method, even for related objects", testObject2.getTestClass1b().getB2(), propertyB.getValue(testObject2));
		
		testObject2.testClass1.b2 = false; 
		Assert.assertEquals("For private boolean, get should use the isXy method, even for related objects", testObject2.testClass1.getB2(), property.getValue(testObject2));
	}

	@Test
	public void updateList() {
		TestClass3 testClass3 = new TestClass3();
		List<TestClass1> list = testClass3.list;
		list.add(new TestClass1());
		Assert.assertEquals(1, testClass3.list.size());
		
		PropertyInterface property = Keys.getProperty(TestClass3.$.list);
		property.setValue(testClass3, list);
		
		Assert.assertEquals("After set a final list field with its existing values the content must be the same", 1, testClass3.list.size());
		
		List<TestClass1> list2 = new ArrayList<>();
		list2.add(new TestClass1());
		list2.add(new TestClass1());
		property.setValue(testClass3, list2);
		Assert.assertEquals("Update of final list field with new values failed", list2.size(), testClass3.list.size());
	}
	
	@Test
	public void methodFieldOfInlineInTwoClasses() {
		TestClass2 testClass2 = new TestClass2();
		testClass2.tc1.setB2(true);
		TestClass4 testClass4 = new TestClass4();
		testClass4.testClass1.setB2(false);
		
		String message = "Method property should return correct value even if it is contained in two inner classes";
		Assert.assertEquals(message,  Boolean.TRUE, Keys.getProperty(TestClass2.$.tc1.getB2()).getValue(testClass2));
		Assert.assertEquals(message,  Boolean.FALSE, Keys.getProperty(TestClass4.$.testClass1.getB2()).getValue(testClass4));
	}
	
	@Test
	public void fieldsOfGetterReturnType() {
		Assert.assertNotNull(TestClass2.$.getTestClass1b().s1);

		String message = "Chained properties should have correct path even if they contain a method property";
		Assert.assertEquals(message, "testClass1b", Keys.getProperty(TestClass2.$.getTestClass1b()).getPath());
		Assert.assertEquals(message, "testClass1b.s1", Keys.getProperty(TestClass2.$.getTestClass1b().s1).getPath());
		Assert.assertEquals(message, "testClass1b.testClass3.list", Keys.getProperty(TestClass2.$.getTestClass1b().getTestClass3().list).getPath());
	}

	@Test
	public void methodPropertyGetterName() {
		try {
			// initialize keys
			TestClass5.$.getA();
			Assert.fail();
		} catch (Exception x) {
			Assert.assertTrue(x.getMessage().startsWith("methodOf must be called with the property name"));
		}
	}

	@Test
	public void methodPropertyWrongName() {
		try {
			// initialize keys
			TestClass6.$.getA();
			Assert.fail();
		} catch (Exception x) {
			Assert.assertTrue(x.getMessage().startsWith("methodOf called with invalid property name"));
		}
	}
	
	@Test(expected = IndexOutOfBoundsException.class)
	public void listElements() {
		TestClass7 testClass7 = new TestClass7();
		testClass7.list = new ArrayList<>();
		TestClass8 testClass8 = new TestClass8();
		testClass8.value = 42;
		testClass7.list.add(testClass8);
		
		// $.list.get(x) is not supported right now. There was a version of Keys which
		// implemented it but there are some problems: final lists (can be setAccessible),
		// resource-names, adhoc generation of missing elements, implementation in SQL queries.
		// last but not least, the Keys class gets very complicated.
		PropertyInterface p = Keys.getProperty(TestClass7.$.list.get(0).value);
		Assert.assertEquals(42, p.getValue(testClass7));
		
		p = Keys.getProperty(TestClass7.$.list.get(1).value);
		p.setValue(testClass7, 43);
		
		Assert.assertEquals(Integer.valueOf(43), testClass7.list.get(1).value);
	}
	
	@Test
	public void testMethodPropertyDependencies() {
		PropertyInterface propertyC = Keys.getProperty(TestClass9.$.testClass10.getC());
		List<PropertyInterface> dependencies = Keys.getDependencies(propertyC);
		
		TestClass9 testClass9 = new TestClass9();
		testClass9.testClass10 = new TestClass10();
	
		dependencies.get(0).setValue(testClass9, BigDecimal.valueOf(1));
		dependencies.get(1).setValue(testClass9, BigDecimal.valueOf(2));
		
		Assert.assertEquals(BigDecimal.valueOf(3), testClass9.testClass10.getC());
		
		TestClass10 testClass10 = new TestClass10();
		testClass10.a = BigDecimal.valueOf(3);
		testClass10.b = BigDecimal.valueOf(4);
	}
	
	@Test
	public void testGetterUsedFromTwoChainedProperties() {
		// this chained properties with a getter method at end failed till 1.0.27.0
		TestClass11a testA = new TestClass11a();
		testA.t.t2.setS3("Test");

		TestClass11b testB = new TestClass11b();
		testB.t.t2.setS3("Test2");

		PropertyInterface pA = Keys.getProperty(TestClass11a.$.t.t2.getS3());
		PropertyInterface pB = Keys.getProperty(TestClass11b.$.t.t2.getS3());

		Assert.assertEquals("Test", pA.getValue(testA));
		Assert.assertEquals("Test2", pB.getValue(testB));
	}
	
	@Test
	public void testExtensionClasses() {
		TestClass12b testObject = new TestClass12b();

		testObject.a = 1;
		testObject.b = 2;
		
		PropertyInterface pA = Keys.getProperty(TestClass12b.$.a);
		PropertyInterface pB = Keys.getProperty(TestClass12b.$.b);

		Assert.assertEquals(1, pA.getValue(testObject));
		Assert.assertEquals(2, pB.getValue(testObject));
	}
	
	//
	
	public static class TestClass1 {
		public static final TestClass1 $ = Keys.of(TestClass1.class);
		
		public String s1;
		private Boolean b2;
		
		public Boolean getB2() {
			if (Keys.isKeyObject(this)) return Keys.methodOf(this, "b2");
			
			return b2;
		}
		
		public void setB2(Boolean b2) {
			this.b2 = b2;
		}
		
		public TestClass3 getTestClass3() {
			if (Keys.isKeyObject(this)) return Keys.methodOf(this, "testClass3");
			return null;
		}
	}

	public static class TestClass2 {
		public static final TestClass2 $ = Keys.of(TestClass2.class);
		
		public String s2;
		private String s3;
		private Boolean b1;
		public final TestClass1 testClass1 = new TestClass1();
		private final TestClass1 testClass1b = new TestClass1();
		public final TestClass1 tc1 = new TestClass1();
		
		public String getS3() {
			if (Keys.isKeyObject(this)) return Keys.methodOf(this, "s3");

			return s3;
		}
		
		public void setS3(String s3) {
			this.s3 = s3;
		}
		
		public Boolean getB1() {
			if (Keys.isKeyObject(this)) return Keys.methodOf(this, "b1");
			
			return b1;
		}
		
		public void setB1(Boolean b1) {
			this.b1 = b1;
		}

		public TestClass1 getTestClass1b() {
			if (Keys.isKeyObject(this)) return Keys.methodOf(this, "testClass1b");
			return testClass1b;
		}
		
	}

	public static class TestClass3 {
		public static final TestClass3 $ = Keys.of(TestClass3.class);
		
		public final List<TestClass1> list = new ArrayList<>();
	}
	
	public static class TestClass4 {
		public static final TestClass4 $ = Keys.of(TestClass4.class);
		
		public final TestClass1 testClass1 = new TestClass1();
	}

	public static class TestClass5 {
		public static final TestClass5 $ = Keys.of(TestClass5.class);
		
		public String getA() {
			if (Keys.isKeyObject(this)) {
				return Keys.methodOf(this, "getA");
			}
			
			return "unused string";
		}
	}
	
	public static class TestClass6 {
		public static final TestClass6 $ = Keys.of(TestClass6.class);
		
		public String getA() {
			if (Keys.isKeyObject(this)) {
				return Keys.methodOf(this, "b");
			}
			
			return "unused string";
		}
	}

	public static class TestClass7 {
		public static final TestClass7 $ = Keys.of(TestClass7.class);
		
		public List<TestClass8> list;
	}

	public static class TestClass8 {
		public static final TestClass8 $ = Keys.of(TestClass8.class);
		
		public Integer value;
	}
	
	public static class TestClass9 {

		public static final TestClass9 $ = Keys.of(TestClass9.class);
		
		public TestClass10 testClass10;
	}

	public static class TestClass10 {

		public static final TestClass10 $ = Keys.of(TestClass10.class);
		
		public BigDecimal a, b;
		
		public BigDecimal getC() {
			if (Keys.isKeyObject(this)) return Keys.methodOf(this, "c", $.a, $.b);
			
			return a != null && b != null ? a.add(b) : null;
		}
	}

	public static class TestClass11 {

		public final TestClass2 t2 = new TestClass2();
	}
	
	public static class TestClass11a {

		public static final TestClass11a $ = Keys.of(TestClass11a.class);

		public TestClass11 t = new TestClass11();
	}

	public static class TestClass11b {

		public static final TestClass11b $ = Keys.of(TestClass11b.class);

		public TestClass11 t = new TestClass11();
	}
	
	public static abstract class TestClass12 {
		
		public Integer a;
	}
	
	public static class TestClass12b extends TestClass12 {
		
		public static final TestClass12b $ = Keys.of(TestClass12b.class);
		
		public Integer b;

	}


}
