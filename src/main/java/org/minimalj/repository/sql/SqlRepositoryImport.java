package org.minimalj.repository.sql;

import java.io.File;
import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.minimalj.application.Application;
import org.minimalj.model.Model;
import org.minimalj.model.properties.Properties;
import org.minimalj.model.properties.Property;
import org.minimalj.util.CsvReader;
import org.minimalj.util.FieldUtils;
import org.minimalj.util.IdUtils;
import org.minimalj.util.StringUtils;

public class SqlRepositoryImport {

	private SqlRepository sqlRepository;

	public SqlRepositoryImport(SqlRepository sqlRepository) {
		this.sqlRepository = sqlRepository;
	}

	public void imprt(File zipFile) {
		System.out.println("Read from: " + zipFile.getPath());
		try (FileInputStream fis = new FileInputStream(zipFile); ZipInputStream zis = new ZipInputStream(fis)) {
			sqlRepository.execute("SET REFERENTIAL_INTEGRITY FALSE");
			Model.getClassesRecursive(Application.getInstance().getEntityClasses(), false, false).forEach(c -> {
				if (sqlRepository.tables.containsKey(c)) {
					sqlRepository.deleteAll(c);
				}
			});
			List<Class<?>> classes = Model.getClassesRecursive(Application.getInstance().getEntityClasses(), true, false);
			ZipEntry zipEntry;
			while ((zipEntry = zis.getNextEntry()) != null) {
				String entryName = zipEntry.getName();
				System.out.println("Import: " + entryName);
				if (!entryName.endsWith(".csv") || entryName.startsWith("User.")) {
					continue;
				}
				String className = entryName.substring(0, entryName.length() - 4);
				Class<?> clazz = classes.stream().filter(c -> c.getSimpleName().equals(className)).findFirst().orElse(null);
				if (clazz == null) {
					continue;
				}
				CsvReader csvReader = new CsvReader(zis);
				csvReader.readValues(new SqlRepositoryImportConsumer(clazz));
			}
			sqlRepository.execute("SET REFERENTIAL_INTEGRITY TRUE");
		} catch (Exception x) {
			throw new RuntimeException(x);
		}
	}

	private class SqlRepositoryImportConsumer implements Consumer<List<String>>, AutoCloseable {
		private final Class<?> clazz;
		private final AbstractTable<?> table;
		private Connection connection;
		private PreparedStatement preparedStatement;
		private List<Class<?>> columnClasses = new ArrayList<>();

		public SqlRepositoryImportConsumer(Class<?> clazz) {
			this.clazz = clazz;
			table = sqlRepository.tables.get(clazz);
		}

		@Override
		public void accept(List<String> values) {
			try {
				doAccept(values);
			} catch (Exception x) {
				throw new RuntimeException(x);
			}
		}

		public void doAccept(List<String> values) throws SQLException {
			if (columnClasses.isEmpty()) {
				String columnNames = "";
				String parameters = "";
				for (String v : values) {
					if (StringUtils.equals(v.toLowerCase(), "id")) {
						columnClasses.add(IdUtils.getIdClass(clazz));
						columnNames += v + ",";
					} else {
						Property property = Properties.getPropertyByPath(clazz, v);
						if (IdUtils.hasId(property.getClazz())) {
							columnClasses.add(IdUtils.getIdClass(property.getClazz()));
						} else {
							columnClasses.add(property.getClazz());
						}
						if (property == null) {
							System.out.println("Not found: " + v);
						}
						String columnName = table.columns.entrySet().stream().filter(e -> e.getValue().getPath().equals(property.getPath())).findAny()
								.orElseThrow(() -> new IllegalArgumentException(property.getPath())).getKey();
						columnNames += columnName + ",";
					}
					parameters += "?,";
				}
				columnNames = columnNames.substring(0, columnNames.length() - 1);
				parameters = parameters.substring(0, parameters.length() - 1);
				String insertStatment = "INSERT INTO " + table.name + "(" + columnNames + ") VALUES (" + parameters + ")";
				connection = sqlRepository.getConnection();
				preparedStatement = connection.prepareStatement(insertStatment);
			} else {
				int parameterIndex = 0;
				for (String v : values) {
					Class<?> columnClass = columnClasses.get(parameterIndex);
					if (v.isEmpty()) {
						sqlRepository.sqlDialect.setParameterNull(preparedStatement, ++parameterIndex, columnClass);
					} else {
						try {
							Object value = FieldUtils.parse(v, columnClass);
							sqlRepository.sqlDialect.setParameter(preparedStatement, ++parameterIndex, value);
						} catch (Exception x) {
							System.out.println(x.getMessage() + " für " + columnClass + " value: " + v);
							sqlRepository.sqlDialect.setParameterNull(preparedStatement, ++parameterIndex, null);
						}
					}
				}
				preparedStatement.execute();
			}
		}

		@Override
		public void close() throws SQLException {
			preparedStatement.close();
			connection.close();
		}
	}

}