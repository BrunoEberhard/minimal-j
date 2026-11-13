package org.minimalj.repository.sql;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.minimalj.model.Dependable;
import org.minimalj.model.annotation.Size;
import org.minimalj.repository.DataSourceFactory;

/**
 * Foreign keys of dependables (e.g. the article of a position) must be created even if the
 * database already exists and the column is not new.
 */
public class SchemaUpdateForeignKeyTest {

	private String file;

	@Before
	public void deleteDatabase() {
		File directory = new File("target/" + getClass().getSimpleName());
		directory.mkdirs();
		file = new File(directory, "db" + System.nanoTime()).getAbsolutePath();
	}

	private SqlRepository open() {
		return new SqlRepository(DataSourceFactory.embeddedDataSource(file), TestDoc.class, TestTarget.class);
	}

	@Test
	public void testCreateHasForeignKeyOnlyForReference() {
		SqlRepository repository = open();
		List<String> foreignKeyColumns = foreignKeyColumns(repository, positionTable(repository));
		Assert.assertFalse(foreignKeyColumns.isEmpty());
		Assert.assertEquals("target", foreignKeyColumns.get(0));
	}

	@Test
	public void testUpdateCreatesMissingForeignKey() {
		SqlRepository repository = open();
		dropForeignKey(repository, positionTable(repository), "target");
		Assert.assertTrue(foreignKeyColumns(repository, positionTable(repository)).isEmpty());

		repository = open();
		List<String> foreignKeyColumns = foreignKeyColumns(repository, positionTable(repository));
		Assert.assertFalse(foreignKeyColumns.isEmpty());
		Assert.assertEquals("target", foreignKeyColumns.get(0));
	}

	@Test
	public void testUpdateSkipsForeignKeyWithOrphans() {
		SqlRepository repository = open();
		String positionTable = positionTable(repository);
		dropForeignKey(repository, positionTable, "target");

		TestTarget target = new TestTarget();
		target.name = "Target";
		target.id = repository.insert(target);
		TestDoc doc = new TestDoc();
		doc.name = "Doc";
		doc.id = repository.insert(doc);
		TestPos pos = new TestPos();
		pos.doc = doc;
		pos.target = target;
		repository.insert(pos);
		// without a foreign key the TestTarget can be deleted, the position then references nothing
		repository.execute("DELETE FROM " + repository.table(TestTarget.class));

		repository = open();
		Assert.assertTrue(foreignKeyColumns(repository, positionTable).isEmpty());
	}

	private static String positionTable(SqlRepository repository) {
		return repository.table(TestPos.class);
	}

	private static List<String> foreignKeyColumns(SqlRepository repository, String tableName) {
		return repository.find(String.class,
				"SELECT LOWER(kcu.column_name) FROM information_schema.table_constraints tc "
						+ "JOIN information_schema.key_column_usage kcu ON kcu.constraint_name = tc.constraint_name AND kcu.constraint_schema = tc.constraint_schema "
						+ "WHERE tc.constraint_type = 'FOREIGN KEY' AND tc.table_schema = current_schema AND LOWER(tc.table_name) = ?",
				100, tableName.toLowerCase());
	}

	private static void dropForeignKey(SqlRepository repository, String tableName, String column) {
		List<String> names = repository.find(String.class,
				"SELECT tc.constraint_name FROM information_schema.table_constraints tc "
						+ "JOIN information_schema.key_column_usage kcu ON kcu.constraint_name = tc.constraint_name AND kcu.constraint_schema = tc.constraint_schema "
						+ "WHERE tc.constraint_type = 'FOREIGN KEY' AND tc.table_schema = current_schema AND LOWER(tc.table_name) = ? AND LOWER(kcu.column_name) = ?",
				100, tableName.toLowerCase(), column);
		for (String name : names) {
			repository.execute("ALTER TABLE " + tableName + " DROP CONSTRAINT " + name);
		}
	}

	public static class TestTarget {
		public Object id;
		@Size(30)
		public String name;
	}

	public static class TestDoc {
		public Object id;
		@Size(30)
		public String name;
		public List<TestPos> positions = new ArrayList<>();
	}

	public static class TestPos implements Dependable<TestDoc> {
		public Object id;
		public TestDoc doc;
		public TestTarget target;

		@Override
		public TestDoc getParent() {
			return doc;
		}
	}
}
