package org.minimalj.repository.sql;

import org.junit.After;
import org.junit.Before;
import org.minimalj.model.Model;
import org.minimalj.repository.DataSourceFactory;

public abstract class SqlTest implements Model {

	protected SqlRepository repository;

	@Before
	public void createTables() {
		createRepositoryH2();
		// createRepositoryMsSql();
		initData();
	}

	protected void createRepositoryH2() {
		repository = new SqlRepository(DataSourceFactory.embeddedDataSource(), getEntityClasses());
	}

	protected void createRepositoryMsSql() {
		repository = new SqlRepository(DataSourceFactory.mssqlDataSource("jdbc:sqlserver://localhost:1433", "user", "password"), getEntityClasses());
		repository.createTables();
	}

	@After
	public void dropTables() {
		repository.dropTables();
	}

	protected void initData() {
		//
	}

}
