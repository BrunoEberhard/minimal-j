package org.minimalj.backend.db;

import java.sql.SQLException;
import java.util.List;

import junit.framework.Assert;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.minimalj.transaction.criteria.Criteria;

public class DbCriteriaTest {
	
	private static DbPersistence persistence;
	
	@BeforeClass
	public static void setupDb() throws SQLException {
		persistence = new DbPersistence(DbPersistence.embeddedDataSource(), A.class, G.class, H.class);
	}
	
	@AfterClass
	public static void shutdownDb() throws SQLException {
	}
	
	@Test // if fields of class reference are correctly written and read
	public void testReferenceField() throws SQLException {
		G g = new G("g1");
		Object id = persistence.insert(g);
		g = persistence.read(G.class, id);
		
		H h = new H();
		h.g = g;
		Object idH = persistence.insert(h);

		h = persistence.read(H.class, idH);
		
		Assert.assertEquals(id, h.g.id);
	}

	@Test // if read by a foreign key works correctly
	public void testForeignKeyCriteria() throws SQLException {
		G g = new G("g1");
		Object id = persistence.insert(g);
		g = persistence.read(G.class, id);
		
		H h = new H();
		h.g = g;
		persistence.insert(h);

		h = new H();
		h.g = g;
		persistence.insert(h);
		
		List<H> hList = persistence.getTable(H.class).read(new Criteria.SimpleCriteria(H.H_.g, g), 3);
		
		Assert.assertEquals(2, hList.size());
	}

	@Test // if read by a foreign key works correctly if the reference is in a dependable
	public void testForeignKeyCriteriaInDependable() throws SQLException {
		G g = new G("g1");
		Object id = persistence.insert(g);
		g = persistence.read(G.class, id);
		
		H h = new H();
		h.i.rG = g;
		persistence.insert(h);

		h = new H();
		h.i.rG = g;
		persistence.insert(h);
		
		List<H> hList = persistence.getTable(H.class).read(new Criteria.SimpleCriteria(H.H_.i.rG, g), 3);
		
		Assert.assertEquals(2, hList.size());
	}

}
