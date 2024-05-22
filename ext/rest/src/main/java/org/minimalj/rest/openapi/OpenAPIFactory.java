package org.minimalj.rest.openapi;

import java.lang.reflect.Modifier;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.minimalj.metamodel.model.MjEntity;
import org.minimalj.metamodel.model.MjEntity.MjEntityType;
import org.minimalj.metamodel.model.MjModel;
import org.minimalj.metamodel.model.MjProperty;
import org.minimalj.metamodel.model.MjProperty.MjPropertyType;
import org.minimalj.model.Api;
import org.minimalj.model.Api.TransactionDefinition;
import org.minimalj.model.Code;
import org.minimalj.model.Dependable;
import org.minimalj.model.Keys;
import org.minimalj.model.Model;
import org.minimalj.model.View;
import org.minimalj.model.properties.FlatProperties;
import org.minimalj.model.properties.Properties;
import org.minimalj.rest.openapi.model.OpenAPI;
import org.minimalj.rest.openapi.model.OpenAPI.Content;
import org.minimalj.rest.openapi.model.OpenAPI.In;
import org.minimalj.rest.openapi.model.OpenAPI.Operation;
import org.minimalj.rest.openapi.model.OpenAPI.Parameter;
import org.minimalj.rest.openapi.model.OpenAPI.Property;
import org.minimalj.rest.openapi.model.OpenAPI.RequestBody;
import org.minimalj.rest.openapi.model.OpenAPI.Response;
import org.minimalj.rest.openapi.model.OpenAPI.Schema;
import org.minimalj.rest.openapi.model.OpenAPI.Type;
import org.minimalj.util.FieldUtils;
import org.minimalj.util.IdUtils;
import org.minimalj.util.StringUtils;

public class OpenAPIFactory {
	
	private final String SCHEMAS = "#/components/schemas/";

	public OpenAPI create(Model model) {
		OpenAPI api = new OpenAPI();
		api.openapi = "3.0.0";
		api.components = new OpenAPI.Components();
		
		api.info = new OpenAPI.Info();
		api.info.version = "1.0.0";
		api.info.title = model.getName();
		
		// Without specification simply "/" is used which fits perfectly.
		// Server server = new Server();
		// server.url = "http://localhost:8080/";
		// api.servers.add(server);

		Api mjApi = model instanceof Api ? (Api) model : null;
		
		MjModel mjModel = new MjModel(model.getEntityClasses());
		for (MjEntity entity : mjModel.entities) {
			if ((entity.getClazz().getModifiers() & Modifier.ABSTRACT) > 0) {
				continue;
			}
			String entityName = entity.getClassName();

			if (IdUtils.hasId(entity.getClazz()) && entity.type != MjEntityType.VIEW) {
				Map<String, Operation> operations = new LinkedHashMap<>();

				Operation operation = operationGetById(entity);
				operations.put("get", operation);
				
				if (mjApi == null || mjApi.canCreate(entity.getClazz())) {
					operation = operationPut(entity);
					operations.put("put", operation);
				}

				if (mjApi == null || mjApi.canDelete(entity.getClazz())) {
					operation = operationDelete(entity);
					operations.put("delete", operation);
				}

				api.paths.put("/" + entityName + "/{id}", operations);
				
				// 
				
				operations = new LinkedHashMap<>();
				
				if (entity.type == MjEntityType.CODE) {
					// the number of codes is expected to be small enough to be loaded at once
					operation = operationGetAll(entity);
					operations.put("get", operation);
				}
				
				if (mjApi == null || mjApi.canUpdate(entity.getClazz())) {
					operation = operationPost(entity);
					operations.put("post", operation);
				}
				
				if (!operations.isEmpty()) {
					api.paths.put("/" + entityName, operations);
				}
			}
			
			addSchema(api, mjApi, entity);	
		}
		
		if (mjApi != null) {
			for (var transaction : mjApi.getTransactions()) {
				Class<?> requestClass = transaction.request;
				addSchema(api, mjApi, new MjEntity(mjModel, requestClass));

				Class<?> responseClass = transaction.response;
				new MjEntity(mjModel, responseClass);
				addSchema(api, mjApi, new MjEntity(mjModel, responseClass));
				
				Map<String, Operation> operations = new LinkedHashMap<>();
				Operation operation = operationPost(transaction);

				operations.put("post", operation);
				api.paths.put("/" + transaction.clazz.getSimpleName(), operations);
			}
		}
		
		return api;
	}

