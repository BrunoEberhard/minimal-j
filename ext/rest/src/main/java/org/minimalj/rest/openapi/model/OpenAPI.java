package org.minimalj.rest.openapi.model;

import java.util.List;

import javax.print.attribute.standard.Media;
import javax.security.auth.callback.Callback;

import org.minimalj.model.annotation.NotEmpty;

public class OpenAPI {

	@NotEmpty
	public String openapi;
	public Info info;
	public List<Server> servers;
	public List<Path> paths;
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
		public List<ServerVariable> variables;
	}
	
	public static class ServerVariable {
		@NotEmpty
		public String name;
		public List<StringValue> enum_;
		@NotEmpty
		public String default_;
		public String description;
	}
	
	public static class StringValue {
		@NotEmpty
		public String value;
	}
	
	public static class Components {
		public List<Schema> schemas;
		public List<Response> responses;
		public List<Parameters> parameters;
		public List<Examples> examples;
		public List<requestBody> requestBodies;
		public List<Header> headers;
		public List<SecuritySchema> securitySchemes;
		public List<Link> links;
		public List<Callback> callbacks;
	}
	
	// PathItem
	public static class Path {
		public String path;
		public String ref_;
		public String summary;
		public String description;
		public Operation get_;
	}

	public static class Operation {
		public List<StringValue> tags;
		public String summary;
		public String description;
		public ExternalDocs externalDocs;
		public String operationId;
		public List<Parameter> parameters;
		
		public Boolean deprecated;
		public List<Server> servers;
	}
	
	public static class ExternalDocs {
		public String description;
		@NotEmpty
		public String url;
	}

	public static class Parameter {
		public String ref_;

		@NotEmpty
		public String name;
		@NotEmpty
		public In in;
		public String description;
		@NotEmpty
		public Boolean required;
		public Boolean deprecated, allowEmptyValue;
	}
	
	public static enum In {
		query, header, path, cookie;
	}
	
	public static class RequestBody {
		public String description;
		@NotEmpty
		public List<Media> content;
		public Boolean required;
	}
	
	public static class Example {
		public String summary;
		public String description;
		public Object value;
		public String externalValue;
	}

	public static class Schema {
		public String name;
		public List<StringValue> required;
		public List<Property> properties;
	}

	public static class Property {
		public String name;
		public Type type;
		public String format; // int32, int64, float, double, byte, binary, date, date-time, password, etc...
		public Schema items; // required for array Property
		public Boolean nullable;
		public String discriminator;
		public Boolean readOnly, writeOnly;
		public String xml;
		public ExternalDocs externalDocs;
		public Object example;
		public boolean deprecated;
	}
	
	public static enum Type {
		INTEGER, NUMBER, STRING, BOOLEAN;
	}
	
}
