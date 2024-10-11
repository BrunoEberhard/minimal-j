package org.minimalj.model;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

public class ModelTest {

	@Test
	public void testDepthFirst() {
		List<Class<?>> classes = Model.getClassesRecursive(new Class[] {TestClass1.class}, true, true);
		Assert.assertEquals(3, classes.size());
		Assert.assertEquals(TestClass3.class, classes.get(0));
		Assert.assertEquals(TestClass2.class, classes.get(1));
		Assert.assertEquals(TestClass1.class, classes.get(2));
	}
	
	public static class TestClass1 {
		public Object id;
		public TestClass2 testClass2;
	}
	
	public static class TestClass2 {
		public Object id;
		public TestClass3 testClass3;
		public TestClass4 testClass4;
	}

	public static class TestClass3 {
		public Object id;
	}

	public static class TestClass4 {
		
	}
}