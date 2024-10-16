package org.minimalj.model.test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;

import org.minimalj.application.Application;
import org.minimalj.application.Configuration;
import org.minimalj.model.EnumUtils;
import org.minimalj.model.Keys;
import org.minimalj.model.Model;
import org.minimalj.model.View;
import org.minimalj.model.annotation.AnnotationUtil;
import org.minimalj.model.annotation.Enabled;
import org.minimalj.model.annotation.Materialized;
import org.minimalj.model.annotation.Searched;
import org.minimalj.model.annotation.SelfReferenceAllowed;
import org.minimalj.model.annotation.Size;
import org.minimalj.model.annotation.TechnicalField;
import org.minimalj.model.annotation.TechnicalField.TechnicalFieldType;
import org.minimalj.model.annotation.Visible;
import org.minimalj.model.properties.FlatProperties;
import org.minimalj.model.properties.Properties;
import org.minimalj.model.properties.Property;
import org.minimalj.util.FieldUtils;
import org.minimalj.util.GenericUtils;
import org.minimalj.util.IdUtils;
import org.minimalj.util.StringUtils;
import org.minimalj.util.resources.Resources;

/**
 * Test some restrictions on model classes.
 * <p>
 * 
 * These tests are called by JUnit tests but also by the Repository. They are
 * fast and its better to see problems at startup of an application.
 */
public class ModelTest {
	private static final Logger logger = Logger.getLogger(ModelTest.class.getName());

	private final Collection<Class<?>> mainClasses;
	private Set<Class<?>> modelClasses = new HashSet<>();
	
	private final List<String> problems = new ArrayList<>();

	public ModelTest(Model model) {
		this(model.getEntityClasses());
	}
	
	public ModelTest(Class<?>... mainClasses) {
		this.mainClasses = Model.getEntityClassesRecursive(mainClasses);

		for (Class<?> clazz : this.mainClasses) {
			testClass(clazz);
		}
		testEnums(this.mainClasses);
	}
	
	/**
	 * Allows fail early when application is started. NanoWebServer / Swing can check at startup
	 * if repository will make problems. Without this there would be a lot of confusing stacktrace lines
	 */
	public static void exitIfProblems() {
		ModelTest test = new ModelTest(Application.getInstance().getEntityClasses());
		if (!test.getProblems().isEmpty()) {
			test.logProblems();
			System.exit(-1);
		}
	}
	
	public List<String> getProblems() {
		return problems;
	}
	
	/**
	 * if there are problems log them and throw IllegalArgumentException
	 * 
	 * @throws IllegalArgumentException if problems is not empty
	 */
	public void assertValid() {
		if (!getProblems().isEmpty()) {
			logProblems();
			throw new IllegalArgumentException("The persistent classes don't apply to the given rules");
		}
	}
	
	public void logProblems() {
		if (!problems.isEmpty()) {
			logger.severe("The entitiy classes don't apply to the given rules");
			for (String s : problems) {
				logger.severe(s);
			}
		}
	}
	
	public boolean isValid() {
		return problems.isEmpty();
	}

	private void testClass(Class<?> clazz) {
		if (!modelClasses.contains(clazz)) {
			modelClasses.add(clazz);
			testName(clazz);
			if (!clazz.isEnum()) { // it's not pretty to have several enum classes with same name but it works
				testNoDuplicateName(clazz);
			}
			if (!View.class.isAssignableFrom(clazz)) {
				testNoOrAbstractSuperclass(clazz);
			}
			if (!testNoSelfMixins(clazz)) {
				return; // further tests could create a StackOverflowException
			}
			testId(clazz);
			testVersion(clazz);
			testHistorized(clazz);
			testConstructor(clazz);
			testFields(clazz);
			testMethods(clazz);
			testSelfReferences(clazz);
			if (!IdUtils.hasId(clazz)) {
				testNoListFields(clazz);
			}
			if (Configuration.isDevModeActive()) {
				testResources(clazz);
			}
		}
	}

	private void testInlineClass(Class<?> clazz) {
		testName(clazz);
		testNoOrAbstractSuperclass(clazz);
		testFields(clazz);
		// TODO testNoInlineRecursion(clazz);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void testConstructor(Class<?> clazz)  {
		if (Enum.class.isAssignableFrom(clazz)) {
			try {
				EnumUtils.createEnum((Class<Enum>) clazz, "Test");
			} catch (Exception e) {
				problems.add("Not possible to create runtime instance of enum " + clazz.getName() + ". Possibly there is no empty constructor");
			}
		} else {
			try {
				Constructor<?> constructor = clazz.getConstructor();
				if (!Modifier.isPublic(constructor.getModifiers())) {
					problems.add("Constructor of " + clazz.getName() + " not public");
				}
			} catch (NoSuchMethodException e) {
				problems.add(clazz.getName() + " has no public empty constructor");
			}
		}
	}