	public void addSchema(OpenAPI api, Api mjApi, MjEntity entity) {
		String entityName = entity.getClassName();	
 		if (api.components.schemas.containsKey(entityName)) {
 			return;
		}
		
		if (entity.isEnumeration()) {
			api.components.schemas.put(entityName, eNum(entity));
		} else {
			api.components.schemas.put(entityName, schema(entity, mjApi));
			api.components.schemas.put(entityName + "_write", schemaWrite(mjApi, entity));
		}
	}


	private Operation operationGetById(MjEntity entity) {
		String entityName = entity.getClassName();
		
		Operation operation = new Operation();
		operation.summary = "Gets a " + entityName + " by id";
		
		Parameter parameter = parameter("id", Type.STRING);
		parameter.required = true;
		parameter.in = In.path;
		parameter.description = entityName + " id";
		
		operation.parameters.add(parameter);
		
		Response response = new Response();
		response.description = "Successful operation";
		
		Content content = new Content();
		content.schema = new Schema();
		content.schema.$ref = SCHEMAS + entity.getClassName();

		response.content.put("application/json", content);
		
		operation.responses.put("200", response);
		
		response = new Response();
		response.description = "Not found";
		
		operation.responses.put("404", response);
		
		return operation;
	}

	private Operation operationGetAll(MjEntity entity) {
		String entityName = entity.getClassName();

		Operation operation = new Operation();
		operation.summary = "Gets all " + entityName;

		addRangeParameters(operation);
		
		Response response = new Response();
		response.description = "Successful operation";

		Schema schema = new Schema();
		schema.type = OpenAPI.Type.ARRAY;
		
		schema.items = new Schema();
		schema.items.$ref = SCHEMAS + entityName;

		Content content = new Content();
		content.schema = schema;

		response.content.put("application/json", content);

		operation.responses.put("200", response);
		return operation;
	}

	private void addRangeParameters(Operation operation) {
		Parameter parameter = parameter("offset", Type.INTEGER);
		parameter.in = In.query;
		parameter.description = "First returned item (starting at 0)";
		operation.parameters.add(parameter);
		
		parameter = parameter("size", Type.INTEGER);
		parameter.in = In.query;
		parameter.description = "Number of maximal returned items";
		operation.parameters.add(parameter);
	}
	
	private Parameter parameter(String name, Type type) {
		Parameter parameter = new Parameter();
		parameter.name = name;
		Schema schema = new Schema();
		schema.type = type;
		parameter.schema = schema;
		return parameter;
	}
	
	private Operation operationPost(MjEntity entity) {
		String entityName = entity.getClassName();
		
		Operation operation = new Operation();
		operation.summary = "Add a new " + entityName;
		
		Content content = new Content();
		content.schema = new Schema();
		content.schema.$ref = SCHEMAS + entityName + "_write";
		
		RequestBody requestBody = new RequestBody();
		requestBody.content.put("application/json", content);

		operation.requestBody = requestBody;
		
		Response response = new Response();
		response.description = "Successful operation";
		
		operation.responses.put("200", response);
		return operation;
	}
	
	private Operation operationPost(TransactionDefinition transaction) {
		String transactionName = transaction.clazz.getSimpleName();
		
		Operation operation = new Operation();
		if (transaction.comment != null) {
			operation.summary = transaction.comment;
		} else {
			operation.summary = "Execute " + transactionName;
		}
		
		{
			Schema schema = new Schema();
			schema.$ref = SCHEMAS + transaction.request.getSimpleName() + "_write";

			Content content = new Content();
			content.schema = schema;

			RequestBody requestBody = new RequestBody();
			requestBody.content.put("application/json", content);

			operation.requestBody = requestBody;
			
		}

		if (transaction.listResponse) {
			addRangeParameters(operation);
		}

		{
			Class<?> resultClass = transaction.response;
			String responseEntityName = resultClass.getSimpleName();

			Response response = new Response();
			response.description = "Successful operation";

			Schema schemaResponse = new Schema();
			if (transaction.listResponse) {
				var schemaItems = new Schema();
				schemaItems.$ref = SCHEMAS + responseEntityName;
				schemaResponse.items = schemaItems;
				schemaResponse.type = Type.ARRAY;
			} else {
				schemaResponse.$ref = SCHEMAS + responseEntityName;
			}

			Content content = new Content();
			content.schema = schemaResponse;

			response.content.put("application/json", content);

			operation.responses.put("200", response);
		}

		return operation;
	}
	
