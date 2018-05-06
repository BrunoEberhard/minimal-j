package org.minimalj.metamodel.generator;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;

import org.minimalj.metamodel.model.MjEntity;
import org.minimalj.metamodel.model.MjEntity.MjEntityType;
import org.minimalj.metamodel.model.MjModel;
import org.minimalj.metamodel.model.MjProperty;
import org.minimalj.metamodel.model.MjProperty.MjPropertyType;
import org.minimalj.util.StringUtils;

public class ClassGenerator {

	private final File dir;
	private Function<MjEntity, String> classNameGenerator;
	
	public ClassGenerator(String path) {
		dir = new File(path);
		dir.mkdirs();
	}
	
	private static boolean deleteDirectory(File dir) {
		if (!dir.exists() || !dir.isDirectory()) {
			return false;
		}

		String[] files = dir.list();
		for (int i = 0, len = files.length; i < len; i++) {
			File f = new File(dir, files[i]);
			if (f.isDirectory()) {
				deleteDirectory(f);
			} else {
				f.delete();
			}
		}
		return dir.delete();
	}
	
	public void setClassNameGenerator(Function<MjEntity, String> classNameGenerator) {
		this.classNameGenerator = classNameGenerator;
	}
	
	private String createClassName(MjEntity entity) {
		if (classNameGenerator != null) {
			return classNameGenerator.apply(entity);
		} else {
			return entity.getClassName();
		}
	}
	
	public void generate(MjModel model) {
		generate(model.entities);
	}
	
	public void generate(Collection<MjEntity> entities) {
		for (MjEntity entity : entities) {
			if (!entity.isPrimitiv() ||entity.isEnumeration()) {
				generateEntity(entity);
			}
		}
	}

	private void generateEntity(MjEntity entity) {
		File packageDir = new File(dir, entity.packageName.replace('.', File.separatorChar));
		packageDir.mkdirs();
	
		File javaFile = new File(packageDir, createClassName(entity) + ".java");
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
		s.append("public enum " + createClassName(entity) + " {\n  ");
		
		generateEnumValues(s, entity);
		
		s.append("\n}");
		s.insert(0, "package " + entity.packageName + ";\n\n");
		return s.toString();
	}

	private void generateEnumValues(StringBuilder s, MjEntity entity) {
		boolean startsWithDigit = false;
		
		for (String element : entity.values) {
			startsWithDigit |= Character.isDigit(element.charAt(0));
		}

		boolean first = true;
		for (String element : entity.values) {
			if (!first) s.append(", ");
			first = false;
			if (startsWithDigit) s.append("_");
			element = element.replaceAll("\\_", "\\_\\_");
			element = element.replaceAll(" ", "\\_");
			element = element.replaceAll("\\-", "\\_");
			element = element.replaceAll("\\.", "\\_");
			// element = StringUtils.toSnakeCase(element);
			if (element.equals("public")) element = "_public";
			s.append(element);
		}
		
		s.append(";");
	}

	public String generate(MjEntity entity) {
		String packageName = entity.packageName;
		String className = createClassName(entity);

		StringBuilder s = new StringBuilder();
		Set<String> forbiddenNames = new TreeSet<>();
		forbiddenNames.add(className);
		for (MjProperty property : entity.properties) {
			generate(s, property, packageName, forbiddenNames);
		}
		s.insert(0, "\npublic class " + className + " {\n\n");
		imprts(s);
		s.insert(0, "package " + packageName + ";\n\n");
		
		s.append("}");
		return s.toString();
	}

	public void generate(StringBuilder s, MjProperty property, String packageName, Set<String> forbiddenNames) {
		String fieldName = property.name;
		
		if (property.type.type == MjEntityType.String) {
			if (property.type.isEnumeration()) {
				if (property.type.values.size() == 1) {
					// for String with exact one possible value the field can be initialized and set to final
					s.append("  public final String " + fieldName + " = \"" + property.type.values.iterator().next() + "\";\n");
					return;
				}
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
		} else if ((property.type.type.getJavaClass() == null || property.type.isEnumeration()) && !packageName.equals(property.type.packageName)) {
			className = property.type.packageName + "." + className;
		}
		
		boolean notEmpty = Boolean.TRUE.equals(property.notEmpty);
		if (notEmpty) {
			s.append("  @NotEmpty\n");
		}
		if (property.propertyType == MjPropertyType.LIST) {
			s.append("  public List<" + className + "> " + fieldName + ";\n");
		} else {
			if (property.type.type == MjEntityType.String && !property.type.isEnumeration()) {
				if (!appendSize(s, property)) {
					if (property.type.getElement() != null) {
						System.out.println("Missing size for element " + property.type.getElement().getAttribute("name"));
					} else {
						System.out.println("Missing size for property " + property.name);
					}
				}
			} else if (property.type.type == MjEntityType.Integer || property.type.type == MjEntityType.Long || property.type.type == MjEntityType.BigDecimal) {
				appendSize(s, property);
			}
			boolean inline = !property.type.isPrimitiv() && notEmpty;
			if (inline) {
				s.append("  public final " + className + " " + fieldName + " = new " + className + "();\n");
			} else {
				s.append("  public " + className + " " + fieldName + ";\n");
			}
		}			
	}

	private boolean appendSize(StringBuilder s, MjProperty property) {
		if (property.type.maxLength != null) {
			s.append("  @Size(" + property.type.maxLength + ")\n");
			return true;
		} else {
			return false;
		}
	}
	
	public String generateInnerClass(StringBuilder s, MjEntity entity, String innerClassName, String packageName, Set<String> forbiddenNames) {
		if (entity.isEnumeration()) {
			s.append("\n  public enum " + innerClassName + " { ");
			generateEnumValues(s, entity);			
			s.append("  }\n");
		} else {
			s.append("\n  public static class " + innerClassName + " {\n\n");
			for (MjProperty property : entity.properties) {
				generate(s, property, packageName, forbiddenNames);
			}
			s.append("  }\n");
		}
		return s.toString();
	}

	
	protected void imprts(StringBuilder java) {
		if (java.indexOf("@NotEmpty") > -1) java.insert(0, "import org.minimalj.model.annotation.NotEmpty;\n");
		if (java.indexOf("@Size") > -1) java.insert(0, "import org.minimalj.model.annotation.Size;\n");
		if (java.indexOf("BigDecimal") > -1) java.insert(0, "import java.math.BigDecimal;\n");
		if (java.indexOf("LocalDate") > -1) java.insert(0, "import java.time.LocalDate;\n");
		if (java.indexOf("LocalTime") > -1) java.insert(0, "import java.time.LocalTime;\n");
		if (java.indexOf("LocalDateTime") > -1) java.insert(0, "import java.time.LocalDateTime;\n");
		if (java.indexOf("List<") > -1) java.insert(0, "import java.util.List;\n");
	}
	
}
