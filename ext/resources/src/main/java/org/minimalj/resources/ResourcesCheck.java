package org.minimalj.resources;

import java.io.PrintStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.minimalj.application.Application;
import org.minimalj.frontend.action.Action;
import org.minimalj.frontend.page.Page;
import org.minimalj.model.EnumUtils;
import org.minimalj.model.View;
import org.minimalj.model.ViewUtil;
import org.minimalj.util.FieldUtils;
import org.minimalj.util.GenericUtils;
import org.minimalj.util.StringUtils;
import org.reflections.Reflections;

import com.minimalj.order24.Order24;

public class ResourcesCheck {
	private ResourcesModel resourcesModel;
	
	private static class ResourcesModel {
		public final List<ResourcesEntity> rootEntities = new ArrayList<>();
		public final Map<Class<?>, ResourcesEntity> classes = new HashMap<>();
		public List<String> pages = new ArrayList<>();
		public List<String> actions = new ArrayList<>();
		public final Map<Class<?>, ResourcesEnum> enums = new HashMap<>();
		
		public ResourcesModel(Application application) {
			for (Class<?> clazz : application.getEntityClasses()) {
				rootEntities.add(read(clazz));
			}
			
			Reflections reflections = new Reflections(application.getClass().getPackageName());
		
			Set actionClasses = reflections.getSubTypesOf(Action.class);
			for (Object o : actionClasses) {
				Class actionClass = (Class) o;
				if (!actionClass.getPackage().getName().startsWith("org.minimalj")) {
					actions.add(actionClass.getSimpleName());
				}
			}
			Collections.sort(actions);
			
			Set pageClasses = reflections.getSubTypesOf(Page.class);
			for (Object o : pageClasses) {
				Class pageClass = (Class) o;
				if (!pageClass.getPackage().getName().startsWith("org.minimalj")) {
					pages.add(pageClass.getSimpleName());
				}
			}
			Collections.sort(pages);
		}

		private ResourcesEntity read(Class<?> clazz) {
			if (classes.containsKey(clazz)) {
				return classes.get(clazz);
			}
			ResourcesEntity resourcesEntity = new ResourcesEntity();
			resourcesEntity.clazz = clazz;
			classes.put(clazz, resourcesEntity);
			
			Field[] fields = clazz.getFields();
			for (Field field : fields) {
				if (!FieldUtils.isPublic(field) || FieldUtils.isStatic(field) || StringUtils.equals(field.getName(), "id", "version", "historized")) {
					continue;
				}
				resourcesEntity.fields.add(field.getName());
				
				Class<?> fieldType = field.getType();
				if (FieldUtils.isAllowedPrimitive(fieldType)) {
					continue;
				}
				
				if (fieldType == List.class) {
					Class<?> listType = GenericUtils.getGenericClass(field);
					resourcesEntity.entities.add(read(listType));
				} else if (fieldType == Set.class) {
					Class<?> setType = GenericUtils.getGenericClass(field);
					readEnum(setType);
				} else if (View.class.isAssignableFrom(fieldType)) {
					resourcesEntity.entities.add(read(ViewUtil.getViewedClass(fieldType)));
				} else if (fieldType.isEnum()) {
					readEnum(fieldType);
				} else {
					resourcesEntity.entities.add(read(fieldType));
				}
			}
			
			return resourcesEntity;
		}
		
		private ResourcesEnum readEnum(Class<?> clazz) {
			if (enums.containsKey(clazz)) {
				return enums.get(clazz);
			}
			ResourcesEnum resourcesEnum = new ResourcesEnum();
			resourcesEnum.clazz = clazz;
			enums.put(clazz, resourcesEnum);
			
			resourcesEnum.values = (List<String>) EnumUtils.valueList((Class) clazz).stream().map(o -> o.toString()).collect(Collectors.toList());
			
			return resourcesEnum;
		}
		
		public void print(PrintStream p) {
			List<Class<?>> printed = new ArrayList<>();

			p.println("# Entities");
			for (ResourcesEntity entity : rootEntities) {
				print(p, entity, printed);
			}
			
			if (!enums.isEmpty()) {
				List<ResourcesEnum> enums = new ArrayList<>(this.enums.values());
				Collections.sort(enums);
				p.println("# Enums");
				for (ResourcesEnum entity : enums) {
					print(p, entity, printed);
				}
				p.println();
			}
			

			p.println("# Pages");
			for (String page : pages) {
				p.println(page + " = ");
			}
			p.println();

			p.println("# Actions");
			for (String a : actions) {
				p.println(a + " = ");
			}
			p.println();
		}
		
		private void print(PrintStream p, ResourcesEntity entity, List<Class<?>> printed) {
			if (printed.contains(entity.clazz)) {
				return;
			}
			printed.add(entity.clazz);
			entity.print(p);

			for (ResourcesEntity e : entity.entities) {
				print(p, e, printed);
			}
		}
		
		private void print(PrintStream p, ResourcesEnum en, List<Class<?>> printed) {
			if (printed.contains(en.clazz)) {
				return;
			}
			printed.add(en.clazz);
			en.print(p);
		}
		
	}
	
	private static class ResourcesEntity {
		public Class<?> clazz;
		public final List<String> fields = new ArrayList<String>();
		public final List<ResourcesEntity> entities = new ArrayList<>();
		
		public void print(PrintStream p) {
			p.println(clazz.getSimpleName() + " = ");
			for (String field: fields) {
				p.println(clazz.getSimpleName() + "." + field + " = ");
			}
			p.println();
		}
	}
	
	private static class ResourcesEnum implements Comparable<ResourcesEnum> {
		public Class<?> clazz;
		public List<String> values;
		
		@Override
		public int compareTo(ResourcesEnum o) {
			return clazz.getSimpleName().compareTo(o.clazz.getSimpleName());
		}
		
		public void print(PrintStream p) {
			p.println(clazz.getSimpleName() + " = ");
			for (String value: values) {
				p.println(clazz.getSimpleName() + "." + value + " = ");
			}
			p.println();
		}
	}
	
	private static class CommonProperty {
		public String name;
	}
	
	
	private static class Entry {
		public String name;
		public final Map<String, String> textByLocal = new HashMap<>();
	}
	
	private Map<Class<?>, List<Entry>> entryByClass = new HashMap<>();

	public ResourcesCheck(Application application) {
		resourcesModel = new ResourcesModel(application);
	}
	public static void main(String[] args) {
		new ResourcesCheck(new Order24()).resourcesModel.print(System.out);;
	}
}
