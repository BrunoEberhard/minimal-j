package org.minimalj.model.test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.minimalj.model.annotation.Size;
import org.minimalj.model.annotation.View;
import org.minimalj.model.annotation.ViewOf;
import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalTime;

/**
 * This tests don't test models. It tests the tests for the model.
 * 
 */
public class ModelTestTest {

	@Test public void 
	should_string_field_without_size_not_allowed() {
		ModelTest modelTest = new ModelTest(TestClass1.class);
		Assert.assertFalse(modelTest.isValid());
	}

	public static class TestClass1 {
		public int id;

		public String a;
	}

	//
	
	@Test public void 
	should_test_accept_fields() {
		ModelTest modelTest = new ModelTest(TestClass2.class);
		assertValid(modelTest);
	}
	
	public static class TestClass2 {
		public int id;
		
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
		public int id;
		public int a;
	}

	//

	@Test public void 
	should_test_not_accept_primitiv_long() {
		ModelTest modelTest = new ModelTest(TestClass4.class);
		Assert.assertFalse(modelTest.isValid());
	}

	public static class TestClass4 {
		public int id;
		public long a;
	}
	
	//

	@Test public void 
	should_test_check_for_id() {
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
		public int id;
		public TestClass2 ref;
	}

	//
	
	@Test public void 
	should_test_accept_reference_to_other_entity_with_View_Annotation() {
		ModelTest modelTest = new ModelTest(TestClass7.class, TestClass2.class);
		assertValid(modelTest);
	}

	public static class TestClass7 {
		public int id;
		@View
		public TestClass2 ref;
	}

	//

	@Test public void 
	should_test_accept_reference_to_View() {
		ModelTest modelTest = new ModelTest(TestClass8.class, TestClass9.class);
		assertValid(modelTest);
	}

	public static class TestClass8 implements ViewOf<TestClass2> {
		public int id;

		@Override
		public String display() {
			return null;
		}
	}

	public static class TestClass9 {
		public int id;
		@View
		public TestClass2 ref;
	}

	//
	
	@Test public void 
	should_test_not_accept_list_in_list() {
		ModelTest modelTest = new ModelTest(TestClass10.class);
		Assert.assertFalse(modelTest.isValid());
	}

	public static class TestClass10 {
		public int id;
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
		public int id;
		public List<TestClass12> list = new ArrayList<>();
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
