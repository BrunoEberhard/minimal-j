package org.minimalj.util.resources;

import java.util.ResourceBundle;

import org.junit.Assert;
import org.junit.Test;
import org.minimalj.model.Keys;
import org.minimalj.model.View;
import org.minimalj.model.properties.Property;
import org.minimalj.util.resources.Resources.ResourceBundleAccess;

public class ResourcesTest {

	public static final ResourcesTest $ = Keys.of(ResourcesTest.class);
	
	private ResourceBundleAccess resourceAccess = new ResourceBundleAccess(ResourceBundle.getBundle(ResourcesTest.class.getName()));
	
	public ResourcesTest1 fullQualified;
	
	public ResourcesTest2 normal;
	public ResourcesTest2 normalReplaced;
	public ResourcesTest2 normalReplacedInMiddle;
	
	public ResourcesTest3 byFieldClass;
	public ResourcesTest4 byFullQualifiedFieldClass;

	public ResourcesTest5 chained;

	public String byFieldName;

	@Test
	public void should_find_name_of_full_qualified_field() throws Exception {
		Assert.assertEquals("Test1", getFieldName($.fullQualified));
	}
	
	@Test
	public void should_find_name_of_normal_field() throws Exception {
		Assert.assertEquals("Test2", getFieldName($.normal));
		Assert.assertEquals("Test2", getFieldName($.normalReplaced));
		Assert.assertEquals("A Test2 B", getFieldName($.normalReplacedInMiddle));
	}
	
	@Test
	public void should_find_name_by_field_class() throws Exception {
		Assert.assertEquals("Test3", getFieldName($.byFieldClass));
	}
	
	@Test
	public void should_find_name_by_full_qualified_field_class() throws Exception {
		Assert.assertEquals("Test4", getFieldName($.byFullQualifiedFieldClass));
	}
	
	@Test
	public void should_find_name_by_field_name() throws Exception {
		Assert.assertEquals("Test5", getFieldName($.byFieldName));
	}

	@Test
	public void should_resolve_chained_field() throws Exception {
		Assert.assertEquals("Chained", getFieldName($.chained.field));
	}

	@Test
	public void should_resolve_double_chained_field() throws Exception {
		Assert.assertNotEquals("Full path should win", "Test_wrong_text", getFieldName($.chained.field2.field));
		Assert.assertEquals("Chained2", getFieldName($.chained.field2.field));
	}

	@Test
	public void should_resolve_double_chained_field3() throws Exception {
		Assert.assertEquals("Chained3", getFieldName($.chained.field3.field));
	}

	// same in view
	
	@Test
	public void should_find_name_of_full_qualified_field_in_view() throws Exception {
		Assert.assertEquals("Test1", getFieldName(ResourcesTestView.$.fullQualified));
	}
	
	@Test
	public void should_find_name_of_normal_field_in_view() throws Exception {
		Assert.assertEquals("Test2", getFieldName(ResourcesTestView.$.normal));
	}
	
	@Test
	public void should_find_name_by_field_class_in_view() throws Exception {
		Assert.assertEquals("Test3", getFieldName(ResourcesTestView.$.byFieldClass));
	}
	
	@Test
	public void should_find_name_by_full_qualified_field_class_in_view() throws Exception {
		Assert.assertEquals("Test4", getFieldName(ResourcesTestView.$.byFullQualifiedFieldClass));
	}
	
	private String getFieldName(Object key) throws Exception {
		Property property = Keys.getProperty(key);
		return resourceAccess.getPropertyName(property, null);
	}
	
	public static class ResourcesTest1 {
		
	}
	
	public static class ResourcesTest2 {
		
	}
	
	public static class ResourcesTest3 {
		
	}
	
	public static class ResourcesTest4 {
		
	}

	public static class ResourcesTest5 {
		public String field;

		public ResourcesTest6 field2;
		public ResourcesTest7 field3;
	}

	public static class ResourcesTest6 {
		public String field;
	}

	public static class ResourcesTest7 {
		public String field;
	}

	public static class ResourcesTestView implements View<ResourcesTest> {
		public static final ResourcesTestView $ = Keys.of(ResourcesTestView.class);
		
		public ResourcesTest1 fullQualified;
		public ResourcesTest2 normal;
		
		public ResourcesTest3 byFieldClass;
		public ResourcesTest4 byFullQualifiedFieldClass;
	}
}
