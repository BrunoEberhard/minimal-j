package org.minimalj.model;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.minimalj.backend.db.DbPersistence;

public class EnumSetTest {

	private static DbPersistence persistence;
	
	@BeforeClass
	public static void setupDb() throws SQLException {
		persistence = new DbPersistence(DbPersistence.embeddedDataSource(), ObjectWithE.class);
	}

	@Test
	public void testEnumToIntFirstElement() {
		Set<E> set = Collections.singleton(E.e0);
		testConversionAndDb(set);
	}

	void testConversionAndDb(Set<E> set) {
		Assert.assertTrue(test(set));
		Assert.assertTrue(testWithDb(set));
	}

	@Test
	public void testEnumToIntSomeElements() {
		Set<E> set = new HashSet<E>();
		set.add(E.e1);
		set.add(E.e5);
		set.add(E.e14);
		testConversionAndDb(set);
	}
	
	@Test
	public void testEnumToIntAllElements() {
		Set<E> set = new HashSet<E>();
		set.addAll(Arrays.asList(E.values()));
		testConversionAndDb(set);
	}

	@Test
	public void testEnumToIntNoElements() {
		Set<E> set = Collections.emptySet();
		testConversionAndDb(set);
	}

	@Test
	public void testEnumToIntLastElement() {
		Set<E> set = Collections.singleton(E.e31);
		testConversionAndDb(set);
	}
	
	private boolean test(Set<E> testSet) {
		int i = EnumUtils.getInt(testSet, E.class);
		Set<E> resultSet = new HashSet<>();
		EnumUtils.fillSet(i, E.class, resultSet);
		return compareSets(testSet, resultSet);
	}

	private boolean testWithDb(Set<E> testSet) {
		ObjectWithE object = new ObjectWithE();
		object.setOfE.addAll(testSet);
		Object id = persistence.insert(object);
		
		ObjectWithE readObject = persistence.read(ObjectWithE.class, id);
		Set<E> resultSet = readObject.setOfE;
		
		return compareSets(testSet, resultSet);
	}

	private boolean compareSets(Set<E> testSet, Set<E> resultSet) {
		for (E e : E.values()) {
			boolean inTestSet = testSet.contains(e);
			boolean inResultSet = resultSet.contains(e);
			if (inTestSet != inResultSet) return false;
		}
		return true;
	}

	public static enum E {
		e0,  e1, e2, e3, e4, e5, e6, e7,
		e8, e9, e10, e11, e12, e13, e14, e15, 
		e16, e17, e18, e19, e20, e21, e22, e23,
		e24, e25, e26, e27, e28, e29, e30, e31;
	}
	
	public static class ObjectWithE {
		public Object id;
		public final Set<E> setOfE = new HashSet<>();
	}
}
