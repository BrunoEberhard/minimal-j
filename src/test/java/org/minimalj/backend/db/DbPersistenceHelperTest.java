package org.minimalj.backend.db;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import junit.framework.Assert;

public class DbPersistenceHelperTest {

	@Test
	public void testReservedDbWords() {
		Assert.assertEquals("WHERE_", DbPersistenceHelper.buildName("where", 128, Collections.emptySet()));
		Assert.assertEquals("WHER_", DbPersistenceHelper.buildName("where", 5, Collections.emptySet()));
	}

	@Test
	public void testNameConflicts() {
		Set<String> alreadyUsedNames = new HashSet<>();
		alreadyUsedNames.add("NAME");
		
		Assert.assertEquals("NA_1", DbPersistenceHelper.buildName("name", 4, alreadyUsedNames));
		alreadyUsedNames.add("NA_1");
		
		// add additional 8 'name'
		for (int i = 2; i<=10; i++) {
			alreadyUsedNames.add(DbPersistenceHelper.buildName("name", 4, alreadyUsedNames));
		}

		// the eleventh needs one place at the end more
		Assert.assertEquals("N_11", DbPersistenceHelper.buildName("name", 4, alreadyUsedNames));
	}

}
