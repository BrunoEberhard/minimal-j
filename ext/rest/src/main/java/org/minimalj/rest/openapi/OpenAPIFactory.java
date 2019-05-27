package org.minimalj.rest.openapi;

import java.util.HashMap;
import java.util.Map;

import org.minimalj.application.Application;
import org.minimalj.metamodel.model.MjEntity;
import org.minimalj.metamodel.model.MjEntity.MjEntityType;
import org.minimalj.metamodel.model.MjModel;
import org.minimalj.metamodel.model.MjProperty;
import org.minimalj.metamodel.model.MjProperty.MjPropertyType;
import org.minimalj.rest.EntityJsonWriter;
import org.minimalj.rest.openapi.model.OpenAPI;
import org.minimalj.rest.openapi.model.OpenAPI.Content;
import org.minimalj.rest.openapi.model.OpenAPI.In;
import org.minimalj.rest.openapi.model.OpenAPI.Operation;
import org.minimalj.rest.openapi.model.OpenAPI.Parameter;
import org.minimalj.rest.openapi.model.OpenAPI.Property;
import org.minimalj.rest.openapi.model.OpenAPI.RequestBody;
import org.minimalj.rest.openapi.model.OpenAPI.Response;
import org.minimalj.rest.openapi.model.OpenAPI.Schema;
import org.minimalj.rest.openapi.model.OpenAPI.Server;
import org.minimalj.rest.openapi.model.OpenAPI.Type;
import org.minimalj.util.FieldUtils;
import org.minimalj.util.IdUtils;

public class OpenAPIFactory {
	
	public static enum API {
		Swagger, OpenAPI3;
	}
	
	private final API api;
	private final String SCHEMAS;
	
	public OpenAPIFactory() {
		this(API.OpenAPI3);
	}
	
	public OpenAPIFactory(API api) {
		this.api = api;
		if (api == API.Swagger) {
			SCHEMAS = "#/definitions/";
		} else {
			SCHEMAS = "#/components/schemas/";
		}
	}

	public String create(Application application) {
		OpenAPI api = new OpenAPI();
		if (this.api == API.OpenAPI3) {
			api.openapi = "3.0.0";
			api.components = new OpenAPI.Components();
		} else {
			api.swagger = "2.0";
		}
		
		api.info = new OpenAPI.Info();
		api.info.version = "1.0.0";
		api.info.title = application.getName();
		
		Server server = new Server();
		server.url = "http://localhost:8090/";
		api.servers.add(server);

		MjModel model = new MjModel(application.getEntityClasses());
		for (MjEntity entity : model.entities) {
			String entityName = entity.getSimpleClassName();

			if (IdUtils.hasId(entity.getClazz())) {
				Map<String, Operation> operations = new HashMap<>();

				Operation operation = operationGetById(entity);
				operations.put("get", operation);
				
				operation = operationPut(entity);
				operations.put("put", operation);

				operation = operationDelete(entity);
				operations.put("delete", operation);

				api.paths.put("/" + entityName + "/{id}", operations);
				
				// 
				
				operations = new HashMap<>();
				
				if (entity.type != MjEntityType.CODE) {
					// the number of codes is expected to be small enough to be loaded at once
					operation = operationGetAll(entity);
					operations.put("get", operation);
				}
				
				operation = operationPost(entity);
				operations.put("post", operation);
				
				api.paths.put("/" + entityName, operations);
			}
			
			Schema schema;
			if (entity.isEnumeration()) {
				if (this.api == API.OpenAPI3) {
					schema = eNum(entity);
				} else {
					// v2 has no reusable enum
					continue;
				}
			} else {
				schema = schema(entity);
			}
			
			if (this.api == API.OpenAPI3) {
				api.components.schemas.put(entityName, schema);
			} else {
				api.definitions.put(entityName, schema);
			}	
		}
		
		return EntityJsonWriter.write(api);
	}


