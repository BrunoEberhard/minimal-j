package org.minimalj.repository.sql;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.minimalj.model.EnumUtils;
import org.minimalj.model.annotation.AnnotationUtil;
import org.minimalj.model.annotation.NotEmpty;
import org.minimalj.model.properties.Property;
import org.minimalj.repository.sql.SqlDialect.PostgresqlDialect;
import org.minimalj.util.IdUtils;
import org.minimalj.util.StringUtils;

public enum SchemaPreparation {
	none, create, update; // TODO verify

	private static final Logger logger = Logger.getLogger(SchemaPreparation.class.getName());
	
	public boolean doUpdate() {
		return this == update;
	}

	public void prepare(SqlRepository repository) throws SQLException {
		logger.fine("Schema preparation: " + this.name());
		if (this == none) {
			return;
		}
		repository.beforeSchemaPreparation(this);
		if (this == SchemaPreparation.create) {
			createEnums(repository);
			createTables(repository);
		} else {
			if (repository.sqlDialect instanceof PostgresqlDialect) {
				updateEnums(repository, this);
			} else {
				// In h2: read Information_schema, ALTER TABLE EXAMPLE ALTER COLUMN C ENUM('a', 'b', 'c') NOT NULL;
				logger.fine("Update enums only implemented for Postgresql");
			}
			updateTables(repository, this);
			logger.fine("Unused tables are not removed");
		}
		repository.updateCodes();
		repository.afterSchemaPreparation(this);
		logger.fine("Schema prepared");
	}

