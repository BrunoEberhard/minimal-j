package org.minimalj.repository.sql;

import org.junit.Assert;
import org.junit.Test;

public class SqlReadWithStringIdTest extends SqlTest {
	
	@Override
	public Class<?>[] getEntityClasses() {
		return new Class<?>[] { G.class };
	};
	
	@Test
	public void testInsertAndDelete() {
		G g = new G("testName1");
		Object id = repository.insert(g);
		
		G g2 = repository.read(G.class, id.toString());
		Assert.assertNotNull(g2);
	}

}