	private boolean isMain(Class<?> clazz) {
		return mainClasses.contains(clazz);
	}
	
	private void testNoOrAbstractSuperclass(Class<?> clazz) {
		Class<?> superclass = clazz.getSuperclass();
		if (superclass != null && superclass != Object.class && superclass != Enum.class) {
			if (!Modifier.isAbstract(superclass.getModifiers())) {
				problems.add(clazz.getName() + ": Domain super classes must be abstract");
			}
		}
	}
	
	private boolean testNoSelfMixins(Class<?> clazz) {
		return testNoSelfMixins(clazz, Collections.emptyList());
	}

	private boolean testNoSelfMixins(Class<?> clazz, List<Class<?>> outerClasses) {
		List<Class<?>> forbiddenClasses = new ArrayList<>(outerClasses);
		forbiddenClasses.add(clazz);
		
		Field[] fields = clazz.getFields();
		for (Field field : fields) {
			if (FieldUtils.isTransient(field) || FieldUtils.isStatic(field)) continue;

			if (FieldUtils.isFinal(field) && !FieldUtils.isList(field)) {
				Class<?> mixinClass = field.getType();
				if (forbiddenClasses.contains(mixinClass)) {
					problems.add(clazz.getName() + ": Mixin classes must not mix in itself");
					return false;
				} else {
					return testNoSelfMixins(mixinClass);
				}
			}
		}
		return true;
	}
	
	private void testId(Class<?> clazz) {
		if (!isMain(clazz)) {
			return;
		}
		try {
			Property property = FlatProperties.getProperty(clazz, "id");
			if (!FieldUtils.isAllowedId(property.getClazz())) {
				problems.add(clazz.getName() + ": id must be of Integer, Long, String, Object");
			}
		} catch (IllegalArgumentException e) {
			problems.add(clazz.getName() + ": Class missing id field");
		}
	}

	private void testVersion(Class<?> clazz) {
		try {
			Field fieldVersion = clazz.getField("version");
			if (isMain(clazz)) {
				if (fieldVersion.getType() == Integer.class) {
					problems.add(clazz.getName() + ": Domain classes version must be of primitiv type int");
				}
				if (!FieldUtils.isPublic(fieldVersion)) {
					problems.add(clazz.getName() + ": field version must be public");
				}
			} else {
				problems.add(clazz.getName() + ": Only main entities are allowed to have an version field");
			}
		} catch (NoSuchFieldException e) {
			// thats ok, version is not mandatory
		} catch (SecurityException e) {
			problems.add(clazz.getName() + " makes SecurityException with the version field");
		}
	}
	
	private void testHistorized(Class<?> clazz) {
		try {
			Field fieldHistorized = clazz.getField("historized");
			if (isMain(clazz)) {
				if (fieldHistorized.getType() != Boolean.TYPE) {
					problems.add(clazz.getName() + ": Domain classes historized must be of primitiv type boolean");
				}
				if (!FieldUtils.isPublic(fieldHistorized)) {
					problems.add(clazz.getName() + ": field historized must be public");
				}
			} else {
				problems.add(clazz.getName() + ": Only main entities are allowed to have an historized field");
			}
		} catch (NoSuchFieldException e) {
			// thats ok, historized is not mandatory
		} catch (SecurityException e) {
			problems.add(clazz.getName() + " makes SecurityException with the historized field");
		}
	}
	
	private void testFields(Class<?> clazz) {
		Field[] fields = clazz.getFields();
		for (Field field : fields) {
			testField(clazz, field);
		}
	}

	private void testField(Class<?> clazz, Field field) {
		if (FieldUtils.isPublic(field) && !FieldUtils.isStatic(field) && !FieldUtils.isTransient(field) && !StringUtils.equals(field.getName(), "id", "version", "historized")) {
			testName(field);
			testTypeOfField(clazz, field);
			testNoMethodsForPublicField(field);
			TechnicalField technicalField = field.getAnnotation(TechnicalField.class);
			if (technicalField != null) {
				testTypeOfTechnicalField(field, technicalField.value());
			}
			Class<?> fieldType = field.getType();
			if (!View.class.isAssignableFrom(field.getDeclaringClass()) && !FieldUtils.isFinal(field)) {
				if (fieldType == String.class) {
					testStringSize(field);
				} else if (fieldType == LocalTime.class || fieldType == LocalDateTime.class) {
					testTimeSize(field);
				}
			}
		}
		if (FieldUtils.isPublic(field) && !FieldUtils.isStatic(field)) {
			testFieldNotInSuperClass(field);
			testEnable(field);
			testVisible(field);
		}
	}
	