	public void execute(SqlRepository repository, String query, Serializable... parameters) {
		if (query != null) {
			if (this == update) {
				repository.execute(query, parameters);
			} else {
				logger.info("NOT EXECUTING: " + query);
			}
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

	
	// update

	public static class InformationSchemaTable {
		public String tableName;
	}

	public static class InformationSchemaColumn {
		public String tableName;
		public String columnName;
		public boolean isNullable;
		public Integer maxLength;
	}

	static class InformationSchema {
		public List<InformationSchemaTable> tables;
		public List<InformationSchemaColumn> columns;

		public InformationSchema(SqlRepository repository) {
			tables = repository.find(InformationSchemaTable.class,
					"SELECT LOWER(table_name) tableName FROM information_schema.tables WHERE table_schema = current_schema", 10000);
			columns = repository.find(InformationSchemaColumn.class,
					"SELECT LOWER(table_name) tableName, LOWER(column_name) columnName, (is_nullable = 'YES') isNullable, character_maximum_length maxLength FROM information_schema.columns WHERE table_schema = current_schema",
					10000);
		}

		public boolean tableExists(String tableName) {
			return tables.stream().anyMatch(t -> t.tableName.equalsIgnoreCase(tableName));
		}

		public List<String> getColumnNames(String tableName) {
			return columns.stream().filter(t -> t.tableName.equalsIgnoreCase(tableName)).map(c -> c.columnName).collect(Collectors.toList());
		}

		public List<String> getNullableColumnNames(String tableName) {
			return columns.stream().filter(c -> c.isNullable && c.tableName.equalsIgnoreCase(tableName)).map(c -> c.columnName).collect(Collectors.toList());
		}

		public int getMaxLength(String tableName, String columnName) {
			return columns.stream().filter(c -> c.tableName.equalsIgnoreCase(tableName) && c.columnName.equalsIgnoreCase(columnName)).findFirst().get().maxLength;
		}
	}
	
	protected void updateTables(SqlRepository repository, SchemaPreparation schemaPreparation) {
		InformationSchema informationSchema = new InformationSchema(repository);
		
		List<AbstractTable<?>> createdTables = new ArrayList<>();
		List<NewColumn> newColumns = new ArrayList<>();
		for (AbstractTable<?> table : repository.tables.values()) {
			updateTable(repository, informationSchema, schemaPreparation, createdTables, newColumns, table);
		}
		for (AbstractTable<?> table : createdTables) {
			table.createIndexes(repository.sqlDialect);
		}
		for (AbstractTable<?> table : createdTables) {
			table.createConstraints(repository.sqlDialect);
		}
		for (NewColumn newColumn : newColumns) {
			newColumn.table.createConstraint(repository.sqlDialect, newColumn.name, newColumn.property);
		}
	}

	protected void updateTable(SqlRepository repository, InformationSchema informationSchema, SchemaPreparation schemaPreparation, List<AbstractTable<?>> createdTables, List<NewColumn> newColumns, AbstractTable<?> table) {
		if (!informationSchema.tableExists(table.name)) {
			logger.info("New table: " + table.name);
			table.createTable(repository.sqlDialect);
			createdTables.add(table);
		} else {
			if (!(table instanceof CrossTable)) {
				updateTableColumns(repository, informationSchema, schemaPreparation, table, newColumns);
			}
			if (table instanceof Table) {
				for (Object dependableTable : ((Table) table).getDependableTables()) {
					updateTable(repository, informationSchema, schemaPreparation, createdTables, newColumns, (AbstractTable<?>) dependableTable);
				}
				for (Object listTable : ((Table) table).getListTables()) {
					updateTable(repository, informationSchema, schemaPreparation, createdTables, newColumns, (AbstractTable<?>) listTable);
				}
			}
		}
	}
	
	private static class NewColumn {
		public AbstractTable<?> table;
		public String name;
		public Property property;
	}

	protected void updateTableColumns(SqlRepository repository, InformationSchema informationSchema, SchemaPreparation schemaPreparation, AbstractTable<?> table, List<NewColumn> newColumns) {
		List<String> columnNames = informationSchema.getColumnNames(table.name);
		List<String> nullableColumns = informationSchema.getNullableColumnNames(table.name);;
		for (Map.Entry<String, Property> column : table.getColumns().entrySet()) {
			Property property = column.getValue();
			String columnName = column.getKey();
			boolean notEmptyProperty = property.getAnnotation(NotEmpty.class) != null;
			if (!columnNames.contains(column.getKey().toLowerCase())) {
				logger.info("New column: " + table.name + "." + columnName);
				String s = "ALTER TABLE " + table.name + " ADD COLUMN " + columnName + " "
						+ table.getColumnDefinition(repository.sqlDialect, property);
				execute(repository, s);
				if (notEmptyProperty) {
					boolean possible = initializeNullValues(repository, table, property, columnName);
					if (possible) {
						logger.info("Make new column " + columnName + " not nullable");
						execute(repository, "ALTER TABLE " + table.name + " ALTER COLUMN " + columnName + " SET NOT NULL");
					} else {
						logger.severe("New column: " + table.name + "." + columnName + " cannot set to not nullable as there is no initial value set in the class");
					}
				}
				if (IdUtils.hasId(property.getClazz()) && doUpdate()) {
					table.createIndex(columnName);
				}
				// defer create constraint (maybe table does not exist yet)
				NewColumn newColumn = new NewColumn(); // convert to record
				newColumn.name = columnName;
				newColumn.table = table;
				newColumn.property = property;
				newColumns.add(newColumn);
			} else {
				boolean nullableColumn = nullableColumns.contains(columnName.toLowerCase());
				if (!nullableColumn && !notEmptyProperty) {
					logger.info("Make column nullable: " + table.name + "." + columnName);
					String s = "ALTER TABLE " + table.name + " ALTER COLUMN " + columnName + " DROP NOT NULL";
					execute(repository, s);
				} else if (nullableColumn && notEmptyProperty) {
					// as above. First check if there is a null value in the db
					// count where is null
					// only if count > 0 check for empty value...
					boolean possible = true;
					Integer countNull = repository.execute(Integer.class, "SELECT COUNT(*) FROM " + table.name + " WHERE " + columnName + " IS NULL");
					if (countNull != null && countNull > 0) {
						possible = initializeNullValues(repository, table, property, columnName);
					}
					if (possible) {
						logger.info("Make column not nullable: " + table.name + "." + columnName);
						String s = "ALTER TABLE " + table.name + " ALTER COLUMN " + columnName + " SET NOT NULL";
						execute(repository, s);
					} else {
						logger.severe("Set column " + table.name + "." + columnName + " to not nullable as there is no initial value set in the class");
					}
				}
				
				if (property.getClazz() == String.class) {
					int maxLength = informationSchema.getMaxLength(table.name, columnName);
					int annotatedSize = AnnotationUtil.getSize(property);
					if (maxLength > annotatedSize) {
						// TODO shorten content
					}
					if (maxLength != annotatedSize) {
						logger.info(maxLength < annotatedSize ? "Increase" : "Decrease " + table.name + "." + columnName + " to size " + annotatedSize);
						execute(repository, "ALTER TABLE " + table.name + " ALTER COLUMN " + columnName + " TYPE VARCHAR(" + annotatedSize +")");
					}
				}
			}
		}
		for (String columnName : columnNames) {
			if (!table.getColumns().containsKey(columnName) && !StringUtils.equals(columnName.toLowerCase(), "id", "version", "position")  && !repository.getSpecialColumnNames().contains(columnName)) {
				logger.info("Drop column: " + table.name + "." + columnName);
				String s = "ALTER TABLE " + table.name + " DROP COLUMN " + columnName;
				execute(repository, s, new Serializable[0]);
			}
		}
	}

	protected boolean initializeNullValues(SqlRepository repository, AbstractTable<?> table, Property property, String columnName) {
		Object emptyValue = property.getValue(EmptyObjects.getEmptyObject(property.getDeclaringClass()));
		if (emptyValue != null) {
			logger.info("Initialize null values of " + table.name + "." + columnName + " to " + emptyValue);
			execute(repository, "UPDATE " + table.name + " SET " + columnName + " = ? WHERE " + columnName + " IS NULL", (Serializable) emptyValue);
			return true;
		} else {
			return false;
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
					execute(repository, repository.sqlDialect.createEnum(enumClass, enumIdentifier));
				}
			} else {
				String getEnumValues = selectEnumValues(enumClass, enumIdentifier);
				List<String> existingEnumValues = repository.find(String.class, getEnumValues, 10000);
				List<Enum> enumValues = EnumUtils.valueList(enumClass);
				for (Enum enmValue : enumValues) {
					if (!existingEnumValues.contains(enmValue.name())) {
						logger.info("New enum value: " + enumIdentifier + "." + enmValue.name());
						if (schemaPreparation.doUpdate()) {
							execute(repository, addEnumValue(enumClass, enumIdentifier, enmValue.name()));
						}
					}
				}
			}
		}
	}

	protected String enumExists(Class<?> clazz, String identifier) {
		return "SELECT COUNT(*) FROM pg_type WHERE typcategory = 'E' AND typname ilike '" + identifier + "' AND typnamespace = (SELECT oid FROM pg_catalog.pg_namespace WHERE nspname = current_schema)";
	}

	protected String selectEnumValues(Class<?> clazz, String enumIdentifier) {
		return "SELECT unnest(enum_range(NULL::" + enumIdentifier + "))";
	}

	protected String addEnumValue(Class<?> clazz, String enumIdentifier, String value) {
		return "ALTER TYPE " + enumIdentifier + " ADD VALUE '" + value + "'";
	}
}