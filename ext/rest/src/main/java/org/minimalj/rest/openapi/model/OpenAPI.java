package org.minimalj.rest.openapi.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.security.auth.callback.Callback;

import org.minimalj.model.annotation.NotEmpty;
import org.minimalj.util.StringUtils;

public class OpenAPI {

	@NotEmpty
	public String openapi;
	public Info info;
	public List<Server> servers = new ArrayList<>();
	public List<Tag> tags = new ArrayList<>();
	public Map<String, Map<String, Operation>> paths = new LinkedHashMap<>();
	public Components components = new Components();
	
	public static class Info {
		@NotEmpty
		public String title;
		public String description;
		public String termsOfService;
		public Contact contact;
		public License license;
		@NotEmpty
		public String version;
	}

	public static class Tag {
		public String name;
		public String description;
		public ExternalDocs externalDocs;
	}
	
	public static class Contact {
		public String name;
		public String url;
		public String email;
	}
	
	public static class License {
		public String name;
		public String url;
	}
	
	public static class Server {
		@NotEmpty
		public String url;
		public String description;
		public List<ServerVariable> variables = new ArrayList<>();
	}
	
	public static class ServerVariable {
		@NotEmpty
		public String name;
		public List<StringValue> enum_ = new ArrayList<>();
		@NotEmpty
		public String default_;
		public String description;
	}
	
	public static class StringValue {
		@NotEmpty
		public String value;
	}
	
	public static class Components {
		public Map<String, Schema> schemas = new LinkedHashMap<>();
		public Map<String, Response> responses = new LinkedHashMap<>();
		public Map<String, Parameter> parameters = new LinkedHashMap<>();
//		public List<Examples> examples = new ArrayList<>();
//		public List<requestBody> requestBodies = new ArrayList<>();
		public Map<String, Header> headers = new LinkedHashMap<>();
//		public List<SecuritySchema> securitySchemes = new ArrayList<>();
//		public List<Link> links = new ArrayList<>();
		public List<Callback> callbacks = new ArrayList<>();
	}

	public static class Operation {
		public List<StringValue> tags = new ArrayList<>();
		public String summary;
		public String description;
		public ExternalDocs externalDocs;
		public String operationId;
		public List<Parameter> parameters = new ArrayList<>();
		public RequestBody requestBody;
		public Map<String, Response> responses = new LinkedHashMap<>(); 
		
		public Boolean deprecated;
		public List<Server> servers;
	}
	
	public static class ExternalDocs {
		public String description;
		@NotEmpty
		public String url;
	}

	public static class Parameter {
		public String $ref;

		@NotEmpty
		public String name;
		@NotEmpty
		public In in;
		public String description;
		@NotEmpty
		public Boolean required;
		public Boolean deprecated, allowEmptyValue;
		
		public String type;
		public String format;
		
		public Schema schema; // content not supported at the moment
	}
	
	public static class RequestBody {
		public String description;
		public Boolean required;
		public Map<String, Content> content = new LinkedHashMap<>();
	}
	
	public static class Response {
		public String $ref;

		public String description;
		public Map<String, Header> headers = new LinkedHashMap<>();
		public Map<String, Content> content = new LinkedHashMap<>();

		// v2 / !v3
		public Schema schema;
	}
	
	public static class Content {
		public Schema schema;
	}
	
	public static enum In {
		query, header, path, cookie;
	}
	
	public static class Example {
		public String summary;
		public String description;
		public Object value;
		public String externalValue;
	}
	
	public static class Header {
		public String $ref;

		public String description;
		public Schema schema;
	}

	public static class Schema {
		public String $ref;

		public Type type;
		public String name;
		public String title, description;
		public List<String> required = new ArrayList<>();
		public Integer maxLength; // for type : STRING
		public Map<String, Property> properties = new LinkedHashMap<>();
		public Object example;
		
		// V2 / V3 for arrays
		public Schema items;
		// V3
		public List<String> eNum;
	}

	public static class Property {
		public String $ref;

		public String title, description;
		public Type type;
		public Integer minLength, maxLength; // for type : STRING
		public Integer maxItems; // for type : ARRAY
		public String format; // int32, int64, float, double, byte, binary, date, date-time, password, etc...
		public Schema items; // required for array Property
		public Boolean nullable;
		public String discriminator;
		public Boolean readOnly, writeOnly;
		public String xml;
		public ExternalDocs externalDocs;
		public Object example;
		public Boolean deprecated;
		public String pattern;
		
		// V2
		public List<String> eNum;
		
		public String getComment() {
			if (!StringUtils.isEmpty(title)) {
				if (example != null) {
					return title + ", example: " + example;
				} else {
					return title;
				}
			} else if (example != null) {
				return "example: " + example;
			}
			return null;
		}
	}
	
	public static enum Type {
		INTEGER, NUMBER, STRING, BOOLEAN, ARRAY, OBJECT;
	}
	
	// V2
	
	public String getSwagger() {
		return openapi;
	}
	
	public void setSwagger(String swagger) {
		this.openapi = swagger;
	}
	
	public Map<String, Schema> getDefinitions() {
		return components.schemas;
	}
	
	public void setDefinitions(Map<String, Schema> definitions) {
		components.schemas = definitions;
	}
    
}