	private void testNoListFields(Class<?> clazz) {
		FlatProperties.getProperties(clazz).values();
		Field[] fields = clazz.getFields();
		for (Field field : fields) {
			if (FieldUtils.isPublic(field) && !FieldUtils.isStatic(field) && !FieldUtils.isTransient(field)) {
				Class<?> fieldType = field.getType();
				if (List.class.equals(fieldType)) {
					problems.add("List in " + clazz.getName()  + ": not allowed. Only classes with id (or inlines of classes with id) may contain lists");
				} else if (FieldUtils.isFinal(field) && !FieldUtils.isAllowedPrimitive(fieldType)) {
					testNoListFields(fieldType);
				}
			}
		}
	}
	
	private void testMethods(Class<?> clazz) {
		Method[] methods = clazz.getMethods();
		for (Method method : methods) {
			testMethod(method);
		}
	}

	private void testMethod(Method method) {
		if (Keys.isPublic(method) && !Keys.isStatic(method) && method.getName().startsWith("get")) {
			if (method.getReturnType() == String.class && (method.getAnnotation(Materialized.class) != null || method.getAnnotation(Searched.class) != null)) {
				String propertyName = StringUtils.lowerFirstChar(method.getName().substring(3));
				Property property = new Keys.MethodProperty(method.getReturnType(), propertyName, method, null);
				try {
					AnnotationUtil.getSize(property);
				} catch (IllegalArgumentException x) {
					problems.add("Missing size for materialized getter: " + method.getDeclaringClass().getName() + "." + method.getName());
				}
			}
		}
	}
	
	private void testName(Field field) {
		String name = field.getName();
		String messagePrefix = field.getName() + " of " + field.getDeclaringClass().getName();
		testName(name, messagePrefix);
	}

	private void testName(Class<?> clazz) {
		String name = clazz.getSimpleName();
		String messagePrefix = "Class " + clazz.getSimpleName();
		testName(name, messagePrefix);
	}

	private void testName(String name, String messagePrefix) {
		for (int i = 0; i<name.length(); i++) {
			char c = name.charAt(i);
			if (isIdentifierChar(c)) continue;
			problems.add(messagePrefix + " has an invalid name. " + c + " is not allowed");
			break;
		}
	}

	private boolean isIdentifierChar(char c) {
		return c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z' || c >= '0' && c <= '9';
	}
	
	private void testNoDuplicateName(Class<?> clazz) {
		String name = clazz.getSimpleName();
		Optional<Class<?>> duplicate = modelClasses.stream().filter(c -> c != clazz && c.getSimpleName().equals(name)).findAny();
		if (duplicate.isPresent()) {
			problems.add("Two classes with simple name: " + name + "(" + clazz.getName() + "/" + duplicate.get().getName() + ")");
		}
	}

	private void testTypeOfField(Class<?> clazz, Field field) {
		Class<?> fieldType = field.getType();
		String messagePrefix = field.getName() + " of " + field.getDeclaringClass().getName();

		if (fieldType == List.class) {
			testTypeOfListField(clazz, field, messagePrefix);
		} else if (fieldType == Set.class) {
			if (!FieldUtils.isFinal(field)) {
				problems.add(messagePrefix + " must be final (" + fieldType.getSimpleName() + " Fields must be final)");
			}
			testTypeOfSetField(field, messagePrefix);
		} else {
			testTypeOfField(field, messagePrefix);
		}
	}

	private void testTypeOfListField(Class<?> clazz, Field field, String messagePrefix) {
		Class<?> listType = GenericUtils.getGenericClass(clazz, field);
		if (listType != null) {
			if (Modifier.isAbstract(listType.getModifiers())) {
				problems.add(messagePrefix + " must not be of an abstract Type");
			}
			testTypeOfListField(listType, "Generic of " + messagePrefix);
			if (IdUtils.hasId(listType) && FieldUtils.isFinal(field)) {
				problems.add("List of identifiables must not be final: " + messagePrefix);
			}
		} else {
			problems.add("Could not evaluate generic of " + messagePrefix);
		}
	}

