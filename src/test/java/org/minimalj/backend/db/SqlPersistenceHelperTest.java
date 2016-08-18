package org.minimalj.backend.db;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.minimalj.persistence.sql.SqlHelper;

public class SqlPersistenceHelperTest {

	@Test
	public void testReservedSqlWords() {
		Assert.assertEquals("WHERE_", SqlHelper.buildName("where", 128, Collections.emptySet()));
		Assert.assertEquals("WHER_", SqlHelper.buildName("where", 5, Collections.emptySet()));
	}

	@Test
	public void testNameConflicts() {
		Set<String> alreadyUsedNames = new HashSet<>();
		alreadyUsedNames.add("NAME");
		
		Assert.assertEquals("NA_1", SqlHelper.buildName("name", 4, alreadyUsedNames));
		alreadyUsedNames.add("NA_1");
		
		// add additional 8 'name'
		for (int i = 2; i<=10; i++) {
			alreadyUsedNames.add(SqlHelper.buildName("name", 4, alreadyUsedNames));
		}

		// the eleventh needs one place at the end more
		Assert.assertEquals("N_11", SqlHelper.buildName("name", 4, alreadyUsedNames));
	}

}
