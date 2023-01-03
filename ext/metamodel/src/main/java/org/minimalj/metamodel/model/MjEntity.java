package org.minimalj.metamodel.model;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.minimalj.model.Code;
import org.minimalj.model.EnumUtils;
import org.minimalj.model.Keys;
import org.minimalj.model.View;
import org.minimalj.model.annotation.Materialized;
import org.minimalj.model.annotation.Searched;
import org.minimalj.model.validation.Validation;
import org.minimalj.util.FieldUtils;
import org.minimalj.util.IdUtils;
import org.minimalj.util.StringUtils;

public class MjEntity {

	public enum MjEntityType {
		ENTITY, HISTORIZED_ENTITY, DEPENDING_ENTITY, CODE, VIEW,
		
		// primitives
		String(String.class), Integer(Integer.class), Long(Long.class), Boolean(Boolean.class), //
		Enum(Enum.class), //
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
	
	public static final Map<Class<?>, MjEntity> PRIMITIVES = Arrays.stream(MjEntity.MjEntityType.values())
			.filter(MjEntityType::isPrimitiv)
			.collect(Collectors.toMap(MjEntityType::getJavaClass, t -> new MjEntity(t)));
	
	public static final MjEntity $ = Keys.of(MjEntity.class);
	
	public Object id;

	public MjEntityType type;

	private final Class<?> clazz;

	public MjEntity viewedEntity;

	public Boolean validatable;
	public final List<MjProperty> properties = new ArrayList<>();

	// restrictions
	public List<String> values; // only for enum
	public String minInclusive, maxInclusive; // only for int / long / temporals
	public Integer minLength, maxLength; // only for string / bigDecimal / byte[]
	
	public MjEntity() {
		// nur für Keys - Klasse
		this.clazz = null;
	}

	public MjEntity(MjEntityType type) {
		this.type = Objects.requireNonNull(type);
		this.clazz = type.getJavaClass();
	}
	
	public MjEntity(MjModel model, Class<?> clazz) {
        this.clazz = Objects.requireNonNull(clazz);
		
		model.addEntity(this);
		validatable = Validation.class.isAssignableFrom(clazz);
		
		Field[] fields = clazz.getFields();
		for (Field field : fields) {
			if (FieldUtils.isPublic(field) && !FieldUtils.isStatic(field) && !FieldUtils.isTransient(field) && !StringUtils.equals(field.getName(), "id", "version", "historized")) {
				properties.add(new MjProperty(model, clazz, field));
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
			type = MjEntityType.Enum;
			List<Object> list = EnumUtils.valueList((Class) clazz);
			values = list.stream().map(e -> e.toString()).map(MjEntity::cutUnderlinePrefix).collect(Collectors.toList());

		} else if (Code.class.isAssignableFrom(clazz)) {
			type = MjEntityType.CODE;
		} else if (View.class.isAssignableFrom(clazz)) {
			type = MjEntityType.VIEW;
		} else if (IdUtils.hasId(clazz)) {
			type = MjEntityType.ENTITY;
		} else {
			type = MjEntityType.DEPENDING_ENTITY;
		}
	}
	
	private static String cutUnderlinePrefix(String s) {
		if (s.startsWith("_")) {
			return s.substring(1);
		} else {
			return s;
		}
	}
	
	public String getClassName() {
		if (Keys.isKeyObject(this)) return Keys.methodOf(this, "className");
		
		if (!isEnumeration() && type.getJavaClass() != null) {
			return type.getJavaClass().getSimpleName();
		} else {
			return clazz.getSimpleName();
		}
	}
	
	public String getPackageName() {
		return clazz.getPackage().getName();
	}

	public boolean isEnumeration() {
		return values != null && !values.isEmpty();
	}

	public boolean isPrimitiv() {
        if (type == null) {
            System.out.println("Null type: " + getClassName());
        }
		return type.isPrimitiv();
	}

	public Class<?> getClazz() {
		return clazz;
	}

}