	private void testTypeOfSetField(Field field, String messagePrefix) {
		@SuppressWarnings("rawtypes")
		Class setType = GenericUtils.getGenericClass(field);
		if (setType != null) {
			if (!Enum.class.isAssignableFrom(setType)) {
				problems.add("Set type must be an enum class: " + messagePrefix);
			}
			@SuppressWarnings("unchecked")
			List<?> values = EnumUtils.valueList(setType);
			if (values.size() > 32) {
				problems.add("Set enum must not have more than 32 elements: " + messagePrefix);
			}
		} else {
			problems.add("Could not evaluate generic of " + messagePrefix);
		}
	}
	
	private void testTypeOfField(Field field, String messagePrefix) {
		Class<?> fieldType = field.getType();
		if (FieldUtils.isAllowedPrimitive(fieldType)) {
			return;
		}
		if (fieldType.isPrimitive()) {
			problems.add(messagePrefix + " has invalid Type");
		}
		if (Modifier.isAbstract(fieldType.getModifiers())) {
			problems.add(messagePrefix + " must not be of an abstract Type");
		}
		if (fieldType.isArray()) {
			problems.add(messagePrefix + " is an array which is not allowed (except for byte[])");
		}
		if (FieldUtils.isFinal(field)) {
			testInlineClass(fieldType);
		} else {
			testClass(fieldType);
		}
	}

	private void testTypeOfListField(Class<?> fieldType, String messagePrefix) {
		if (fieldType.isPrimitive()) {
			problems.add(messagePrefix + " has invalid Type");
			return;
		}
		if (Modifier.isAbstract(fieldType.getModifiers())) {
			problems.add(messagePrefix + " must not be of an abstract Type");
			return;
		}
		if (fieldType.isArray()) {
			problems.add(messagePrefix + " is an array which is not allowed");
			return;
		}
		testClass(fieldType);
	}
	
	private void testStringSize(Field field) {
		Property property = Properties.getProperty(field);
		try {
			AnnotationUtil.getSize(property);
		} catch (IllegalArgumentException x) {
			problems.add("Missing size for: " + property.getDeclaringClass().getName() + "." + property.getPath());
		}
	}
	
	private void testTimeSize(Field field) {
		Property property = Properties.getProperty(field);
		int size = AnnotationUtil.getSize(property, AnnotationUtil.OPTIONAL);
		if (size > -1) {
			if (size != Size.TIME_HH_MM && size != Size.TIME_WITH_SECONDS && size != Size.TIME_WITH_MILLIS) {
				problems.add("Unsupported size for: " + property.getDeclaringClass().getName() + "." + property.getPath() + " - only Size.TIME_ constants can be used");
			}
		}
	}

	private void testEnable(Field field) {
		Enabled enabled = field.getAnnotation(Enabled.class);
		if (enabled != null) {
			testCondition("enable", field, enabled.value());
		}
	}

	private void testVisible(Field field) {
		Visible visible = field.getAnnotation(Visible.class);
		if (visible != null) {
			testCondition("visible", field, visible.value());
		}
	}

	private void testCondition(String annotationName, Field field, String conditionMethod) {
		if (StringUtils.equals(conditionMethod, "true", "false")) {
			return;
		}
		Property property = Properties.getProperty(field);
		if (conditionMethod.startsWith("!")) {
			conditionMethod = conditionMethod.substring(1);
		}
		try {
			Class<?> clazz = field.getDeclaringClass();
			Method method = clazz.getMethod(conditionMethod);
			if (method.getReturnType() != Boolean.TYPE) {
				problems.add("Condition: " + conditionMethod + " used in " + annotationName + " for " + property.getDeclaringClass().getName() + "." + property.getPath() + " does not return a boolean");
			}
		} catch (NoSuchMethodException x) {
			problems.add("Unknown condition: " + conditionMethod + " used in " + annotationName + " for " + property.getDeclaringClass().getName() + "." + property.getPath());
		} catch (Exception x) {
			throw new RuntimeException(x);
		}
	}
	
	private void testNoMethodsForPublicField(Field field) {
		Property property = Properties.getProperty(field);
		if (property != null) {
			if (property.getClass().getSimpleName().startsWith("Method")) {
				problems.add("A public attribute must not have getter or setter methods: " + field.getDeclaringClass().getName() + "." + field.getName());
			}
		} else {
			problems.add("No property for " + field.getName());
		}
	}
	
