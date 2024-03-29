package org.minimalj.metamodel.generator;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;

import org.minimalj.metamodel.model.MjEntity;
import org.minimalj.metamodel.model.MjEntity.MjEntityType;
import org.minimalj.metamodel.model.MjModel;
import org.minimalj.metamodel.model.MjProperty;
import org.minimalj.metamodel.model.MjProperty.MjPropertyType;
import org.minimalj.util.StringUtils;

public class ClassGenerator {

	public static final Collection<String> FORBIDDEN_NAMES = Arrays.asList("List", "Size", "NotEmpty", "Generated", "Keys");

	private final File directory;
	private int indent = 0;
	
	public ClassGenerator(String path) {
		directory = new File(path);
		directory.mkdirs();
		delete(directory);
	}
	
	static void delete(File directory) {
		for (File file : directory.listFiles()) {
			if (file.isDirectory()) {
				delete(file);
			} else if (!isHandMade(file)) {
				file.delete();
			}
		}
	}
	
	private static boolean isHandMade(File file) {
		try (Scanner scanner = new Scanner(file)) {
			while (scanner.hasNextLine()) {
				if (scanner.nextLine().startsWith("@Generated")) {
					return false;
				}
			}
		} catch (FileNotFoundException e) {
			return false;
		}
		return true;
	}
	
	private String createClassName(MjEntity entity) {
		if (entity.type == MjEntityType.ByteArray) {
			return "byte[]";
		} else if (entity.isPrimitiv() && !entity.isEnumeration()) {
			return entity.type.getJavaClass().getSimpleName();
		} else {
			return entity.getClassName();
		}
	}
	
	public void generate(MjModel model) {
		generate(model.entities);
	}
	
	public void generate(Collection<? extends MjEntity> entities) {
		for (MjEntity entity : entities) {
			if (!entity.isPrimitiv() || entity.isEnumeration()) {
				generateEntity(entity);
			}
		}
	}

	private void generateEntity(MjEntity entity) {
		File packageDir = new File(directory, entity.getPackageName().replace('.', File.separatorChar));
		packageDir.mkdirs();
	
		File javaFile = new File(packageDir, createClassName(entity) + ".java");
		if (isHandMade(javaFile)) {
			System.out.println("Not generated: " + entity.getClassName());
			return;
		}
		String java;
		if (entity.isEnumeration()) {
			java = generateEnum(entity);
		} else {
			java = generate(entity);
		}
		try (FileWriter filerWriter = new FileWriter(javaFile); BufferedWriter writer = new BufferedWriter(filerWriter)) {
			writer.write(java);
		} catch (IOException x) {
			throw new RuntimeException(x);
		}
	}

	private String generateEnum(MjEntity entity) {
		StringBuilder s = new StringBuilder();
		s.append("package " + entity.getPackageName() + ";\n\n");
		s.append("import javax.annotation.Generated;\n\n");
		s.append("@Generated(value=\"" + this.getClass().getName() + "\")\n");
		indent(s, indent).append("public enum " + createClassName(entity) + " {\n\t");
		
		generateEnumValues(s, entity);
		
		s.append("\n}");
		return s.toString();
	}

	private void generateEnumValues(StringBuilder s, MjEntity entity) {
		boolean startsWithDigit = entity.values.stream().anyMatch(element -> Character.isDigit(element.charAt(0)));
		
		boolean first = true;
		for (String element : entity.values) {
			if (!first) s.append(", ");
			first = false;
			if (startsWithDigit) s.append("_");
			element = toEnum(element);
			s.append(element);
		}
		
		s.append(";");
	}

	public static String toEnum(String element) {
		element = element.replaceAll("\\_", "\\_\\_");
		element = element.replaceAll(" ", "\\_");
		element = element.replaceAll("\\-", "\\_");
		element = element.replaceAll("\\.", "\\_");
		if (element.equals("public")) element = "_public";
		return element;
	}

