package org.minimalj.backend.db;

import junit.framework.Assert;

import org.junit.Test;
import org.minimalj.model.Keys;
import org.minimalj.model.PropertyInterface;
import org.minimalj.model.ViewUtil;


public class ViewUtilTest {

	@Test
	public void testGetViewedClassForViewOf() {
		PropertyInterface property = Keys.getProperty(J.J_.aView);
		Assert.assertEquals(A.class, ViewUtil.getViewedClass(property));
	}
	
	public void testGetViewedClassOfView() {
		Assert.assertEquals(A.class, ViewUtil.getViewedClass(AView.class));
	}
}
