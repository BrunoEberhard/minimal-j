package org.minimalj.rest.openapi.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.print.attribute.standard.Media;
import javax.security.auth.callback.Callback;

import org.minimalj.model.annotation.NotEmpty;

public class OpenAPI {

	@NotEmpty
	public String openapi;
	public Info info;
	public List<Server> servers = new ArrayList<>();
	public Map<String, Map<String, Operation>> paths = new LinkedHashMap<>();
	public Components components;
	
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
//		public List<Response> responses = new ArrayList<>();
		public Map<String, Schema> parameters = new LinkedHashMap<>();
//		public List<Examples> examples = new ArrayList<>();
//		public List<requestBody> requestBodies = new ArrayList<>();
//		public List<Header> headers = new ArrayList<>();
//		public List<SecuritySchema> securitySchemes = new ArrayList<>();
//		public List<Link> links = new ArrayList<>();
		public List<Callback> callbacks = new ArrayList<>();
	}
	
	// PathItem
//	public static class Path {
//		public Map<String, Operation> operations = new LinkedHashMap<>();
//	}

	public static class Operation {
		public List<StringValue> tags = new ArrayList<>();
		public String summary;
		public String description;
		public ExternalDocs externalDocs;
		public String operationId;
		public List<Parameter> parameters = new ArrayList<>();
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
		public String ref;

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
	
	public static class Response {
		public String description;
		public Map<String, Content> content = new LinkedHashMap<>();
	}
	
	public static class Content {
		public Schema schema;
	}
	
	public static enum In {
		query, header, path, cookie;
	}
	
	public static class RequestBody {
		public String description;
		@NotEmpty
		public List<Media> content = new ArrayList<>();
		public Boolean required;
	}
	
	public static class Example {
		public String summary;
		public String description;
		public Object value;
		public String externalValue;
	}

	public static class Schema {
		public Type type;
		public String name;
		public List<StringValue> required = new ArrayList<>();
		public Map<String, Property> properties = new LinkedHashMap<>();
		public String $ref;
	}

	public static class Property {
		public Type type;
		public String format; // int32, int64, float, double, byte, binary, date, date-time, password, etc...
		public Schema items; // required for array Property
		public String $ref;
		public Boolean nullable;
		public String discriminator;
		public Boolean readOnly, writeOnly;
		public String xml;
		public ExternalDocs externalDocs;
		public Object example;
		public Boolean deprecated;
	}
	
	public static enum Type {
		INTEGER, NUMBER, STRING, BOOLEAN, ARRAY, OBJECT;
	}
	
    
}
