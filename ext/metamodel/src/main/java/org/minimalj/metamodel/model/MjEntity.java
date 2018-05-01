package org.minimalj.metamodel.model;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.minimalj.model.Code;
import org.minimalj.model.EnumUtils;
import org.minimalj.model.Keys;
import org.minimalj.model.annotation.Materialized;
import org.minimalj.model.annotation.Searched;
import org.minimalj.model.validation.Validation;
import org.minimalj.util.FieldUtils;
import org.minimalj.util.IdUtils;
import org.minimalj.util.StringUtils;
import org.w3c.dom.Element;

public class MjEntity {

	public enum MjEntityType {
		ENTITY, HISTORIZED_ENTITY, DEPENDING_ENTITY, CODE,
		
		// primitives
		String(String.class), Integer(Integer.class), Long(Long.class), Boolean(Boolean.class), //
		BigDecimal(BigDecimal.class), LocalDate(LocalDate.class), //
		LocalTime(LocalTime.class), LocalDateTime(LocalDateTime.class), ByteArray(byte[].class);
		
		private MjEntityType() {
			javaClass = null;
		}

		private MjEntityType(Class<?> javaClass) {
			this.javaClass = javaClass;
		}

		private Class<?> javaClass;
		
		public Class<?> getJavaClass() {
			return javaClass;
		}
		
		public boolean isPrimitiv() {
			return javaClass != null;
		}
	}
	
	public static final MjEntity $ = Keys.of(MjEntity.class);
	
	public Object id;

	public MjEntityType type;

	private Class<?> clazz;
	public String name; // classname without package
	public String packageName;

	public Boolean validatable;
	public final List<MjProperty> properties = new ArrayList<>();

	// restrictions
	public List<String> values; // only for enum
	public String minInclusive, maxInclusive; // only for int / long / temporals
	public Integer minLength, maxLength; // only for string / bigDecimal / byte[]
	
	public MjEntity() {
	}
	
	public MjEntity(MjEntityType type) {
		this(null, type);
	}
	
	public MjEntity(MjModel model, MjEntityType type) {
		this.type = Objects.requireNonNull(type);
		this.clazz = type.getJavaClass();
		if (clazz != null) {
			this.name = clazz.getSimpleName();
		}
	}
	
	public MjEntity(MjModel model, Class<?> clazz) {
		this.clazz = clazz;
		this.name = clazz.getSimpleName();
		this.packageName = clazz.getPackage().getName();
		
		model.addEntity(this);
		validatable = Validation.class.isAssignableFrom(clazz);
		
		Field[] fields = clazz.getFields();
		for (Field field : fields) {
			if (FieldUtils.isPublic(field) && !FieldUtils.isStatic(field) && !FieldUtils.isTransient(field) && !StringUtils.equals(field.getName(), "id", "version", "historized")) {
				properties.add(new MjProperty(model, field));
			}
		}
		Method[] methods = clazz.getMethods();
		for (Method method: methods) {
			if (!Keys.isPublic(method) || Keys.isStatic(method)) continue;
			if (method.getAnnotation(Searched.class) == null && method.getAnnotation(Materialized.class) == null) continue;
			String methodName = method.getName();
			if (!methodName.startsWith("get") || methodName.length() < 4) continue;
			properties.add(new MjProperty(model, method));
		}
		
		if (Enum.class.isAssignableFrom(clazz)) {
			type = MjEntityType.String;
			List<Object> list = EnumUtils.valueList((Class<Enum>) clazz);
			values = list.stream().map(e -> e.toString()).collect(Collectors.toList());
		} else if (Code.class.isAssignableFrom(clazz)) {
			type = MjEntityType.CODE;
		} else if (IdUtils.hasId(clazz)) {
			type = MjEntityType.ENTITY;
		} else {
			type = MjEntityType.DEPENDING_ENTITY;
		}
	}
	
	public MjEntity(String name) {
		this.name = name;
		this.type = MjEntityType.ENTITY;
	}
	
	public Class<?> getClazz() {
		if (clazz == null) {
			throw new IllegalStateException(name + " is not a java class");
		}
		return clazz;
	}
	
	public String getClassName() {
		if (Keys.isKeyObject(this)) return Keys.methodOf(this, "className");
		
		if (!isEnumeration() && type.getJavaClass() != null) {
			return type.getJavaClass().getSimpleName();
		} else {
			return name;
		}
	}
	
	public boolean isEnumeration() {
		return values != null && !values.isEmpty();
	}

	public boolean isPrimitiv() {
		return type.isPrimitiv();
	}
	
	public String getSimpleClassName() {
		return name;
	}
	
	// TOOD move
	
	private Element element;
	
	public Element getElement() {
		return element;
	}
	
	public void setElement(Element element) {
		this.element = element;
	}
}
