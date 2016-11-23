package org.minimalj.metamodel.page;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.minimalj.frontend.page.TablePage;
import org.minimalj.metamodel.model.MjEnumValue;
import org.minimalj.model.EnumUtils;
import org.minimalj.util.resources.Resources;

@SuppressWarnings("rawtypes")
public class EnumTablePage extends TablePage<MjEnumValue> {

	private final Class enumClass;
	private List<MjEnumValue> enumValues;
	
	public EnumTablePage(Class<?> enumClass) {
		super(new Object[]{MjEnumValue.$.ord, MjEnumValue.$.name});
		this.enumClass = enumClass;
	}
	
	@Override
	public String getTitle() {
		return MessageFormat.format(Resources.getString(EnumTablePage.class), enumClass.getName());
	}
	
	@Override
	protected List<MjEnumValue> load() {
		if (enumValues == null) {
			@SuppressWarnings("unchecked")
			List<Object> list = EnumUtils.valueList(enumClass);
			enumValues = new ArrayList<>();
			for (Object o : list) {
				Enum e = (Enum) o;
				MjEnumValue value = new MjEnumValue();
				value.name = e.name();
				value.ord = e.ordinal();
				enumValues.add(value);
			}
		}
		return enumValues;
	}

}
