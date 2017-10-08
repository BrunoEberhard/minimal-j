package org.minimalj.metamodel.model;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.minimalj.model.Code;
import org.minimalj.model.Keys;
import org.minimalj.model.annotation.Size;
import org.minimalj.model.validation.Validation;
import org.minimalj.util.FieldUtils;
import org.minimalj.util.IdUtils;
import org.minimalj.util.StringUtils;

public class MjEntity {

	public enum MjEntityType {
		ENTITY, HISTORIZED_ENTITY, DEPENDING_ENTITY, CODE;
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
	
	public MjEntity() {
		//
	}
	
	public MjEntity(MjModel model, Class<?> clazz) {
		this.model = model;
		this.clazz = clazz;
		
		model.addEntity(this);
		name = clazz.getName();
		validatable = Validation.class.isAssignableFrom(clazz);
		
		Field[] fields = clazz.getFields();
		for (Field field : fields) {
			if (FieldUtils.isPublic(field) && !FieldUtils.isStatic(field) && !FieldUtils.isTransient(field) && !StringUtils.equals(field.getName(), "id", "version", "historized")) {
				properties.add(new MjProperty(model, field));
			}
		}
		
		if (Code.class.isAssignableFrom(clazz)) {
			type = MjEntityType.CODE;
		} else if (IdUtils.hasId(clazz)) {
			type = MjEntityType.ENTITY;
		} else {
			type = MjEntityType.DEPENDING_ENTITY;
		}
	}
	
	public Class<?> getClazz() {
		return clazz;
	}
}
