package ch.openech.mj.db.model;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import ch.openech.mj.util.StringUtils;

public class Code {
	
	private final ResourceBundle resources;
	private final String prefix;
	private final String displayName;
	private final String nullText;
	private final String defolt;
	protected final List<CodeItem<String>> codeItems = new ArrayList<CodeItem<String>>();

	public Code(ResourceBundle resources, String prefix) {
		this.resources = resources;
		this.prefix = prefix;
		
		displayName = readDisplayName();
		nullText = readNullText();
		defolt = readDefault();
		readValues();
	}

	protected String getString(String key) {
		key = prefix + "." + key;
		if (resources.containsKey(key)) return resources.getString(key);
		else return null;
	}
	
	private String readDisplayName() {
		return getString("object");
	}
	
	private String readNullText() {
		String unknownText = getString("null.text");
		if (StringUtils.isBlank(unknownText)) {
			unknownText = "";
		}
		return unknownText;
	}
	
	private String readDefault() {
		return getString("default");
	}
	
	protected void readValues() {
		int index = 0;
		String key = null;
		while ((key = getString("key." + index)) != null) {
			String text = getString("text." + index);
			if (StringUtils.isBlank(text)) text = "Wert " + key;
			String description = getString("description." + index);
			codeItems.add(new CodeItem<String>(key, text, description));
			index++;
		}
		if (index == 0) {
			throw new RuntimeException("Code without values: " + prefix);
		}
	}
	
	public String getDisplayName() {
		return displayName;
	}
	
	public String getNullText() {
		return nullText;
	}
	
	public String getDefault() {
		return defolt;
	}
	
	public List<CodeItem<String>> getCodeItems() {
		return codeItems;
	}

	public String getText(String key) {
		for (CodeItem<String> codeItem : codeItems) {
			if (StringUtils.equals(key, codeItem.getKey())) {
				return codeItem.getText();
			}
		}
		return "Wert " + key;
	}

	public int count() {
		return codeItems.size();
	}

	public int getSize() {
		int maxSize = 0;
		for (CodeItem<String> codeItem : codeItems) {
			maxSize = Math.max(maxSize, codeItem.getKey().length());
		}
		return maxSize;
	}
}