	private Operation operationPut(MjEntity entity) {
		String entityName = entity.getClassName();
		
		Operation operation = new Operation();
		operation.summary = "Update a " + entityName;

		Parameter parameter = parameter("id", Type.STRING);
		parameter.required = true;
		parameter.in = In.path;
		parameter.description = entityName + " id";
		operation.parameters.add(parameter);

		Content content = new Content();
		content.schema = new Schema();
		content.schema.$ref = SCHEMAS + entityName + "_write";
		
		RequestBody requestBody = new RequestBody();
		requestBody.content.put("application/json", content);

		operation.requestBody = requestBody;
		
		Response response = new Response();
		response.description = "Successful operation";
		
		operation.responses.put("200", response);
		return operation;
	}
	
	private Operation operationDelete(MjEntity entity) {
		String entityName = entity.getClassName();
		
		Operation operation = new Operation();
		operation.summary = "Delete a " + entityName;

		Parameter parameter = parameter("id", Type.STRING);
		parameter.required = true;
		parameter.in = In.path;
		parameter.description = entityName + " id";
		
		operation.parameters.add(parameter);
		
		Response response = new Response();
		response.description = "Successful operation";
		
		operation.responses.put("200", response);
		return operation;
	}
	
	private Schema schema(MjEntity entity, Api mjApi) {
		Schema schema = new Schema();
		
		var idProperty = idProperty(entity);
		if (idProperty != null) {
			schema.properties.put("id", idProperty);
		}
		
		boolean hasVersion = FieldUtils.hasValidVersionfield(entity.getClazz());
		if (hasVersion) {
			var property = new Property();
			property.nullable = true;
			property.readOnly = true;
			property.type = OpenAPI.Type.INTEGER;
			schema.properties.put("version", property);
		}
		
		boolean historized = FieldUtils.hasValidHistorizedField(entity.getClazz());
		if (historized) {
			var property = new Property();
			property.nullable = true;
			property.readOnly = true;
			property.type = OpenAPI.Type.BOOLEAN;
			schema.properties.put("historized", property);
		}
		
		for (MjProperty mjProperty : entity.properties) {
			if (isParentProperty(entity, mjProperty)) {
				continue;
			}
			var property = property(mjProperty, mjApi, false);
			if (mjProperty.notEmpty) {
				schema.required.add(mjProperty.name);
			}
			schema.properties.put(mjProperty.name, property);
		}
		return schema;
	}
	
	private Schema schemaWrite(Api mjApi, MjEntity entity) {
		Schema schema = new Schema();
		
		for (MjProperty mjProperty : entity.properties) {
			if (mjProperty.technical != null) {
				continue;
			}
			var property = property(mjProperty, mjApi, true);
			if (mjProperty.notEmpty) {
				schema.required.add(mjProperty.name);
			}
			schema.properties.put(mjProperty.name, property);
		}
		return schema;
	}

	protected Property idProperty(MjEntity entity) {
		var idProperty = FlatProperties.getProperty(entity.getClazz(), "id", true);
		if (idProperty != null) {
			var property = new Property();
			property.nullable = false;
			if (idProperty.getClazz() == Integer.class || idProperty.getClazz() == Long.class) {
				property.type = OpenAPI.Type.INTEGER;
//				AutoIncrement autoIncrement = null;
//				try {
//					autoIncrement = idProperty.getAnnotation(AutoIncrement.class);
//				} catch (Exception x) {
//					System.out.println("AutoIncrement not working for: " + entity.getClassName());
//				}
//				if (autoIncrement == null || autoIncrement.value()) {
//					property.readOnly = true;
//				}
			} else {
				property.type = OpenAPI.Type.STRING;
			}
			return property;
		} else {
			return null;
		}
	}
	
	private boolean isParentProperty(MjEntity entity, MjProperty mjProperty) {
		if (Dependable.class.isAssignableFrom(entity.getClazz())) {
			var keys = (Dependable<?>) Keys.of(entity.getClazz());
			var property = Properties.getProperty(entity.getClazz(), mjProperty.name);
			return keys.getParent() == property.getValue(keys);
		} else {
			return false;
		}
	}
	