	private Operation operationGetById(MjEntity entity) {
		String entityName = entity.getSimpleClassName();
		
		Operation operation = new Operation();
		operation.summary = "Gets a " + entityName + " by id";
		
		Parameter parameter = stringParameter("id");
		parameter.required = true;
		parameter.in = In.path;
		parameter.description = entityName + " id";
		
		operation.parameters.add(parameter);
		
		Response response = new Response();
		response.description = "Successful operation";
		
		Schema schema = new Schema();
		schema.$ref = SCHEMAS + entityName;

		if (api == API.OpenAPI3) {
			Content content = new Content();
			content.schema = schema;

			response.content.put("application/json", content);
		} else {
			response.schema = schema;
		}
		
		operation.responses.put("200", response);
		
		response = new Response();
		response.description = "Not found";
		
		operation.responses.put("404", response);
		
		return operation;
	}


	private Operation operationGetAll(MjEntity entity) {
		String entityName = entity.getSimpleClassName();

		Operation operation = new Operation();
		operation.summary = "Gets all " + entityName;

		Parameter parameter = stringParameter("offset");
		parameter.in = In.query;
		parameter.description = "First returned item (starting at 0)";
		operation.parameters.add(parameter);
		
		parameter = stringParameter("size");
		parameter.in = In.query;
		parameter.description = "Number of maximal returned items";
		operation.parameters.add(parameter);
		
		Response response = new Response();
		response.description = "Successful operation";

		if (api == API.OpenAPI3) {
			Schema schema = new Schema();
			schema.type = OpenAPI.Type.ARRAY;

			schema.items = new Schema();
			schema.items.$ref = SCHEMAS + entityName;

			Content content = new Content();
			content.schema = schema;

			response.content.put("application/json", content);
		} else {
			Schema schema = new Schema();
			schema.type = OpenAPI.Type.ARRAY;
			Schema itemSchema = new Schema();
			itemSchema.$ref = SCHEMAS + entityName;
			schema.items = itemSchema;

			response.schema = schema;
		}

		operation.responses.put("200", response);
		return operation;
	}
	
	private Parameter stringParameter(String name) {
		Parameter parameter = new Parameter();
		parameter.name = name;
		if (api == API.OpenAPI3) {
			Schema schema = new Schema();
			schema.type = Type.STRING;
			parameter.schema = schema;
		} else {
			parameter.type = "string";
		}
		return parameter;
	}
	
	private Operation operationPost(MjEntity entity) {
		String entityName = entity.getSimpleClassName();
		
		Operation operation = new Operation();
		operation.summary = "Add a new " + entityName;
		
		Schema schema = new Schema();
		schema.$ref = SCHEMAS + entityName;
		
		if (api == API.OpenAPI3) {
			Content content = new Content();
			content.schema = schema;
			
			RequestBody requestBody = new RequestBody();
			requestBody.content.put("application/json", content);

			operation.requestBody = requestBody;
		} else {
			// TODO response.schema = schema;
		}
		
		Response response = new Response();
		response.description = "Successful operation";
		
		operation.responses.put("200", response);
		return operation;
	}
	
	private Operation operationPut(MjEntity entity) {
		String entityName = entity.getSimpleClassName();
		
		Operation operation = new Operation();
		operation.summary = "Update a " + entityName;

		Parameter parameter = stringParameter("id");
		parameter.required = true;
		parameter.in = In.path;
		parameter.description = entityName + " id";
		operation.parameters.add(parameter);

		Schema schema = new Schema();
		schema.$ref = SCHEMAS + entityName;
		
		if (api == API.OpenAPI3) {
			Content content = new Content();
			content.schema = schema;
			
			RequestBody requestBody = new RequestBody();
			requestBody.content.put("application/json", content);

			operation.requestBody = requestBody;
		} else {
			// TODO response.schema = schema;
		}
		
		Response response = new Response();
		response.description = "Successful operation";
		
		operation.responses.put("200", response);
		return operation;
	}
	
	private Operation operationDelete(MjEntity entity) {
		String entityName = entity.getSimpleClassName();
		
		Operation operation = new Operation();
		operation.summary = "Delete a " + entityName;

		Parameter parameter = stringParameter("id");
		parameter.required = true;
		parameter.in = In.path;
		parameter.description = entityName + " id";
		
		operation.parameters.add(parameter);
		
		Response response = new Response();
		response.description = "Successful operation";
		
		operation.responses.put("200", response);
		return operation;
	}
	
