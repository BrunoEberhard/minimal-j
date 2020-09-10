package org.minimalj.metamodel.generator;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

import org.minimalj.metamodel.model.MjEntity;
import org.minimalj.metamodel.model.MjModel;
import org.minimalj.metamodel.model.MjProperty;

public class ResourceGenerator {

	private final File directory;
	private List<String> entries = new ArrayList<>();
	private Predicate<String> propertyFilter;

	public ResourceGenerator(String path) {
		directory = new File(path);
	}

	public void setPropertyFilter(Predicate<String> propertyFilter) {
		this.propertyFilter = propertyFilter;
	}

	public void generate(MjModel model) {
		generate(model.entities);
	}

	public void generate(Collection<? extends MjEntity> entities) {
		for (MjEntity entity : entities) {
			entries.add(entity.getClassName() + " = " + toLabel(entity.getClassName()));

			for (MjProperty property : entity.properties) {
				if (property.technical != null || propertyFilter != null && !propertyFilter.test(property.name)) {
					continue;
				}
				String key = entity.getClassName() + "." + property.name;
				entries.add(key + " = " + toLabel(property.name));
			}
		}
		Collections.sort(entries);

		File propertyFile = new File(directory, "entities.properties");
		try (FileWriter filerWriter = new FileWriter(propertyFile); BufferedWriter writer = new BufferedWriter(filerWriter)) {
			String lastPrefix = null;
			for (String entry : entries) {
				String prefix = getPrefix(entry);
				if (lastPrefix != null && !lastPrefix.equals(prefix)) {
					writer.append('\n');
				}
				writer.append(entry).append('\n');
				lastPrefix = prefix;
			}
		} catch (IOException x) {
			throw new RuntimeException(x);
		}
	}

	private String getPrefix(String entry) {
		entry = entry.substring(0, entry.indexOf('=')).trim();
		if (entry.indexOf('.') > -1) {
			return entry.substring(0, entry.indexOf('.'));
		} else {
			return entry;
		}
	}

	protected StringBuilder toLabel(String name) {
		StringBuilder s = new StringBuilder();
		for (int i = 0; i < name.length(); i++) {
			char c = name.charAt(i);
			if (i == 0) {
				s.append(Character.toUpperCase(c));
			} else if (Character.isUpperCase(c)) {
				s.append(' ').append(c);
			} else {
				s.append(c);
			}
		}
		return s;
	}
}
