package ch.openech.mj.db;

import junit.framework.Assert;

import org.junit.Test;

import ch.openech.mj.model.Keys;
import ch.openech.mj.model.PropertyInterface;
import ch.openech.test.db.A;
import ch.openech.test.db.AView;
import ch.openech.test.db.J;


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
