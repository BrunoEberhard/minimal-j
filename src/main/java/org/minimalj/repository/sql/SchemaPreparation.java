package org.minimalj.repository.sql;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.minimalj.model.Code;
import org.minimalj.model.EnumUtils;
import org.minimalj.model.properties.Property;
import org.minimalj.util.Codes;
import org.minimalj.util.CsvReader;
import org.minimalj.util.StringUtils;

public enum SchemaPreparation {
	none, create, verifyOnly, update, updateWithDrops;

	private static final Logger logger = Logger.getLogger(SchemaPreparation.class.getName());

	public boolean doUpdate() {
		return this == update || this == updateWithDrops;
	}

	public void prepare(SqlRepository repository) throws SQLException {
		if (this == SchemaPreparation.create) {
			repository.beforeCreateTables();
			createEnums(repository);
			createTables(repository);
			createCodes(repository);
			repository.afterCreateTables();
		} else if (this != SchemaPreparation.none) {
			updateEnums(repository, this);
			updateTables(repository, this);
		}
	}

	// create

	private void createEnums(SqlRepository repository) {
		for (Class<? extends Enum<?>> enumClass : repository.enums) {
			String identifier = repository.sqlIdentifier.identifier(enumClass.getSimpleName(), Collections.emptyList());
			String query = repository.getSqlDialect().createEnum(enumClass, identifier);
			if (query != null) {
				repository.execute(query);
			}
		}
	}

	private void createTables(SqlRepository repository) {
		for (AbstractTable<?> table : repository.tables.values()) {
			table.createTable(repository.sqlDialect);
		}
		for (AbstractTable<?> table : repository.tables.values()) {
			table.createIndexes(repository.sqlDialect);
		}
		for (AbstractTable<?> table : repository.tables.values()) {
			table.createConstraints(repository.sqlDialect);
		}
	}

	// TODO move someplace where it's available for all kind of repositories (Memory
	// DB for example)
	private void createCodes(SqlRepository repository) {
		repository.startTransaction(Connection.TRANSACTION_READ_UNCOMMITTED);
		createConstantCodes(repository);
		createCsvCodes(repository);
		repository.endTransaction(true);
	}

	@SuppressWarnings("unchecked")
	private void createConstantCodes(SqlRepository repository) {
		for (AbstractTable<?> table : repository.tables.values()) {
			if (Code.class.isAssignableFrom(table.getClazz())) {
				Class<? extends Code> codeClass = (Class<? extends Code>) table.getClazz();
				List<? extends Code> constants = Codes.getConstants(codeClass);
				for (Code code : constants) {
					((Table<Code>) table).insert(code);
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	private void createCsvCodes(SqlRepository repository) {
		List<AbstractTable<?>> tableList = new ArrayList<>(repository.tables.values());
		for (AbstractTable<?> table : tableList) {
			if (Code.class.isAssignableFrom(table.getClazz())) {
				Class<? extends Code> clazz = (Class<? extends Code>) table.getClazz();
				InputStream is = clazz.getResourceAsStream(clazz.getSimpleName() + ".csv");
				if (is != null) {
					CsvReader reader = new CsvReader(is, repository.getObjectProvider());
					List<? extends Code> values = reader.readValues(clazz);
					values.forEach(value -> ((Table<Code>) table).insert(value));
				}
			}
		}
	}

	// update

	protected void updateTables(SqlRepository repository, SchemaPreparation schemaPreparation) {
		List<AbstractTable<?>> createdTables = new ArrayList<>();
		for (AbstractTable<?> table : repository.tables.values()) {
			int count = repository.find(Integer.class, tableExists(table.clazz, table.name), 1).get(0);
			if (count == 0) {
				logger.info("New table: " + table.name);
				table.createTable(repository.sqlDialect);
				createdTables.add(table);
			} else {
				updateTableColumns(repository, schemaPreparation, table);
				// TODO updateTableIndexes
				// TODO updateTableConstraints
			}
		}
		for (AbstractTable<?> table : createdTables) {
			table.createIndexes(repository.sqlDialect);
		}
		for (AbstractTable<?> table : createdTables) {
			table.createConstraints(repository.sqlDialect);
		}
	}

	protected void updateTableColumns(SqlRepository repository, SchemaPreparation schemaPreparation, AbstractTable<?> table) {
		List<String> columnNames = repository.find(String.class, selectColumns(table.name), 10000);
		for (Map.Entry<String, Property> column : table.getColumns().entrySet()) {
			String columnName = column.getKey();
			if (!columnNames.contains(columnName)) {
				logger.info("New column: " + table.name + "." + columnName);
				if (schemaPreparation.doUpdate()) {
					String s = "ALTER TABLE " + table.name + " ADD COLUMN " + columnName + " "
							+ table.getColumnDefinition(repository.sqlDialect, column.getValue());
					repository.execute(s);
				}
			} else {
				// TODO verifyColumnType
			}
		}
		for (String columnName : columnNames) {
			if (!table.getColumns().containsKey(columnName) && !StringUtils.equals(columnName, "id", "version", "historized")) {
				logger.info("Drop column: " + table.name + "." + columnName);
				String s = "ALTER TABLE " + table.name + " DROP COLUMN " + columnName;
				repository.execute(s);
			}
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected void updateEnums(SqlRepository repository, SchemaPreparation schemaPreparation) {
		for (Class enumClass : repository.enums) {
			String enumIdentifier = repository.sqlIdentifier.identifier(enumClass.getSimpleName(), Collections.emptyList()); // + "a";
			int count = repository.find(Integer.class, enumExists(enumClass, enumIdentifier), 1).get(0);
			if (count == 0) {
				logger.info("New enum: " + enumIdentifier);
				if (schemaPreparation.doUpdate()) {
					repository.execute(repository.sqlDialect.createEnum(enumClass, enumIdentifier));
				}
			} else {
				String getEnumValues = selectEnumValues(enumClass, enumIdentifier);
				List<String> existingEnumValues = repository.find(String.class, getEnumValues, 10000);
				List<Enum> enumValues = EnumUtils.valueList(enumClass);
				for (Enum enmValue : enumValues) {
					if (!existingEnumValues.contains(enmValue.name())) {
						logger.info("New enum value: " + enumIdentifier + "." + enmValue.name());
						if (schemaPreparation.doUpdate()) {
							repository.execute(addEnumValue(enumClass, enumIdentifier, enmValue.name()));
						}
					}
				}
			}
		}
	}

	protected String tableExists(Class<?> clazz, String identifier) {
		return "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = current_schema AND table_name = '" + identifier + "'";
	}

	protected String enumExists(Class<?> clazz, String identifier) {
		return "SELECT COUNT(*) FROM pg_type WHERE typcategory = 'E' AND typname = '" + identifier + "'";
	}

	protected String selectEnumValues(Class<?> clazz, String enumIdentifier) {
		return "SELECT unnest(enum_range(NULL::" + enumIdentifier + "))";
	}

	protected String addEnumValue(Class<?> clazz, String enumIdentifier, String value) {
		return "ALTER TYPE " + enumIdentifier + " ADD VALUE '" + value + "'";
	}

//	protected String columnExists(String tableIdentifier, String columnIdentifier) {
//		return "SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = current_schema AND table_name = '" + tableIdentifier + "'" + " AND column_name = '" + columnIdentifier + "'";
//	}

	protected String selectColumns(String tableIdentifier) {
		return "SELECT column_name FROM information_schema.columns WHERE table_schema = current_schema AND table_name = '" + tableIdentifier + "'";
	}

}