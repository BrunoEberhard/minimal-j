package org.minimalj.backend.db;

import junit.framework.Assert;

import org.junit.Test;
import org.minimalj.backend.db.DbPersistenceHelper;
import org.minimalj.model.Keys;
import org.minimalj.model.PropertyInterface;


public class DbPersistenceHelperTest {

	@Test
	public void testGetViewedClassForViewOf() {
		PropertyInterface property = Keys.getProperty(J.J_.aView);
		Assert.assertEquals(A.class, DbPersistenceHelper.getViewedClass(property));
	}
	
	public void testGetViewedClassOfView() {
		Assert.assertEquals(A.class, DbPersistenceHelper.getViewedClass(AView.class));
	}
}
