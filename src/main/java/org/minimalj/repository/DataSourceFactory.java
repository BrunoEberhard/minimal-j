package org.minimalj.repository;

import java.util.logging.Logger;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.apache.derby.jdbc.EmbeddedDataSource;
import org.h2.jdbcx.JdbcDataSource;
import org.mariadb.jdbc.MySQLDataSource;
import org.minimalj.util.LoggingRuntimeException;

public class DataSourceFactory {
	public static final Logger logger = Logger.getLogger(DataSourceFactory.class.getName());
	
	public static DataSource getJndiDataSource() {
		try {
			Context initContext = new InitialContext();
			DataSource dataSource = (DataSource) initContext.lookup("java:/comp/env/jdbc");
			return dataSource;
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
	 * @return a DataSource representing a in memory database
	 */
	public static DataSource embeddedDataSource() {
		return embeddedDataSource(null);
	}
	
	public static DataSource embeddedDataSource(String file) {
		JdbcDataSource dataSource;
		try {
			dataSource = new JdbcDataSource();
		} catch (NoClassDefFoundError e) {
			logger.severe("Missing JdbcDataSource. Please ensure to have h2 in the classpath");
			throw new IllegalStateException("Configuration error: Missing JdbcDataSource");
		}
		dataSource.setUrl(file != null ? "jdbc:h2:" + file : "jdbc:h2:mem:TempDB" + (memoryDbCount++));
		return dataSource;
	}

	public static DataSource embeddedDerbyDataSource(String file) {
		EmbeddedDataSource dataSource;
		try {
			dataSource = new EmbeddedDataSource();
		} catch (NoClassDefFoundError e) {
			logger.severe("Missing EmbeddedDataSource. Please ensure to have derby in the classpath");
			throw new IllegalStateException("Configuration error: Missing EmbeddedDataSource");
		}
		
		dataSource.setDatabaseName(file != null ? file : "memory:TempDB" + (memoryDbCount++));
		dataSource.setCreateDatabase("create");
		return dataSource;
	}
	
	public static DataSource dataSource(String url, String user, String password) {
		if (url.startsWith("jdbc:oracle")) {
			return oracleDbDataSource(url, user, password);
		} else if (url.startsWith("jdbc:mariadb")) {
			return mariaDbDataSource(url, user, password);
		} else {
			throw new RuntimeException("Unknown jdbc URL: " + url);
		}
	}
	
	public static DataSource mariaDbDataSource(String url, String user, String password) {
		try {
			MySQLDataSource dataSource = new MySQLDataSource();
			dataSource.setURL(url);
			dataSource.setUser(user);
			dataSource.setPassword(password);
			return dataSource;
		} catch (NoClassDefFoundError e) {
			logger.severe("Missing MySQLDataSource. Please ensure to have mariadb-java-client in the classpath");
			throw new IllegalStateException("Configuration error: Missing MySQLDataSource");
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
	
//	private void connectToCloudFoundry() throws ClassNotFoundException, SQLException, JSONException {
//		String vcap_services = System.getenv("VCAP_SERVICES");
//		
//		if (vcap_services != null && vcap_services.length() > 0) {
//			JSONObject root = new JSONObject(vcap_services);
//
//			JSONArray mysqlNode = (JSONArray) root.get("mysql-5.1");
//			JSONObject firstSqlNode = (JSONObject) mysqlNode.get(0);
//			JSONObject credentials = (JSONObject) firstSqlNode.get("credentials");
//
//			String dbname = credentials.getString("name");
//			String hostname = credentials.getString("hostname");
//			String user = credentials.getString("user");
//			String password = credentials.getString("password");
//			String port = credentials.getString("port");
//			
//			String dbUrl = "jdbc:mysql://" + hostname + ":" + port + "/" + dbname;
//
//			Class.forName("com.mysql.jdbc.Driver");
//			connection = DriverManager.getConnection(dbUrl, user, password);
//			
//			isDerbyDb = false;
//			isDerbyMemoryDb = false;
//			isMySqlDb = true;
//		}
//	}
}
