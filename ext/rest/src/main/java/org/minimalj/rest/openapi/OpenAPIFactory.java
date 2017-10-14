package org.minimalj.rest.openapi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.minimalj.application.Application;
import org.minimalj.metamodel.model.MjEntity;
import org.minimalj.metamodel.model.MjModel;
import org.minimalj.metamodel.model.MjProperty;
import org.minimalj.metamodel.model.MjProperty.MjPropertyType;
import org.minimalj.model.EnumUtils;
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
			String entityName = entity.getClazz().getSimpleName();

			if (IdUtils.hasId(entity.getClazz())) {
				Map<String, Operation> operations = new HashMap<>();
				Operation operation = operationGetById(entity);
				operations.put("get", operation);
				api.paths.put("/" + entityName + "/{id}", operations);
				
				operations = new HashMap<>();
				
				operation = operationGetAll(entity);
				operations.put("get", operation);
				
				operation = operationPut(entity);
				operations.put("put", operation);
				
				api.paths.put("/" + entityName, operations);
			}
			
			Schema schema;
			if (Enum.class.isAssignableFrom(entity.getClazz())) {
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
		
		EntityJsonWriter writer = new EntityJsonWriter();
		return writer.write(api);
	}


	private Operation operationGetById(MjEntity entity) {
		String entityName = entity.getClazz().getSimpleName();
		
		Operation operation = new Operation();
		operation.summary = "Gets a " + entityName + " by id";
		
		Parameter parameter = new Parameter();
		parameter.name = "id";
		parameter.required = true;
		parameter.in = In.path;
		parameter.description = entityName + " id";
		
		if (api == API.OpenAPI3) {
			Schema schema = new Schema();
			schema.type = Type.STRING;
			parameter.schema = schema;
		} else {
			parameter.type = "string";
		}
		
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
		return operation;
	}


	private Operation operationGetAll(MjEntity entity) {
		String entityName = entity.getClazz().getSimpleName();

		Operation operation = new Operation();
		operation.summary = "Gets all " + entityName;

		Response response = new Response();
		response.description = "Successful operation";

		if (api == API.OpenAPI3) {
			Schema schema = new Schema();
			schema.type = OpenAPI.Type.ARRAY;
			schema.$ref = SCHEMAS + entityName;

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
	
	private Operation operationPut(MjEntity entity) {
		String entityName = entity.getClazz().getSimpleName();
		
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
	
	private Schema schema(MjEntity entity) {
		Schema schema = new Schema();

		Property property = new Property();
		if (api == API.OpenAPI3 ) {
			property.nullable = true;
		}
		schema.required.add("id");
		property.type = OpenAPI.Type.STRING;
		schema.properties.put("id", property);
		
		boolean hasVersion = FieldUtils.hasValidVersionfield(entity.getClazz());
		if (hasVersion) {
			property = new Property();
			if (api == API.OpenAPI3 ) {
				property.nullable = true;
			}
			schema.required.add("version");
			property.type = OpenAPI.Type.INTEGER;
			schema.properties.put("version", property);
		}
		
		boolean historized = FieldUtils.hasValidHistorizedField(entity.getClazz());
		if (historized) {
			property = new Property();
			if (api == API.OpenAPI3) {
				property.nullable = true;
			}
			schema.required.add("historized");
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
			property.type = type(mjProperty);
			
			if (api == API.OpenAPI3) {
				property.$ref = ref(mjProperty);
				if (mjProperty.propertyType == MjPropertyType.ENUM) {
					property.type = null;
					property.$ref = SCHEMAS + mjProperty.type.getClazz().getSimpleName();
				}
					
			} else {
				if (property.type == Type.ARRAY) {
					property.items = schema(mjProperty.type);
				} else if (mjProperty.propertyType == MjPropertyType.ENUM) {
					// OpenApi3 has reusable enums
					property.eNum = new ArrayList<>();
					for (Object e : EnumUtils.valueList((Class<? extends Enum>) mjProperty.type.getClazz())) {
						property.eNum.add(e.toString());
					}
				} else {
					property.$ref = ref(mjProperty);
				}
				if (property.$ref != null) {
					property.type = null;
				}
			}
			
			property.format = format(mjProperty);
			property.items = items(mjProperty);
			schema.properties.put(mjProperty.name, property);
		}
		return schema;
	}
	
	// V3
	private Schema eNum(MjEntity entity) {
		Schema schema = new Schema();

		schema.type = OpenAPI.Type.STRING;
		List value = EnumUtils.valueList((Class<? extends Enum>) entity.getClazz());
		schema.eNum = (List<String>) value.stream().map(e -> e.toString()).collect(Collectors.toList());
		
		return schema;
	}
	
	private OpenAPI.Type type(MjProperty property) {
		switch (property.propertyType) {
		case String:
		case LocalDate:
		case LocalDateTime:
		case LocalTime:
		case REFERENCE:
			return OpenAPI.Type.STRING;
		case Integer:
		case Long:
			return OpenAPI.Type.INTEGER;
		case LIST:
		case ENUM_SET:
			return OpenAPI.Type.ARRAY;
		case ENUM:
			return OpenAPI.Type.STRING;
		case INLINE:
		case DEPENDABLE:
			return OpenAPI.Type.OBJECT;

		default: return null;
		}
	}
	
	private String ref(MjProperty property) {
		if (property.propertyType == MjPropertyType.INLINE ||property.propertyType == MjPropertyType.DEPENDABLE) {
			return SCHEMAS + property.type.getClazz().getSimpleName();
		} else {
			return null;
		}
	}
	
	private String format(MjProperty property) {
		switch(property.propertyType) {
		case String : return null;
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
			schema.$ref = SCHEMAS + property.type.getClazz().getSimpleName();
			return schema;
		} else {
			return null;
		}
	}

}
