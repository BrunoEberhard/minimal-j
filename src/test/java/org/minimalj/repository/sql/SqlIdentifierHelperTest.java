package org.minimalj.repository.sql;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.minimalj.repository.sql.SqlIdentifier;

public class SqlIdentifierHelperTest {

	@Test
	public void testReservedSqlWords() {
		Assert.assertEquals("WHERE_", SqlIdentifier.buildIdentifier("where", 128, Collections.emptySet()));
		Assert.assertEquals("WHER_", SqlIdentifier.buildIdentifier("where", 5, Collections.emptySet()));
	}

	@Test
	public void testNameConflicts() {
		Set<String> alreadyUsedNames = new HashSet<>();
		alreadyUsedNames.add("NAME");
		
		Assert.assertEquals("NA_1", SqlIdentifier.buildIdentifier("name", 4, alreadyUsedNames));
		alreadyUsedNames.add("NA_1");
		
		// add additional 8 'name'
		for (int i = 2; i<=10; i++) {
			alreadyUsedNames.add(SqlIdentifier.buildIdentifier("name", 4, alreadyUsedNames));
		}

		// the eleventh needs one place at the end more
		Assert.assertEquals("N_11", SqlIdentifier.buildIdentifier("name", 4, alreadyUsedNames));
	}

}
