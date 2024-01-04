package org.minimalj.util;

import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

public class GenericUtilsTest {

	@Test
	public void listOfString() throws NoSuchFieldException, SecurityException {
		Assert.assertEquals(String.class, GenericUtils.getGenericClass(GenericUtilsTestClassA.class.getField("listString")));
		Assert.assertEquals(String.class, GenericUtils.getGenericClass(GenericUtilsTestClassA.class, GenericUtilsTestClassA.class.getField("listString")));
	}

	@Test
	public void mapOfStringInteger() throws NoSuchFieldException, SecurityException {
		List<Object> classes = GenericUtils.getGenericClasses(GenericUtilsTestClassA.class, GenericUtilsTestClassA.class.getField("mapStringInteger"));
		Assert.assertEquals(2, classes.size());
		Assert.assertEquals(String.class, classes.get(0));
		Assert.assertEquals(Integer.class, classes.get(1));
	}

	@Test
	public void mapOfMapStringMap() throws NoSuchFieldException, SecurityException {
		List<Object> classes = GenericUtils.getGenericClasses(GenericUtilsTestClassA.class, GenericUtilsTestClassA.class.getField("mapStringMap"));
		Assert.assertEquals(2, classes.size());
		Assert.assertEquals(String.class, classes.get(0));
		Assert.assertTrue(classes.get(1) instanceof List);
		List<?> secondType = (List<?>) classes.get(1);
		Assert.assertEquals(Integer.class, secondType.get(0));
		Assert.assertEquals(Long.class, secondType.get(1));
	}

	@Test
	@Ignore // not yet working
	public void interfaceType() throws NoSuchFieldException, SecurityException {
		Assert.assertEquals(GenericUtilsTestClassA.class, GenericUtils.getTypeArgument(GenericUtilsTestClassB.class, List.class));
		Assert.assertEquals(GenericUtilsTestClassA.class, GenericUtils.getTypeArgument(GenericUtilsTestClassD.class, List.class));
	}

	public static class GenericUtilsTestClassA {

		public List<String> listString;
		public Map<String, Integer> mapStringInteger;
		public Map<String, Map<Integer, Long>> mapStringMap;
	}

	public static abstract class GenericUtilsTestClassB implements List<GenericUtilsTestClassA> {
	}
	
	public static abstract class GenericUtilsTestClassC<T> implements List<T> {
	}

	public static abstract class GenericUtilsTestClassD extends GenericUtilsTestClassC<GenericUtilsTestClassA> {
	}

}
