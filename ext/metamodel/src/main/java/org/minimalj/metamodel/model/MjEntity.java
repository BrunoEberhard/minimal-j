package org.minimalj.metamodel.model;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.minimalj.model.Code;
import org.minimalj.model.EnumUtils;
import org.minimalj.model.Keys;
import org.minimalj.model.annotation.Materialized;
import org.minimalj.model.annotation.Searched;
import org.minimalj.model.annotation.Size;
import org.minimalj.model.validation.Validation;
import org.minimalj.util.FieldUtils;
import org.minimalj.util.IdUtils;
import org.minimalj.util.StringUtils;

public class MjEntity {

	public enum MjEntityType {
		ENTITY, HISTORIZED_ENTITY, DEPENDING_ENTITY, CODE, ENUM,
		
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
	}
	
	public static final MjEntity $ = Keys.of(MjEntity.class);
	
	public Object id;
	@Size(1024)
	public String name;
	public MjEntityType type;
	public Boolean validatable;
	public final List<MjProperty> properties = new ArrayList<>();

	private MjModel model;
	private Class<?> clazz;
	private String simpleClassName;
	
	// restrictions
	private List<String> values; // only for enum
	private String minInclusive, maxInclusive; // only for int / long / temporals
	public Integer minLength, maxLength; // only for string / bigDecimal / byte[]
	
	public MjEntity() {
		//
	}
	
	MjEntity(MjModel model, MjEntityType type) {
		this.model = model;
		this.clazz = type.getJavaClass();
		this.name = clazz.getName();
		this.simpleClassName = clazz.getSimpleName();
	}
	
	public MjEntity(MjModel model, Class<?> clazz) {
		this.model = model;
		this.clazz = clazz;
		this.simpleClassName = clazz.getSimpleName();
		
		model.addEntity(this);
		name = clazz.getName();
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
			type = MjEntityType.ENUM;
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
	
	public Class<?> getClazz() {
		if (clazz == null) {
			throw new IllegalStateException(name + " is not a java class");
		}
		return clazz;
	}
	
	public String getClassName() {
		return name;
	}
	
	public String getSimpleClassName() {
		return simpleClassName;
	}
	
	public List<String> getValues() {
		if (type != MjEntityType.ENUM) {
			throw new IllegalArgumentException();
		}
		return values;
	}
}
