package org.minimalj.backend.db;

import junit.framework.Assert;

import org.junit.Test;
import org.minimalj.model.Keys;
import org.minimalj.model.ViewUtil;
import org.minimalj.model.properties.PropertyInterface;


public class ViewUtilTest {

	@Test
	public void testGetViewedClassForViewOf() {
		PropertyInterface property = Keys.getProperty(J.$.aView);
		Assert.assertEquals(A.class, ViewUtil.getReferencedClass(property));
	}
	
	public void testGetViewedClassOfView() {
		Assert.assertEquals(A.class, ViewUtil.getViewedClass(AView.class));
	}
}
