package org.minimalj.backend.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.minimalj.util.resources.ResourceHelper;
import org.minimalj.util.resources.Resources;

/**
 * Was the version management of the database. Not used at moment
 * 
 */
@Deprecated
public class DbVersion {
	private static Logger logger = Logger.getLogger(DbVersion.class.getName());
	
	public static String getVersion() {
		return ResourceHelper.getString(Resources.getResourceBundle(), "Database.version");
	}

	public static int getVersionAsInt() {
		try {
			return Integer.parseInt(Resources.getString("Database.versionAsInt"));
		} catch (Exception x) {
			logger.log(Level.CONFIG, "Database.versionAsInt missing or not a Integer", x);
			return 10000;
		}
	}
	
	public static int getMinimalVersionAsInt() {
		try {
			return Integer.parseInt(Resources.getString("Database.minimalVersionAsInt"));
		} catch (Exception x) {
			logger.log(Level.CONFIG, "Database.minimalVersionAsInt missing or not a Integer", x);
			return 10000;
		}
	}

	//
	
	public static int getVersionOf(Connection connection) {
		Statement statement = null;
		
		try {
			statement = connection.createStatement();
			ResultSet resultSet = statement.executeQuery("SELECT MAX(versionCount) FROM Version");
			if (resultSet.next()) {
				return resultSet.getInt(1);
			}
		} catch (SQLException x) {
			// Vielleicht exisitert Version gar nicht
			
		}
		finally {
			if (statement != null) {
				try {
					// does close the resultSet too
					statement.close();
				} catch (SQLException e) {
					e.printStackTrace();
				} 
			}
		}
		
		// empty DB. Will get the version you want
		return getVersionAsInt();
	}
	
	public static void insertVersion(Connection connection) {
		PreparedStatement statement = null;
		
		try {
			statement = connection.prepareStatement("INSERT INTO Version (application, versionCount, version, description, time) VALUES (?, ?, ?,  ?, current_timestamp)");
			statement.setString(1, ResourceHelper.getApplicationTitle());
			statement.setInt(2, getVersionAsInt());
			statement.setString(3, getVersion());
			statement.setString(4, "Created");
			statement.execute();
		} catch (SQLException x) {
			logger.log(Level.WARNING, "Could not insert DbVersion", x);
		} finally {
			if (statement != null) {
				try {
					statement.close(); // does close the resultSet too
				} catch (SQLException x) {
					logger.log(Level.WARNING, "Could not close statement", x);
				}
			}
		}
	}	


}
