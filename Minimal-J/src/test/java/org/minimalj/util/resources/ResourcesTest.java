package org.minimalj.util.resources;

import java.util.ResourceBundle;

import org.junit.Assert;
import org.junit.Test;
import org.minimalj.model.Keys;
import org.minimalj.model.View;
import org.minimalj.model.properties.PropertyInterface;

public class ResourcesTest {

	public static final ResourcesTest $ = Keys.of(ResourcesTest.class);
	
	public ResourcesTest1 fullQualified;
	public ResourcesTest2 normal;
	
	public ResourcesTest3 byFieldClass;
	public ResourcesTest4 byFullQualifiedFieldClass;

	public String byFieldName;

	static {
		Resources.addResourceBundle(ResourceBundle.getBundle(ResourcesTest.class.getName()));
	}
	
	@Test
	public void should_find_name_of_full_qualified_field() throws Exception {
		Assert.assertEquals("Test1", getFieldName($.fullQualified));
	}
	
	@Test
	public void should_find_name_of_normal_field() throws Exception {
		Assert.assertEquals("Test2", getFieldName($.normal));
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
		PropertyInterface property = Keys.getProperty(key);
		String name = Resources.getObjectFieldName(Resources.getResourceBundle(), property);
		return name;
	}
	
	public static class ResourcesTest1 {
		
	}
	
	public static class ResourcesTest2 {
		
	}
	
	public static class ResourcesTest3 {
		
	}
	
	public static class ResourcesTest4 {
		
	}
	
	public static class ResourcesTestView implements View<ResourcesTest> {
		public static final ResourcesTestView $ = Keys.of(ResourcesTestView.class);
		
		public ResourcesTest1 fullQualified;
		public ResourcesTest2 normal;
		
		public ResourcesTest3 byFieldClass;
		public ResourcesTest4 byFullQualifiedFieldClass;
	}
}
