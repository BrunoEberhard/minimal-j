package org.minimalj.util.resources;

import java.util.ResourceBundle;

import org.junit.Assert;
import org.junit.Test;
import org.minimalj.model.Keys;
import org.minimalj.model.PropertyInterface;
import org.minimalj.model.annotation.ViewOf;

public class ResourcesTest {

	public static final ResourcesTest _ = Keys.of(ResourcesTest.class);
	
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
		Assert.assertEquals("Test1", getFieldName(_.fullQualified));
	}
	
	@Test
	public void should_find_name_of_normal_field() throws Exception {
		Assert.assertEquals("Test2", getFieldName(_.normal));
	}
	
	@Test
	public void should_find_name_by_field_class() throws Exception {
		Assert.assertEquals("Test3", getFieldName(_.byFieldClass));
	}
	
	@Test
	public void should_find_name_by_full_qualified_field_class() throws Exception {
		Assert.assertEquals("Test4", getFieldName(_.byFullQualifiedFieldClass));
	}
	
	@Test
	public void should_find_name_by_field_name() throws Exception {
		Assert.assertEquals("Test5", getFieldName(_.byFieldName));
	}
	
	// same in view
	
	@Test
	public void should_find_name_of_full_qualified_field_in_view() throws Exception {
		Assert.assertEquals("Test1", getFieldName(ResourcesTestView._.fullQualified));
	}
	
	@Test
	public void should_find_name_of_normal_field_in_view() throws Exception {
		Assert.assertEquals("Test2", getFieldName(ResourcesTestView._.normal));
	}
	
	@Test
	public void should_find_name_by_field_class_in_view() throws Exception {
		Assert.assertEquals("Test3", getFieldName(ResourcesTestView._.byFieldClass));
	}
	
	@Test
	public void should_find_name_by_full_qualified_field_class_in_view() throws Exception {
		Assert.assertEquals("Test4", getFieldName(ResourcesTestView._.byFullQualifiedFieldClass));
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
	
	public static class ResourcesTestView implements ViewOf<ResourcesTest> {
		public static final ResourcesTestView _ = Keys.of(ResourcesTestView.class);
		
		public ResourcesTest1 fullQualified;
		public ResourcesTest2 normal;
		
		public ResourcesTest3 byFieldClass;
		public ResourcesTest4 byFullQualifiedFieldClass;
		
		@Override
		public String display() {
			return null;
		}
		
	}
}
