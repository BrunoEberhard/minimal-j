package org.minimalj.repository;

import java.sql.SQLException;
import java.util.logging.Logger;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.h2.jdbcx.JdbcDataSource;
import org.mariadb.jdbc.MariaDbDataSource;
import org.minimalj.application.Configuration;
import org.minimalj.util.LoggingRuntimeException;
import org.minimalj.util.StringUtils;
import org.postgresql.ds.PGSimpleDataSource;

import com.microsoft.sqlserver.jdbc.SQLServerDataSource;

public class DataSourceFactory {
	public static final Logger logger = Logger.getLogger(DataSourceFactory.class.getName());
	
	public static DataSource create() {
		DataSource jndiDataSource = DataSourceFactory.getJndiDataSource();
		if (jndiDataSource != null) {
			return jndiDataSource;
		}
		
		String database = Configuration.get("MjSqlDatabase");
		String user = Configuration.get("MjSqlDatabaseUser", "APP");
		String password = Configuration.get("MjSqlDatabasePassword", "APP");
		
		if (!StringUtils.isBlank(database)) {
			return DataSourceFactory.dataSource(database, user, password);
		} else {
			String databaseFile = Configuration.get("MjSqlDatabaseFile", null);
			return DataSourceFactory.embeddedDataSource(databaseFile);
		}
	}
	
	public static DataSource getJndiDataSource() {
		try {
			Context initContext = new InitialContext();
			return (DataSource) initContext.lookup("java:/comp/env/jdbc");
		} catch (NamingException e) {
			logger.fine("Exception while retrieving JNDI datasource (" + e.getMessage() + ")");
			return null;
		}
	}

	// every JUnit test must have a fresh memory db
	private static int memoryDbCount = 1;
	
	/**
	 * Convenience method for prototyping and testing. The tables will be
	 * created automatically.
	 * 
	 * To make this db inspectable call: 
	 * org.h2.tools.Server.createTcpServer().start(); 
	 * and connect to
	 * jdbc:h2:tcp://localhost:9092/mem:TempDB1
	 * 
	 * @return a DataSource representing a in memory database
	 */
	public static DataSource embeddedDataSource() {
		return embeddedDataSource(null);
	}
	
	public static DataSource embeddedDataSource(String file) {
		return h2DataSource(file != null ? "jdbc:h2:" + file : "jdbc:h2:mem:TempDB" + (memoryDbCount++));
	}

	public static DataSource h2DataSource(String url) {
		JdbcDataSource dataSource;
		try {
			dataSource = new JdbcDataSource();
		} catch (NoClassDefFoundError e) {
			logger.severe("Missing JdbcDataSource. Please ensure to have h2 in the classpath");
			throw new IllegalStateException("Configuration error: Missing JdbcDataSource");
		}
		dataSource.setUrl(url);
		return dataSource;
	}

	public static DataSource dataSource(String url, String user, String password) {
		if (url.startsWith("jdbc:oracle")) {
			return oracleDbDataSource(url, user, password);
		} else if (url.startsWith("jdbc:mariadb")) {
			return mariaDbDataSource(url, user, password);
		} else if (url.startsWith("jdbc:postgresql")) {
			return postgresqlDataSource(url, user, password);
		} else if (url.startsWith("jdbc:sqlserver")) {
			return mssqlDataSource(url, user, password);
		} else if (url.startsWith("jdbc:h2")) {
			return h2DataSource(url);
		} else {
			throw new RuntimeException("Unknown jdbc URL: " + url);
		}
	}
	
	public static DataSource mariaDbDataSource(String url, String user, String password) {
		try {
			MariaDbDataSource dataSource = new MariaDbDataSource();
			dataSource.setUrl(url);
			dataSource.setUser(user);
			dataSource.setPassword(password);
			return dataSource;
		} catch (SQLException e) {
			throw new RuntimeException(e);
		} catch (NoClassDefFoundError e) {
			logger.severe("Missing MariaDbDataSource. Please ensure to have mariadb-java-client in the classpath");
			throw new IllegalStateException("Configuration error: Missing MariaDbDataSource");
		}
	}
	
	public static DataSource postgresqlDataSource(String url, String user, String password) {
		try {
			PGSimpleDataSource dataSource = new PGSimpleDataSource();
			dataSource.setUrl(url);
			dataSource.setUser(user);
			dataSource.setPassword(password);
			return dataSource;
		} catch (NoClassDefFoundError e) {
			logger.severe("Missing PGSimpleDataSource. Please ensure to have postgresql driver in the classpath");
			throw new IllegalStateException("Configuration error: Missing PGSimpleDataSource");
		}
	}

	public static DataSource mssqlDataSource(String url, String user, String password) {
		try {
			SQLServerDataSource dataSource = new SQLServerDataSource();
			dataSource.setURL(url);
			dataSource.setUser(user);
			dataSource.setPassword(password);
			return dataSource;
		} catch (NoClassDefFoundError e) {
			logger.severe("Missing SQLServerDataSource. Please ensure to have mssql-jdbc in the classpath");
			throw new IllegalStateException("Configuration error: Missing SQLServerDataSource");
		}
	}

	/**
	 * Don't forget to add the dependency to ojdbc like this in your pom.xml
	 * <pre>
	 * &lt;dependency&gt;
	 * 	&lt;groupId&gt;com.oracle&lt;/groupId&gt;
	 * 	&lt;artifactId&gt;ojdbc7&lt;/artifactId&gt;
	 * 	&lt;version&gt;12.1.0.2&lt;/version&gt;
	 * 	&lt;scope&gt;provided&lt;/scope&gt;
	 * &lt;/dependency&gt;		
	 * </pre>
	 * 
	 * You need to register at the oracle maven repository to actually get the driver.
	 * 
	 * @param url for example "jdbc:oracle:thin:@localhost:1521:orcl"
	 * @param user User
	 * @param password Password
	 * @return DataSource
	 */
	public static DataSource oracleDbDataSource(String url, String user, String password) {
		try {
			@SuppressWarnings("unchecked")
			Class<? extends DataSource> dataSourceClass = (Class<? extends DataSource>) Class.forName("oracle.jdbc.pool.OracleDataSource");
			DataSource dataSource = dataSourceClass.newInstance();
			dataSourceClass.getMethod("setURL", String.class).invoke(dataSource, url);
			dataSourceClass.getMethod("setUser", String.class).invoke(dataSource, user);
			dataSourceClass.getMethod("setPassword", String.class).invoke(dataSource, password);
			
			// OracleDataSource dataSource = new OracleDataSource();
			// dataSource.setURL(url);
			// dataSource.setUser(user);
			// dataSource.setPassword(password);
			return dataSource;
		} catch (Exception e) {
			throw new LoggingRuntimeException(e, logger, "Cannot connect to oracle db");
		}
	}

}