	public String generate(MjEntity entity) {
		String packageName = entity.getPackageName();
		String className = createClassName(entity);

		StringBuilder s = new StringBuilder();
		Set<String> forbiddenNames = new TreeSet<>(FORBIDDEN_NAMES);
		forbiddenNames.add(className);
		generateProperties(s, entity, packageName, forbiddenNames);
		
		boolean id = entity.type == MjEntityType.ENTITY || entity.type == MjEntityType.VIEW || entity.type == MjEntityType.CODE && entity.properties.stream().noneMatch(p -> p.name.equals("id"));
		if (entity instanceof GeneratorEntity && ((GeneratorEntity) entity).noId) {
			id = false;
		}
		if (id) {
			s.insert(0, "\tpublic Object id;\n");
		}
		s.insert(0, "\tpublic static final " + className + " $ = Keys.of(" + className + ".class);\n\n");
		String classNameWithExtends = className;
		if (entity.superEntity != null) {
			classNameWithExtends += " extends " + entity.superEntity.getClassName();
		}
		if (entity.type == MjEntityType.CODE) {
			s.insert(0, "\npublic class " + classNameWithExtends + " implements Code {\n");
		} else if (entity.type == MjEntityType.VIEW && entity.viewedEntity != null) {
			s.insert(0, "\npublic class " + classNameWithExtends + " implements View<" + entity.viewedEntity.getClassName() + "> {\n");			
		} else {
			s.insert(0, "\npublic class " + classNameWithExtends + " {\n");
		}
		if (entity.comment != null) {
			s.insert(0, "\n@Comment(\"" + entity.comment + "\")");
		}
		s.insert(0, "\n@Generated(value=\"" + this.getClass().getName() + "\")");
		imprts(s);
		s.insert(0, "package " + packageName + ";\n\n");
		
		s.append("}");
		return s.toString();
	}

	private void generateProperties(StringBuilder s, MjEntity entity, String packageName, Set<String> forbiddenNames) {
		indent++;
		for (MjProperty property : entity.properties) {
			generate(s, property, packageName, forbiddenNames);
			s.append("\n");
		}
		indent--;
	}

	public void generate(StringBuilder s, MjProperty property, String packageName, Set<String> forbiddenNames) {
		String fieldName = property.name;
		
		if (property.type.type == MjEntityType.Enum) {
			if (property.type.values.size() == 1 && Boolean.TRUE.equals(property.notEmpty)) {
				// for String with exact one possible value the field can be initialized and set
				// to final
				indent(s, indent).append("public final String " + fieldName + " = \"" + property.type.values.iterator().next() + "\";\n");
				return;
			}
		}
		
		String className = createClassName(property.type);

		if (StringUtils.isEmpty(className)) {
			// no general type, needs inner class
			className = StringUtils.upperFirstChar(fieldName);
			while (forbiddenNames.contains(className)) {
				className = className + "_";
			}
			forbiddenNames.add(className);
			generateInnerClass(s, property.type, className, packageName, forbiddenNames);
		} else if ((property.type.type.getJavaClass() == null || property.type.isEnumeration()) && !packageName.equals(property.type.getPackageName())) {
			className = property.type.getPackageName() + "." + className;
		}
		
		boolean notEmpty = Boolean.TRUE.equals(property.notEmpty);
		boolean inline = property.propertyType == MjPropertyType.INLINE || !property.type.isPrimitiv() && property.type.type == MjEntityType.DEPENDING_ENTITY && notEmpty;
		boolean autoIncrement = Boolean.TRUE.equals(property.autoIncrement);

		if (notEmpty && !inline) {
			indent(s, indent).append("@NotEmpty\n");
		}
		if (autoIncrement) {
			indent(s, indent).append("@AutoIncrement\n");
		}		
		if (property.technical != null) {
			indent(s, indent).append("@TechnicalField(TechnicalFieldType." + property.technical.name() + ")\n");			
		}
		if (property.comment != null) {
			indent(s, indent).append("@Comment(\"" + property.comment + "\")\n");			
		}
		if (property.propertyType == MjPropertyType.LIST) {
			appendSize(s, property);
			indent(s, indent).append("public List<" + className + "> " + fieldName + ";\n");
		} else if (property.propertyType == MjPropertyType.ENUM_SET) {
			indent(s, indent).append("public final Set<" + className + "> " + fieldName + " = new TreeSet<>();\n");
		} else {
			if (property.type.type == MjEntityType.String && !property.type.isEnumeration()) {
				if (!appendSize(s, property)) {
					System.out.println("Missing size for property " + property.name);
				}
			} else if (property.type.type == MjEntityType.Integer || property.type.type == MjEntityType.Long || property.type.type == MjEntityType.BigDecimal) {
				appendSize(s, property);
			}
			if (inline) {
				indent(s, indent).append("public final " + className + " " + fieldName + " = new " + className + "();\n");
			} else {
				indent(s, indent).append("public " + className + " " + fieldName + ";\n");
			}
		}			
	}

