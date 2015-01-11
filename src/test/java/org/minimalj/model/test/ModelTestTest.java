package org.minimalj.model.test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.junit.Assert;
import org.junit.Test;
import org.minimalj.model.View;
import org.minimalj.model.annotation.Size;
import org.minimalj.model.annotation.Reference;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * This tests don't test models. It tests the tests for the model.
 * 
 */
public class ModelTestTest {

	@Test public void 
	should_test_not_accept_string_field_without_size() {
		ModelTest modelTest = new ModelTest(TestClass1.class);
		Assert.assertFalse(modelTest.isValid());
	}

	public static class TestClass1 {
		public Object id;

		public String a;
	}

	//
	
	@Test public void 
	should_test_accept_fields() {
		ModelTest modelTest = new ModelTest(TestClass2.class);
		assertValid(modelTest);
	}
	
	public static class TestClass2 {
		public Object id;
		
		@Size(255)
		public String a;
		public Integer b;
		public Long c;
		public LocalDate d;
		public LocalTime e;
		public BigDecimal f;
	}

	//

	@Test public void 
	should_test_not_accept_primitiv_int() {
		ModelTest modelTest = new ModelTest(TestClass3.class);
		Assert.assertFalse(modelTest.isValid());
	}

	public static class TestClass3 {
		public Object id;
		public int a;
	}

	//

	@Test public void 
	should_test_not_accept_primitiv_long() {
		ModelTest modelTest = new ModelTest(TestClass4.class);
		Assert.assertFalse(modelTest.isValid());
	}

	public static class TestClass4 {
		public Object id;
		public long a;
	}
	
	//

	@Test public void 
	should_test_not_accept_missing_id() {
		ModelTest modelTest = new ModelTest(TestClass5.class);
		Assert.assertFalse(modelTest.isValid());
	}

	public static class TestClass5 {
		public Integer a;
	}
	
	//
	
	@Test public void 
	should_test_not_accept_reference_to_other_entity() {
		ModelTest modelTest = new ModelTest(TestClass6.class, TestClass2.class);
		Assert.assertFalse(modelTest.isValid());
	}

	public static class TestClass6 {
		public Object id;
		public TestClass2 ref;
	}

	//
	
	@Test public void 
	should_test_accept_reference_to_other_entity_with_View_Annotation() {
		ModelTest modelTest = new ModelTest(TestClass7.class, TestClass2.class);
		assertValid(modelTest);
	}

	public static class TestClass7 {
		public Object id;
		@Reference
		public TestClass2 ref;
	}

	//

	@Test public void 
	should_test_accept_reference_to_View() {
		ModelTest modelTest = new ModelTest(TestClass8.class, TestClass9.class);
		assertValid(modelTest);
	}

	public static class TestClass8 implements View<TestClass2> {
		public Object id;
	}

	public static class TestClass9 {
		public Object id;
		@Reference
		public TestClass2 ref;
	}

	//
	
	@Test public void 
	should_test_not_accept_list_in_list() {
		ModelTest modelTest = new ModelTest(TestClass10.class);
		Assert.assertFalse(modelTest.isValid());
	}

	public static class TestClass10 {
		public Object id;
		public final List<TestClass11> list = new ArrayList<>();
	}

	public static class TestClass11 {
		public final List<TestClass12> list = new ArrayList<>();
	}

	public static class TestClass12 {
		public Integer a;
	}

	//

	@Test public void 
	should_test_not_accept_list_without_final() {
		ModelTest modelTest = new ModelTest(TestClass13.class);
		Assert.assertFalse(modelTest.isValid());
	}

	public static class TestClass13 {
		public Object id;
		public List<TestClass12> list = new ArrayList<>();
	}

	//

	@Test public void 
	should_test_not_accept_inheritence() {
		ModelTest modelTest = new ModelTest(TestClass14.class);
		Assert.assertFalse(modelTest.isValid());
	}

	public static class TestClass14 extends TestClass2 {
		public Integer i;
	}

	//

	@Test public void 
	should_test_not_accept_list_without_type() {
		ModelTest modelTest = new ModelTest(TestClass15.class);
		Assert.assertFalse(modelTest.isValid());
	}

	public static class TestClass15 {
		public Object id;
		@SuppressWarnings("rawtypes")
		public final List list = new ArrayList();
	}

	//

	@Test public void 
	should_test_not_accept_set_without_type() {
		ModelTest modelTest = new ModelTest(TestClass16.class);
		Assert.assertFalse(modelTest.isValid());
	}

	public static class TestClass16 {
		public Object id;
		@SuppressWarnings("rawtypes")
		public final Set s = new TreeSet();
	}

	//

	private void assertValid(ModelTest modelTest) {
		if (!modelTest.isValid()) {
			for (String s : modelTest.getProblems()) {
				System.out.println(s);
			}
		}
		Assert.assertTrue(modelTest.isValid());
	}
	
}
