package org.minimalj.repository;

import java.util.logging.Logger;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.apache.derby.jdbc.EmbeddedDataSource;
import org.mariadb.jdbc.MySQLDataSource;

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

	/**
	 * Convenience method for prototyping and testing
	 * @return a DataSource representing a in memory database managed by derby db.
	 */
	public static DataSource embeddedDataSource() {
		return embeddedDataSource(null);
	}
	
	// every JUnit test must have a fresh memory db
	private static int memoryDbCount = 1;
	
	public static DataSource embeddedDataSource(String file) {
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
//		if (url.startsWith("jdbc:oracle")) {
//			return oracleDbDataSource(url, user, password);
//		} else 
		if (url.startsWith("jdbc:mariadb")) {
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

//	<dependency>
//		<groupId>com.oracle</groupId>
//		<artifactId>ojdbc7</artifactId>
//		<version>12.1.0.2</version>
//		<scope>provided</scope>
//	</dependency>		
//		
//	public static DataSource oracleDbDataSource(String url, String user, String password) {
//		try {
//			OracleDataSource dataSource = new OracleDataSource();
//			// dataSource.setURL("jdbc:oracle:thin:@localhost:1521:orcl");
//			dataSource.setURL(url);
//			dataSource.setUser(user);
//			dataSource.setPassword(password);
//			return dataSource;
//		} catch (NoClassDefFoundError e) {
//			logger.severe("Missing OracleConnectionPoolDataSourceImpl. Please ensure to have ojdbc7 in the classpath");
//			throw new IllegalStateException("Configuration error: Missing OracleConnectionPoolDataSourceImpl");
//		} catch (SQLException e) {
//			throw new LoggingRuntimeException(e, logger, "Cannot connect to oracle db");
//		}
//	}
	
	
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
