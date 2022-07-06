package org.minimalj.model.test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.junit.Assert;
import org.junit.Test;
import org.minimalj.model.View;
import org.minimalj.model.annotation.Materialized;
import org.minimalj.model.annotation.Size;

/**
 * This tests don't test models. It tests the tests for the model.
 * 
 */
public class ModelTestTest {

	@Test public void 
	should_not_accept_string_field_without_size() {
		ModelTest modelTest = new ModelTest(TestClass1.class);
		Assert.assertFalse(modelTest.isValid());
	}

	public static class TestClass1 {
		public Object id;

		public String a;
	}

	@Test public void 
	should_not_accept_materialized_string_method_without_size() {
		ModelTest modelTest = new ModelTest(TestClass1b.class);
		Assert.assertFalse(modelTest.isValid());
	}

	public static class TestClass1b {
		public Object id;

		@Materialized
		public String getA() {
			return "";
		}
	}

	@Test public void 
	should_accept_not_materialized_string_method_without_size() {
		ModelTest modelTest = new ModelTest(TestClass1c.class);
		Assert.assertTrue(modelTest.isValid());
	}

	public static class TestClass1c {
		public Object id;

		public String getA() {
			return "";
		}
	}

	//
	
	@Test public void 
	should_accept_fields() {
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
	should_not_accept_primitiv_int() {
		ModelTest modelTest = new ModelTest(TestClass3.class);
		Assert.assertFalse(modelTest.isValid());
	}

	public static class TestClass3 {
		public Object id;
		public int a;
	}

	//

	@Test public void 
	should_not_accept_primitiv_long() {
		ModelTest modelTest = new ModelTest(TestClass4.class);
		Assert.assertFalse(modelTest.isValid());
	}

	public static class TestClass4 {
		public Object id;
		public long a;
	}
	
	//

	@Test public void 
	should_not_accept_missing_id() {
		ModelTest modelTest = new ModelTest(TestClass5.class);
		Assert.assertFalse(modelTest.isValid());
	}

	public static class TestClass5 {
		public Integer a;
	}
	
	//
	
	@Test public void 
	should_accept_reference_to_other_entity() {
		ModelTest modelTest = new ModelTest(TestClass6.class, TestClass2.class);
		Assert.assertTrue(modelTest.isValid());
	}

	public static class TestClass6 {
		public Object id;
		public TestClass2 ref;
	}

	//

	@Test public void 
	should_accept_reference_to_View() {
		ModelTest modelTest = new ModelTest(TestClass8.class, TestClass9.class);
		assertValid(modelTest);
	}

	public static class TestClass8 implements View<TestClass2> {
		public Object id;
	}

	public static class TestClass9 {
		public Object id;
		public TestClass2 ref;
	}

	//
	
	@Test public void 
	should_not_accept_eager_list_in_eager_list() {
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
	should_not_accept_eager_list_in_lazy_list() {
		ModelTest modelTest = new ModelTest(TestClass10b.class);
		Assert.assertFalse(modelTest.isValid());
	}

	public static class TestClass10b {
		public Object id;
		public List<TestClass11> list = new ArrayList<>();
	}

	//
	
	@Test public void 
	should_not_accept_lazy_list_in_eager_list() {
		ModelTest modelTest = new ModelTest(TestClass10c.class);
		Assert.assertFalse(modelTest.isValid());
	}

	public static class TestClass10c {
		public Object id;
		public final List<TestClass11c> list = new ArrayList<>();
	}
	
	public static class TestClass11c {
		public List<TestClass12> list = new ArrayList<>();
	}
	
	//

	@Test public void 
	should_accept_list_without_final() {
		ModelTest modelTest = new ModelTest(TestClass13.class);
		Assert.assertTrue(modelTest.isValid());
	}

	public static class TestClass13 {
		public Object id;
		public List<TestClass12> list = new ArrayList<>();
	}

	//

	@Test public void 
	should_not_accept_inheritence_from_non_abstract_superclass() {
		ModelTest modelTest = new ModelTest(TestClass14.class);
		Assert.assertFalse(modelTest.isValid());
	}

	public static class TestClass14 extends TestClass2 {
		public Integer i;
	}
	
	//

	@Test public void 
	should_accept_inheritence_from_abstract_superclass() {
		ModelTest modelTest = new ModelTest(TestClass19.class);
		Assert.assertTrue(modelTest.isValid());
	}

	public static abstract class TestClass18 {
		public Integer id;
	}

	public static class TestClass19 extends TestClass18 {
		public Integer a;
	}

	//

	@Test public void 
	should_not_accept_fields_of_abstract_class() {
		ModelTest modelTest = new ModelTest(TestClass22.class);
		Assert.assertFalse(modelTest.isValid());
	}

	public static class TestClass22 {
		public Integer id;
		public TestClass18 fieldOfAbstractClass;
	}
	
	//

	@Test public void 
	should_not_accept_list_without_type() {
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
	should_not_accept_set_without_type() {
		ModelTest modelTest = new ModelTest(TestClass16.class);
		Assert.assertFalse(modelTest.isValid());
	}

	public static class TestClass16 {
		public Object id;
		@SuppressWarnings("rawtypes")
		public final Set s = new TreeSet();
	}
	
	//
	
	@Test public void
	should_not_accept_invalid_field_names() {
		ModelTest modelTest = new ModelTest(TestClass17.class);
		Assert.assertFalse(modelTest.isValid());
	}
	
	public static class TestClass17 {
		public Object id;
		public String a$;
	}

	@Test public void
	should_not_accept_invalid_class_names() {
		ModelTest modelTest = new ModelTest(TestClass18$.class);
		Assert.assertFalse(modelTest.isValid());
	}
	
	public static class TestClass18$ {
		public Object id;
		public Integer a;
	}
	
	@Test public void
	should_not_accept_self_mixin() {
		ModelTest modelTest = new ModelTest(TestClass19b.class);
		Assert.assertFalse(modelTest.isValid());
	}
	
	public static class TestClass19a {
		public Object id;
		public final TestClass19b mixin = new TestClass19b();
	}
	
	public static class TestClass19b {
		public final TestClass19b selfMixin = new TestClass19b();
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
	
	@Test public void 
	should_accept_Temporal_without_size() {
		ModelTest modelTest = new ModelTest(TestClass20.class);
		Assert.assertTrue(modelTest.isValid());
	}

	public static class TestClass20 {
		public Object id;

		public LocalDate a;
		public LocalTime b;
		public LocalDateTime c;
	}

	@Test public void 
	should_accept_Temporal_with_size() {
		ModelTest modelTest = new ModelTest(TestClass21.class);
		Assert.assertTrue(modelTest.isValid());
	}

	public static class TestClass21 {
		public Object id;

		@Size(Size.TIME_HH_MM)
		public LocalTime t1;
		@Size(Size.TIME_WITH_SECONDS)
		public LocalTime t2;
		@Size(Size.TIME_WITH_MILLIS)
		public LocalTime t3;

		@Size(Size.TIME_HH_MM)
		public LocalDateTime dt1;
		@Size(Size.TIME_WITH_SECONDS)
		public LocalDateTime dt2;
		@Size(Size.TIME_WITH_MILLIS)
		public LocalDateTime dt3;
	}

	@Test public void 
	should_not_accept_Temporal_with_unsupported_size() {
		ModelTest modelTest = new ModelTest(TestClass22a.class);
		Assert.assertFalse(modelTest.isValid());
		
		modelTest = new ModelTest(TestClass22b.class);
		Assert.assertFalse(modelTest.isValid());
	}

	public static class TestClass22a {
		public Object id;

		@Size(Size.TIME_HH_MM + 1)
		public LocalTime t1;
	}

	public static class TestClass22b {
		public Object id;

		@Size(Size.TIME_HH_MM + 1)
		public LocalDateTime t1;
	}
	
	//

	@Test public void 
	should_not_accept_direct_self_reference() {
		ModelTest modelTest = new ModelTest(TestClass23.class);
		Assert.assertFalse(modelTest.isValid());
	}

	public static class TestClass23 {
		public Object id;
		public TestClass23 selfReference;
	}
	
	@Test public void 
	should_not_accept_indirect_self_reference() {
		ModelTest modelTest = new ModelTest(TestClass24a.class);
		Assert.assertFalse(modelTest.isValid());
	}

	public static class TestClass24a {
		public Object id;
		public TestClass24b reference;
	}
	
	public static class TestClass24b {
		public Object id;
		public TestClass24a reference;
	}

	@Test public void 
	should_accept_indirect_self_reference() {
		ModelTest modelTest = new ModelTest(TestClass25a.class);
		Assert.assertTrue(modelTest.isValid());
	}

	public static class TestClass25a {
		public Object id;
		public List<TestClass25b> referenceList;
	}
	
	public static class TestClass25b {
		public Object id;
		public TestClass25a reference;
	}
	

	@Test public void 
	should_accept_indirect_self_reference_through_view() {
		ModelTest modelTest = new ModelTest(TestClass26.class);
		Assert.assertTrue(modelTest.isValid());
	}

	public static class TestClass26 {
		public Object id;
		public TestView26 v;
	}
	
	public static class TestView26 implements View<TestClass26> {
		public Object id;
		// view must not contain field v
	}

	@Test public void 
	should_not_accept_self_reference_through_view() {
		ModelTest modelTest = new ModelTest(TestClass27.class);
		Assert.assertFalse(modelTest.isValid());
	}

	public static class TestClass27 {
		public Object id;
		public TestView27 v;
	}
	
	public static class TestView27 implements View<TestClass27> {
		public Object id;
		public TestView27 v;
	}

	@Test public void 
	should_accept_two_fields_of_same_class() {
		ModelTest modelTest = new ModelTest(TestClass28a.class);
		Assert.assertTrue(modelTest.isValid());
	}

	public static class TestClass28a {
		public Object id;
		public TestClass28b a, b;
	}

	public static class TestClass28b {
		public Object id;
	}
}
