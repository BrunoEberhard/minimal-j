package org.minimalj.repository.ignite;

import java.util.List;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.minimalj.repository.ignite.IgniteRepository;
import org.minimalj.repository.query.By;

public class IgniteDbReadTest {
	
	private static IgniteRepository repository;
	
	@Before
	public void setupRepository() {
		repository = new IgniteRepository(A.class, G.class, H.class);
	}
	
	@AfterClass
	public static void shutdownRepository() {
	}
	
	@Test // if fields of class reference are correctly written and read
	public void testReferenceField() {
		G g = new G("g1");
		Object id = repository.insert(g);
		g = repository.read(G.class, id);
		
		H h = new H();
		h.g = g;
		Object idH = repository.insert(h);

		h = repository.read(H.class, idH);
		
		Assert.assertEquals(id, h.g.id);
	}

	@Test // if read by a reference works correctly
	public void testSimpleField() {
		G g = new G("g2");
		Object id = repository.insert(g);
		g = repository.read(G.class, id);
		
		H h = new H();
		h.g = g;
		repository.insert(h);

		h = new H();
		h.g = g;
		repository.insert(h);
		
		List<H> hList = repository.find(H.class, By.field(H.$.g, g).limit(3));
		Assert.assertEquals("Read by reference", 2, hList.size());

		hList = repository.find(H.class, By.field(H.$.g.id, g.id).limit(3));
		Assert.assertEquals("Read by references id", 2, hList.size());

		hList = repository.find(H.class, By.field(H.$.g.g, "g2").limit(3));
		Assert.assertEquals("Read by references field", 2, hList.size());
	}

	@Test // if read by a foreign key works correctly if the reference is in a dependable
	public void testForeignKeyFieldInDependable() {
		G g = new G("g3");
		Object id = repository.insert(g);
		g = repository.read(G.class, id);
		
		H h = new H();
		h.i.rG = g;
		repository.insert(h);

		h = new H();
		h.i.rG = g;
		repository.insert(h);
		
		List<H> hList = repository.find(H.class, By.field(H.$.i.rG, g).limit(3));
		
		Assert.assertEquals(2, hList.size());
	}

}
