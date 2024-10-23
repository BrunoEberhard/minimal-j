package org.minimalj.repository.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.minimalj.application.Application;
import org.minimalj.model.Model;
import org.minimalj.model.properties.Property;
import org.minimalj.repository.query.By;
import org.minimalj.util.StringUtils;

public class SqlRepositoryExport {

	private final SqlRepository sqlRepository;

	public SqlRepositoryExport(SqlRepository sqlRepository) {
		this.sqlRepository = sqlRepository;
	}

	public void export(SqlRepository exportRepository) {
		try (Connection connection = sqlRepository.getConnection(); Connection exportConnection = exportRepository.getConnection()) {
			exportRepository.execute("SET REFERENTIAL_INTEGRITY FALSE");
			List<Class<?>> classes = Model.getClassesRecursive(Application.getInstance().getEntityClasses(), true, false);
			for (Class<?> clazz : classes) {
				if (!sqlRepository.tableExists(clazz)) {
					continue;
				}
				if (sqlRepository.count(clazz, By.ALL) == 0) {
					continue;
				}
				AbstractTable<?> table = sqlRepository.tables.get(clazz);
				exportTable(table, connection, exportConnection, exportRepository);
			}
		} catch (Exception e1) {
			throw new RuntimeException(e1);
		}
		exportRepository.execute("SET REFERENTIAL_INTEGRITY TRUE");
		exportRepository.closeAllConnections();
	}

	protected void exportTable(AbstractTable<?> table, Connection connection, Connection exportConnection, SqlRepository exportRepository) throws Exception {
		PreparedStatement preparedStatement = null;
		AbstractTable<?> exportTable = exportRepository.getTable(table.getClazz());
		try (Statement statement = connection.createStatement()) {
			statement.execute("SELECT * from " + table.name);
			try (ResultSet resultSet = statement.getResultSet()) {
				while (resultSet.next()) {
					int columnCount = resultSet.getMetaData().getColumnCount();
					String columnNames = "";
					String values = "";
					if (preparedStatement == null) {
						Set<Entry<String, Property>> exportTableColumns = exportRepository.getTable(table.getClazz()).columns.entrySet();
						for (int column = 1; column <= columnCount; column++) {
							String columnName = resultSet.getMetaData().getColumnName(column);
							if (!StringUtils.equals(columnName.toLowerCase(), "id")) {
								Property property = table.columns.entrySet().stream().filter(e -> e.getKey().equalsIgnoreCase(columnName)).findFirst()
										.map(e -> e.getValue()).orElseThrow(() -> new IllegalArgumentException(columnName));

								String exportColumnName = exportTableColumns.stream().filter(e -> e.getValue().getPath().equals(property.getPath())).map(e -> e.getKey()).findAny().orElse(null);

								columnNames += exportColumnName + ",";
							} else {
								columnNames += "id,";
							}
							values += "?,";
						}
						columnNames = columnNames.substring(0, columnNames.length() - 1);
						values = values.substring(0, values.length() - 1);
						String insertQuery = "INSERT INTO " + exportTable.name + "(" + columnNames + ") VALUES (" + values + ")";
						preparedStatement = exportConnection.prepareStatement(insertQuery);
					}
					for (int column = 1; column <= columnCount; column++) {
						Object value = resultSet.getObject(column);
						preparedStatement.setObject(column, value);
					}
					preparedStatement.execute();
				}
			}
		} finally {
			if (preparedStatement != null) {
				preparedStatement.close();
			}
		}
	}
}
