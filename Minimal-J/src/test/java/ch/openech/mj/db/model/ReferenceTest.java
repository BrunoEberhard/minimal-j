package ch.openech.mj.db.model;

import org.junit.Assert;
import org.junit.Test;

import ch.openech.mj.model.Keys;
import ch.openech.mj.model.PropertyInterface;
import ch.openech.mj.model.Reference;
import ch.openech.mj.model.annotation.Size;

public class ReferenceTest {

	@Test
	public void testAccessToReferencedEntity() {
		TestClassWithReference testClassWithReference = new TestClassWithReference();
		testClassWithReference.testClassA.set(TestClassA.TEST_CLASS_A.a, "Hello");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testInvalidAccess() {
		TestClassWithReference testClassWithReference = new TestClassWithReference();
		testClassWithReference.testClassA.set("b", "Hello");
	}

	@Test
	public void testAccessThroughProperty() {
		TestClassWithReference testClassWithReference = new TestClassWithReference();
		PropertyInterface property = Keys.getProperty(TestClassWithReference.TESTCLASSWITHREFERENCE_CLASS_WITH_REFERENCE.testClassA.get(TestClassA.TEST_CLASS_A.a));
		property.setValue(testClassWithReference, "setValue");
		Reference<TestClassA> reference = testClassWithReference.testClassA;
		Assert.assertEquals("setValue", reference.get(TestClassA.TEST_CLASS_A.a));
		
		reference.set(TestClassA.TEST_CLASS_A.a, "setValue2");
		String actualString = (String) property.getValue(testClassWithReference);
		Assert.assertEquals("setValue2", actualString);
	}

	@Test
	public void testOfProperty() {
		PropertyInterface property = Keys.getProperty(TestClassWithReference.TESTCLASSWITHREFERENCE_CLASS_WITH_REFERENCE.testClassA.get(TestClassA.TEST_CLASS_A.a));
		Assert.assertEquals(String.class, property.getFieldClazz());
		Assert.assertEquals(100, property.getAnnotation(Size.class).value());
	}

	public static class TestClassA {
		public static final TestClassA TEST_CLASS_A = Keys.of(TestClassA.class);
		@Size(100)
		public String a;
	}
	
	public static class TestClassWithReference {
		public static final TestClassWithReference TESTCLASSWITHREFERENCE_CLASS_WITH_REFERENCE = Keys.of(TestClassWithReference.class);
		public final Reference<TestClassA> testClassA = new Reference<>(TestClassA.TEST_CLASS_A.a);
	}
}
