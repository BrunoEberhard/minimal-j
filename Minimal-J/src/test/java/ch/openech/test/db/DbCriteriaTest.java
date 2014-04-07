package ch.openech.test.db;

import java.sql.SQLException;
import java.util.List;

import junit.framework.Assert;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import ch.openech.mj.criteria.Criteria;
import ch.openech.mj.db.DbPersistence;

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
		int gid = (int) persistence.insert(g);
		g = persistence.read(G.class, gid);
		
		H h = new H();
		h.g = g;
		long idH = persistence.insert(h);

		h = persistence.read(H.class, idH);
		
		Assert.assertEquals(gid, h.g.id);
	}

	@Test // if read by a foreign key works correctly
	public void testForeignKeyCriteria() throws SQLException {
		G g = new G("g1");
		int gid = (int) persistence.insert(g);
		g = persistence.read(G.class, gid);
		
		H h = new H();
		h.g = g;
		persistence.insert(h);

		h = new H();
		h.g = g;
		persistence.insert(h);
		
		List<H> hList = persistence.getTable(H.class).read(new Criteria.SimpleCriteria(H.H_.g, g));
		
		Assert.assertEquals(2, hList.size());
	}

	@Test // if read by a foreign key works correctly if the reference is in a immutalbe
	public void testForeignKeyCriteriaInImmutable() throws SQLException {
		G g = new G("g1");
		int gid = (int) persistence.insert(g);
		g = persistence.read(G.class, gid);
		
		H h = new H();
		h.i.rG = g;
		persistence.insert(h);

		h = new H();
		h.i.rG = g;
		persistence.insert(h);
		
		List<H> hList = persistence.getTable(H.class).read(new Criteria.SimpleCriteria(H.H_.i.rG, g));
		
		Assert.assertEquals(2, hList.size());
	}

}