	private Schema schema(MjEntity entity) {
		Schema schema = new Schema();

		Property property = new Property();
		if (api == API.OpenAPI3 ) {
			property.nullable = true;
		}
		property.type = OpenAPI.Type.STRING;
		if (entity.type != MjEntityType.CODE) {
			// for codes the id can be chosen. For normal entites the framework creates the id
			property.readOnly = true;
		}
		schema.properties.put("id", property);
		
		boolean hasVersion = FieldUtils.hasValidVersionfield(entity.getClazz());
		if (hasVersion) {
			property = new Property();
			if (api == API.OpenAPI3 ) {
				property.nullable = true;
			}
			property.readOnly = true;
			property.type = OpenAPI.Type.INTEGER;
			schema.properties.put("version", property);
		}
		
		boolean historized = FieldUtils.hasValidHistorizedField(entity.getClazz());
		if (historized) {
			property = new Property();
			if (api == API.OpenAPI3) {
				property.nullable = true;
			}
			property.readOnly = true;
			property.type = OpenAPI.Type.BOOLEAN;
			schema.properties.put("historized", property);
		}
		
		for (MjProperty mjProperty : entity.properties) {
			property = new Property();
			if (api == API.OpenAPI3 ) {
				property.nullable = mjProperty.notEmpty ? null : true; // nullable false is default, omit
			}
			if (mjProperty.notEmpty) {
				schema.required.add(mjProperty.name);
			}
			if (mjProperty.technical) {
				property.readOnly = true;
			}
			property.type = type(mjProperty);
			
			if (api == API.OpenAPI3) {
				if (property.type == Type.ARRAY) {
					property.items = items(mjProperty);
				} else if (mjProperty.type.isEnumeration()) {
					property.type = null;
					property.$ref = SCHEMAS + mjProperty.type.getSimpleClassName();
				} else {
					property.$ref = ref(mjProperty);
				}
			} else {
				if (property.type == Type.ARRAY) {
					property.items = schema(mjProperty.type);
				} else if (mjProperty.type.isEnumeration()) {
					// OpenApi3 has reusable enums, swagger has not
					property.eNum = mjProperty.type.values;
				} else {
					property.$ref = ref(mjProperty);
				}
				if (property.$ref != null) {
					property.type = null;
				}
			}
			
			property.format = format(mjProperty);

			schema.properties.put(mjProperty.name, property);
		}
		return schema;
	}
	
	// V3
	private Schema eNum(MjEntity entity) {
		Schema schema = new Schema();

		schema.type = OpenAPI.Type.STRING;
		schema.eNum = entity.values;
		
		return schema;
	}
	
	private OpenAPI.Type type(MjProperty property) {
		switch (property.propertyType) {
		case LIST:
		case ENUM_SET:
			return OpenAPI.Type.ARRAY;
		case INLINE:
		case DEPENDABLE:
			return OpenAPI.Type.OBJECT;
		case VALUE:
			if (property.type.type == MjEntityType.Integer || property.type.type == MjEntityType.Long) {
				return OpenAPI.Type.INTEGER;
			} else {
				return OpenAPI.Type.STRING;	
			}
		default: return null;
		}
	}
	
	private String ref(MjProperty property) {
		if (!property.type.isPrimitiv()) {
			return SCHEMAS + property.type.getSimpleClassName();
		} else {
			return null;
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

	private Schema items(MjProperty property) {
		if (property.propertyType == MjPropertyType.LIST && IdUtils.hasId(property.type.getClazz())) {
			Schema schema = new Schema();
			schema.type = OpenAPI.Type.STRING;
			return schema;
		}
		if (property.propertyType == MjPropertyType.LIST || property.propertyType == MjPropertyType.ENUM_SET) {
			Schema schema = new Schema();
			schema.$ref = SCHEMAS + property.type.getSimpleClassName();
			return schema;
		} else {
			return null;
		}
	}

}
