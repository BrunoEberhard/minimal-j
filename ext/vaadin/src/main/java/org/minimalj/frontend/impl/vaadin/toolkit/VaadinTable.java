package org.minimalj.frontend.impl.vaadin.toolkit;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.minimalj.frontend.Frontend.ITable;
import org.minimalj.frontend.Frontend.TableActionListener;
import org.minimalj.model.Keys;
import org.minimalj.model.Rendering;
import org.minimalj.model.properties.PropertyInterface;
import org.minimalj.util.resources.Resources;

import com.vaadin.data.PropertyDefinition;
import com.vaadin.data.PropertySet;
import com.vaadin.data.ValueProvider;
import com.vaadin.server.Setter;
import com.vaadin.ui.Grid;
import com.vaadin.ui.components.grid.ItemClickListener;
import com.vaadin.ui.renderers.TextRenderer;

import elemental.json.JsonValue;

@SuppressWarnings({"unchecked", "rawtypes"})
public class VaadinTable<T> extends Grid<T> implements ITable<T> {
	private static final long serialVersionUID = 1L;

	private final TableActionListener<T> listener;
	private VaadinTableItemClickListener tableClickListener;
	// private Action action_delete = new ShortcutAction("Delete", ShortcutAction.KeyCode.DELETE, null);
	// private Action action_enter = new ShortcutAction("Enter", ShortcutAction.KeyCode.DELETE, null);

	private PropertyInterface[] properties;
	
	public VaadinTable(Object[] keys, boolean multiSelect, TableActionListener<T> listener) {
		super(propertySet(keys));
		addStyleName("table");
		this.listener = listener;
		this.properties = Keys.getProperties(keys);
		
		setSelectionMode(multiSelect ? SelectionMode.MULTI : SelectionMode.SINGLE);
		setSizeFull();
		
		tableClickListener = new VaadinTableItemClickListener();
		addItemClickListener(tableClickListener);
	}
	
	@Override
    public Column<T, ?> addColumn(String propertyName) {
        return addColumn(propertyName, new MjTableRenderer(propertyName));
    }
	
	private static <T> PropertySet<T> propertySet(Object[] keys) {
		return new MjTablePropertySet(keys);
	}

	private static class MjTablePropertySet<T> implements PropertySet<T> {
		private static final long serialVersionUID = 1L;

		private final PropertyInterface properties[];
		private final List<PropertyDefinition<T, ?>> defList = new ArrayList<>();
		
		public  MjTablePropertySet(Object[] keys) {
			properties = Keys.getProperties(keys);
			for (PropertyInterface p : properties) {
				defList.add(new MjTablePropertyDefinition(p));
			}
		}
		
		@Override
		public Stream<PropertyDefinition<T, ?>> getProperties() {
			return defList.stream();
		}

		@Override
		public Optional<PropertyDefinition<T, ?>> getProperty(String name) {
			for (PropertyDefinition d : defList) {
				if (d.getName().equals(name)) {
					return Optional.of(d);
				}
			} 
			return Optional.empty();
		}
		
		private static class MjTablePropertyDefinition<T> implements PropertyDefinition {
			private static final long serialVersionUID = 1L;

			private final PropertyInterface property;
			
			public MjTablePropertyDefinition(PropertyInterface property) {
				this.property = property;
			}

			@Override
			public ValueProvider getGetter() {
				return new MjTableValueProvider();
			}

			@Override
			public Optional getSetter() {
				return Optional.of(new MjTableSetter());
			}

			@Override
			public Class getType() {
				return property.getClazz();
			}

			@Override
			public String getName() {
				return property.getPath();
			}

			@Override
			public String getCaption() {
				return Resources.getPropertyName(property);
			}

			@Override
			public PropertySet getPropertySet() {
				return null;
			}

			@Override
			public Class getPropertyHolderType() {
				return property.getDeclaringClass();
			}
			
			private class MjTableValueProvider implements ValueProvider {
				private static final long serialVersionUID = 1L;

				@Override
				public Object apply(Object source) {
					return property.getValue(source);
				}
			}
			
			private class MjTableSetter implements Setter {
				private static final long serialVersionUID = 1L;

				@Override
				public void accept(Object bean, Object fieldvalue) {
					property.setValue(bean, fieldvalue);
				}
			}
		}
	}
	
	@Override
	public void setObjects(List<T> objects) {
		setItems(objects);
	}
	
	private class MjTableRenderer extends TextRenderer {

		private final String propertyName;

		public MjTableRenderer(String propertyName) {
			this.propertyName = propertyName;
		}

		private static final long serialVersionUID = 1L;

		@Override
		public JsonValue encode(Object value) {
			PropertyInterface property = null;
			for (PropertyInterface p : properties) {
				if (p.getPath().equals(propertyName)) {
					property = p;
					break;
				}
			}
			
			value = Rendering.render(value, Rendering.RenderType.PLAIN_TEXT, property);
			return super.encode(value);
		}
	}
	
	private class VaadinTableItemClickListener implements ItemClickListener<T> {
		private static final long serialVersionUID = 1L;

		@Override
		public void itemClick(ItemClick<T> event) {
			if (event.getMouseEventDetails().isDoubleClick()) {
				listener.action(event.getItem());
			}			
		}
	}
	
//	private class VaadinTableActionHandler implements Handler {
//		private static final long serialVersionUID = 1L;
//
//		@Override
//		public Action[] getActions(Object target, Object sender) {
//			if (sender == VaadinTable.this) {
//				return new Action[]{action_delete, action_enter};
//			} else {
//				return null;
//			}
//		}
//
//		
//	}

}
