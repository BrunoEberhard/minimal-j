package org.minimalj.metamodel.page;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.minimalj.frontend.page.TablePage;
import org.minimalj.metamodel.model.MjEntity;
import org.minimalj.metamodel.model.MjEnumValue;
import org.minimalj.util.resources.Resources;

public class EnumTablePage extends TablePage<MjEnumValue> {

	private final MjEntity entity;
	private List<MjEnumValue> enumValues;
	
	public EnumTablePage(MjEntity entity) {
		super(new Object[]{MjEnumValue.$.ord, MjEnumValue.$.name});
		this.entity = entity;
	}
	
	@Override
	public String getTitle() {
		return MessageFormat.format(Resources.getString(EnumTablePage.class), entity.getClassName());
	}
	
	@Override
	protected List<MjEnumValue> load() {
		if (enumValues == null) {
			Collection<String> values = entity.values;
			enumValues = new ArrayList<>();
			int index = 0;
			for (String s : values) {
				MjEnumValue value = new MjEnumValue();
				value.name = s;
				value.ord = index++;
				enumValues.add(value);
			}
		}
		return enumValues;
	}

}