	private boolean appendSize(StringBuilder s, MjProperty property) {
		if (property.size != null) {
			indent(s, indent).append("@Size(" + property.size + ")\n");
			return true;
		} else if (property.type.maxLength != null) {
			indent(s, indent).append("@Size(" + property.type.maxLength + ")\n");
			return true;
		} else if (property.type.type == MjEntityType.String) {
			indent(s, indent).append("@Size(255) // unknown\n");
			return true;
		} else {
			return false;
		}
	}
	
	public String generateInnerClass(StringBuilder s, MjEntity entity, String innerClassName, String packageName, Set<String> forbiddenNames) {
		if (entity.isEnumeration()) {
			s.append('\n');
			indent(s, indent).append("public enum " + innerClassName + " { ");
			generateEnumValues(s, entity);			
			indent(s, indent).append("}\n");
		} else {
			indent(s, indent).append("public static class " + innerClassName + " {\n");
			indent(s, indent + 1).append("public static final " + innerClassName + " $ = Keys.of(" + innerClassName + ".class);\n\n");
			generateProperties(s, entity, packageName, forbiddenNames);
			indent(s, indent).append("}\n");
		}
		return s.toString();
	}
	
	private StringBuilder indent(StringBuilder s, int indent) {
		while (indent > 0) {
			s.append('\t');
			indent--;
		}
		return s;
	}

	
	protected void imprts(StringBuilder java) {
		java.insert(0, "import org.minimalj.model.Keys;\n");
		if (java.indexOf("implements Code ") > -1) java.insert(0, "import org.minimalj.model.Code;\n");
		if (java.indexOf("implements View<") > -1) java.insert(0, "import org.minimalj.model.View;\n");
		if (java.indexOf("@AutoIncrement") > -1) java.insert(0, "import org.minimalj.model.annotation.AutoIncrement;\n");
		if (java.indexOf("@Generated") > -1) java.insert(0, "import javax.annotation.Generated;\n");
		if (java.indexOf("@NotEmpty") > -1) java.insert(0, "import org.minimalj.model.annotation.NotEmpty;\n");
		if (java.indexOf("@Size") > -1) java.insert(0, "import org.minimalj.model.annotation.Size;\n");
		if (java.indexOf("@TechnicalField") > -1) java.insert(0, "import org.minimalj.model.annotation.TechnicalField.TechnicalFieldType;\nimport org.minimalj.model.annotation.TechnicalField;\n");
		if (java.indexOf("@Comment") > -1) java.insert(0, "import org.minimalj.model.annotation.Comment;\n");
		if (java.indexOf("BigDecimal") > -1) java.insert(0, "import java.math.BigDecimal;\n");
		if (java.indexOf("LocalDate ") > -1) java.insert(0, "import java.time.LocalDate;\n");
		if (java.indexOf("LocalTime") > -1) java.insert(0, "import java.time.LocalTime;\n");
		if (java.indexOf("LocalDateTime") > -1) java.insert(0, "import java.time.LocalDateTime;\n");
		if (java.indexOf("List<") > -1) java.insert(0, "import java.util.List;\n");
		if (java.indexOf("Set<") > -1) java.insert(0, "import java.util.Set;\n");
		if (java.indexOf("TreeSet<") > -1) java.insert(0, "import java.util.TreeSet;\n");
	}
	
}
