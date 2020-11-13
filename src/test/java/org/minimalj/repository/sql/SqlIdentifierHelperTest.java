package org.minimalj.repository.sql;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

public class SqlIdentifierHelperTest {

	@Test
	public void testReservedSqlWords() {
		Assert.assertEquals("where_", new SqlIdentifier(128).column("where", Collections.emptySet()));
		Assert.assertEquals("wher_", new SqlIdentifier(5).column("where", Collections.emptySet()));
	}

	@Test
	public void testNameConflicts() {
		SqlIdentifier sqlIdentifier = new SqlIdentifier(4);
		
		Set<String> alreadyUsedNames = new HashSet<>();
		alreadyUsedNames.add("name");
		
		Assert.assertEquals("na_1", sqlIdentifier.column("name", alreadyUsedNames));
		alreadyUsedNames.add("na_1");
		
		// add additional 8 'name'
		for (int i = 2; i<=10; i++) {
			alreadyUsedNames.add(sqlIdentifier.column("name", alreadyUsedNames));
		}

		// the eleventh needs one place at the end more
		Assert.assertEquals("n_11", sqlIdentifier.column("name", alreadyUsedNames));
	}

}