	protected Property property(MjProperty mjProperty, Api mjApi, boolean write) {
		var property = new Property();
		// property.nullable = mjProperty.notEmpty ? null : true; // nullable false is default, omit
		property.format = format(mjProperty);
		property.description = StringUtils.escapeHTML(mjProperty.comment);
		if (mjProperty.technical != null) {
			property.readOnly = true;
		}
		property.type = type(mjProperty, write);
		
		if (property.type == Type.ARRAY) {
			property.items = items(mjProperty, write);
		} else {
			if (mjProperty.type.isEnumeration()) {
				property.type = null;
				property.$ref = SCHEMAS + mjProperty.type.getClassName();
			}
			boolean primitiveOrCodeOrView = mjProperty.type.isPrimitiv() || Code.class.isAssignableFrom(mjProperty.type.getClazz()) || View.class.isAssignableFrom(mjProperty.type.getClazz());
			if (!write && !primitiveOrCodeOrView) {
				property.type = null;
				property.$ref = SCHEMAS + mjProperty.type.getClassName();
			}
			if (write && !primitiveOrCodeOrView) {
				if (IdUtils.hasId(mjProperty.type.getClazz()) && (mjApi == null || mjApi.canCreate(mjProperty.type.getClazz()))) {
					var schemaRef = new Schema();
					schemaRef.$ref = SCHEMAS + mjProperty.type.getClassName() + "_write";

					var schemaId = new Schema();
					schemaId.type = property.type;
					property.type = null;
					
					property.oneOf = List.of(schemaId, schemaRef);
				} else {
					property.$ref = SCHEMAS + mjProperty.type.getClassName() + "_write";
				}
			}
		}
		
		// it's not allowed to have $ref and description for same property:
		// https://github.com/OAI/OpenAPI-Specification/issues/556
		if (!StringUtils.isEmpty(property.description) && property.$ref != null) {
			var schemaRef = new Schema();
			schemaRef.$ref = property.$ref;
			property.oneOf = List.of(schemaRef);
			property.$ref = null;
		}
		
		if (mjProperty.type.type == MjEntityType.String) {
			property.maxLength = mjProperty.size;
		}
		return property;
	}
	
	private Schema eNum(MjEntity entity) {
		Schema schema = new Schema();

		schema.type = OpenAPI.Type.STRING;
		schema.eNum = entity.values;
		
		return schema;
	}
	
	private OpenAPI.Type type(MjProperty property, boolean write) {
		switch (property.propertyType) {
		case LIST:
		case ENUM_SET:
			return OpenAPI.Type.ARRAY;
		case INLINE:
		case DEPENDABLE:
			return null;
		case VALUE:
			if (property.type.type == MjEntityType.Integer || property.type.type == MjEntityType.Long) {
				return OpenAPI.Type.INTEGER;
			} else if (property.type.type == MjEntityType.Boolean) {
				return OpenAPI.Type.BOOLEAN;
			} else if (write || Code.class.isAssignableFrom(property.type.getClazz())) {
				var idClass = IdUtils.getIdClass(property.type.getClazz());
				if (idClass == Integer.class || idClass == Long.class) {
					return OpenAPI.Type.INTEGER;
				} else {
					return OpenAPI.Type.STRING;	
				}
			} else {
				return OpenAPI.Type.STRING;	
			}
		default: return null; // $ref will be used
		}
	}
	
	private String format(MjProperty property) {
		switch(property.type.type) {
		case String : return null;
		case Enum : return null;
		case Integer: return "int32";
		case Long: return "int64";
		case LocalDate: return "date";
		case LocalTime: return null; // local time does not exist in json
		case LocalDateTime: return "date-time";
		default: return null;
		}
	}

	private Schema items(MjProperty property, boolean write) {
//		if (property.propertyType == MjPropertyType.LIST && (property.type == null || IdUtils.hasId(property.type.getClazz()))) {
////		if (property.propertyType == MjPropertyType.LIST && IdUtils.hasId(property.type.getClazz())) {
//			Schema schema = new Schema();
//			schema.type = OpenAPI.Type.STRING;
//			return schema;
//		}
		if (property.propertyType == MjPropertyType.LIST || property.propertyType == MjPropertyType.ENUM_SET) {
			Schema schema = new Schema();
			schema.$ref = SCHEMAS + property.type.getClassName() + (write && property.propertyType != MjPropertyType.ENUM_SET ? "_write" : "");
			return schema;
		} else {
			return null;
		}
	}

}
