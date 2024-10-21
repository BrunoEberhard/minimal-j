package org.minimalj.repository.sql;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Base64;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

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

	public void export(File zipFile) {
		if (zipFile.exists()) {
			throw new IllegalArgumentException("ZipFile already exists");
		}
//		if (!zipFile.canWrite()) {
//			throw new IllegalArgumentException("Cannot write on " + zipFile.getPath());
//		}
		try (FileOutputStream fileOutputStream = new FileOutputStream(zipFile); ZipOutputStream zos = new ZipOutputStream(fileOutputStream); PrintWriter writer = new PrintWriter(zos)) {
			List<Class<?>> classes = Model.getClassesRecursive(Application.getInstance().getEntityClasses(), true, false);
			for (Class<?> clazz: classes) {
				if (!sqlRepository.tableExists(clazz)) {
					continue;
				}
				if (sqlRepository.count(clazz, By.ALL) == 0) {
					continue;
				}
				AbstractTable<?> table = sqlRepository.tables.get(clazz);	
				zos.putNextEntry(new ZipEntry(clazz.getSimpleName() + ".csv"));
				exportTable(table, writer);
				writer.flush();
				zos.flush();
			}
		} catch (Exception e1) {
			throw new RuntimeException(e1);
		}
	}

	protected void exportTable(AbstractTable<?> table, PrintWriter writer) throws Exception {
		try (Connection connection = sqlRepository.getConnection(); Statement statement = connection.createStatement()) {
			statement.execute("SELECT * from " + table.name);
			try (ResultSet resultSet = statement.getResultSet()) {
				boolean firstRow = true;
				while (resultSet.next()) {
					int columnCount = resultSet.getMetaData().getColumnCount();
					if (firstRow) {
						firstRow = false;
						for (int column = 1; column <= columnCount; column++) {
							String columnName = resultSet.getMetaData().getColumnName(column);
							String propertyName = toPropertyName(table, columnName);
							writer.write(propertyName);
							writer.write(column < columnCount ? "," : "\n");
						}
					}
					for (int column = 1; column <= columnCount; column++) {
						String columnName = resultSet.getMetaData().getColumnName(column);
						Object value = resultSet.getObject(column);
						if (!StringUtils.equals(columnName.toLowerCase(), "id")) {
							Property property = table.columns.entrySet().stream().filter(e -> e.getKey().equalsIgnoreCase(columnName)).findFirst()
									.map(e -> e.getValue()).orElseThrow(() -> new IllegalArgumentException(columnName));
							if (property != null) {
								Class<?> fieldClass = property.getClazz();
								boolean isByteArray = fieldClass.isArray() && fieldClass.getComponentType() == Byte.TYPE;
								if (isByteArray) {
									byte[] bytes = resultSet.getBytes(column);
									value = bytes != null ? Base64.getEncoder().encodeToString(bytes) : "";
								} else {
									value = sqlRepository.getSqlDialect().convertToFieldClass(fieldClass, value);
								}
							}
						}
						write(writer, value);
						writer.write(column < columnCount ? "," : "\n");
					}
				}
			}
		}
	}

	private static String toPropertyName(AbstractTable<?> table, String columnName) {
		if (StringUtils.equals(columnName.toLowerCase(), "id", "version", "historized")) {
			return columnName.toLowerCase();
		}
		return table.columns.entrySet().stream().filter(e -> e.getKey().equalsIgnoreCase(columnName)).findFirst().map(e -> e.getValue())
				.orElseThrow(() -> new IllegalArgumentException(columnName)).getPath();
	}

	private static void write(PrintWriter writer, Object o) {
		if (o == null) {
			return;
		}
		String s = o.toString();
		if (!s.contains(",") && !s.contains("\n")) {
			writer.write(s);
		} else {
			writer.write('"');
			for (char c : s.toCharArray()) {
				if (c == '"') {
					writer.write("\"\"");
				} else {
					writer.write(c);
				}
			}
			writer.write('"');
		}
	}

}