	private void testFieldNotInSuperClass(Field field) {
		Property property = Properties.getProperty(field);
		String propertyName = property.getName();
		Class<?> declaringClass = property.getDeclaringClass();
		Class<?> superClass = declaringClass.getSuperclass();
		while (superClass != Object.class) {
			Property samePropertyInSuperClass = Properties.getProperty(superClass, propertyName);
			if (samePropertyInSuperClass != null && samePropertyInSuperClass.getDeclaringClass() == superClass) {
				problems.add("Property " + propertyName + " is declared in " + property.getDeclaringClass().getSimpleName() + " and in it's super class " + superClass.getSimpleName());
				return;
			}
			superClass = superClass.getSuperclass();
		}
	}
	
	private void testTypeOfTechnicalField(Field field, TechnicalFieldType type) {
		if (type == TechnicalFieldType.CREATE_DATE || type == TechnicalFieldType.EDIT_DATE) {
			if (field.getType() != LocalDateTime.class) {
				problems.add("Technical field " + field.getDeclaringClass().getSimpleName() + "." + type.name() + " must be of LocalDateTime, not " + field.getType().getName());
			} 
		} else if (type == TechnicalFieldType.CREATE_USER || type == TechnicalFieldType.EDIT_USER) {
			if (FieldUtils.isAllowedPrimitive(field.getType()) && field.getType() != String.class) {
				problems.add("Technical field " + field.getDeclaringClass().getSimpleName() + "." + type.name() + " must be of String or (User) clazz, not " + field.getType().getName());
			} 
		}
	}
	
	private void testResources(Class<?> clazz) {
		for (Property property : FlatProperties.getProperties(clazz).values()) {
			if (StringUtils.equals(property.getName(), "id", "version")) continue;
			Resources.getPropertyName(property);
		}
	}
	
	private void testSelfReferences(Class<?> clazz) {
		testSelfReferences("", clazz, new HashSet<>());
	}
	
	private void testSelfReferences(String path, Class<?> clazz, Set<Class<?>> seenClasses) {
		Field[] fields = clazz.getFields();
		for (Field field : fields) {
			if (FieldUtils.isPublic(field) && !FieldUtils.isStatic(field) &!FieldUtils.isTransient(field)) {
				Class<?> fieldType = field.getType();
				boolean selfReferenceAllowed = AnnotationUtil.isAnnotationPresentOrInherited(fieldType, SelfReferenceAllowed.class);
				if (!FieldUtils.isAllowedPrimitive(fieldType) && fieldType != List.class && fieldType != Set.class && fieldType != Object.class && !selfReferenceAllowed) {
					String pathWithField = (path.length() == 0 ? "" : path + ".") + field.getName();
					if (seenClasses.contains(fieldType)) {
						problems.add("Self reference cycle: " + pathWithField);
					} else {
						seenClasses.add(fieldType);
						testSelfReferences(pathWithField, fieldType, seenClasses);
						seenClasses.remove(fieldType);
					}
				}
			}
		}
	}
	
	private void testEnums(Collection<Class<?>> mainClasses) {
		Set<Class<? extends Enum<?>>> enums = new HashSet<>();
		Set<Class<?>> visited = new HashSet<>();
		mainClasses.forEach(clazz -> collectEnums(clazz, enums, visited));
		for (Class<? extends Enum<?>> enm : enums) {
			for (Class<? extends Enum<?>> enm2 : enums) {
				if (enm != enm2 && enm.getSimpleName().equals(enm2.getSimpleName())) {
					problems.add("Two enum classes with same simple name not allowed: " + enm.getName() + " / " + enm2.getName());
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	private void collectEnums(Class<?> clazz, Set<Class<? extends Enum<?>>> enums, Set<Class<?>> visited) {
		if (!visited.contains(clazz)) {
			visited.add(clazz);
			Field[] fields = clazz.getFields();
			for (Field field : fields) {
				if (FieldUtils.isPublic(field) && !FieldUtils.isStatic(field) &!FieldUtils.isTransient(field)) {
					Class<?> fieldType = field.getType();
					if (Collection.class.isAssignableFrom(fieldType)) {
						fieldType = GenericUtils.getGenericClass(clazz, field);
					}
					if (fieldType != null) {
						if (fieldType.isEnum()) {
							enums.add((Class<? extends Enum<?>>) fieldType);
						}
						if (!fieldType.isPrimitive()) {
							collectEnums(fieldType, enums, visited);
						}
					}
				}
			}
		}
	}
	
}