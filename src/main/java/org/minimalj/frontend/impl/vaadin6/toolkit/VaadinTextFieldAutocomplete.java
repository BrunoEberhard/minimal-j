package org.minimalj.frontend.impl.vaadin6.toolkit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.minimalj.frontend.Frontend.IComponent;
import org.minimalj.frontend.Frontend.Input;
import org.minimalj.frontend.Frontend.InputComponentListener;
import org.minimalj.util.StringUtils;

import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;

public class VaadinTextFieldAutocomplete extends ComboBox implements IComponent {
	private static final long serialVersionUID = 1L;

	private final InputComponentListener listener;
	private final List<String> choice;
	private String filterstring;
	
	public VaadinTextFieldAutocomplete(List<String> choice, InputComponentListener listener) {
		this.listener = listener;
		this.choice = choice;
		addListener(new ComboBoxChangeListener());

		setImmediate(true);
		setNullSelectionAllowed(true);
	}

    @Override
    public void changeVariables(Object source, Map<String, Object> variables) {
    	filterstring = (String) variables.get("filter");
        super.changeVariables(source, variables);
    }
	
	@Override
	protected List<?> getFilteredOptions() {
		if (!StringUtils.isBlank(filterstring)) {
			List<String> items = new ArrayList<>();
			for (String i : choice) {
				if (i != null && i.startsWith(filterstring)) {
					items.add(i);
				}
			}
			if (items.isEmpty()) {
				return Collections.singletonList(filterstring);
			} else if (items.contains(filterstring)) {
				return items;
			} else {
				List<String> itemsPlusFilter = new ArrayList<String>(items.size() + 1);
				itemsPlusFilter.add(filterstring);
				itemsPlusFilter.addAll(items);
				return itemsPlusFilter;
			}
		} else {
			return Collections.emptyList();
		}
	}

	public class ComboBoxChangeListener implements ValueChangeListener {

		private static final long serialVersionUID = 1L;

		@Override
		public void valueChange(com.vaadin.data.Property.ValueChangeEvent event) {
			listener.changed(VaadinTextFieldAutocomplete.this);
		}
	}
	
	public static class VaadinTextAutocompleteDelegate implements Input<String>, VaadinDelegateComponent {
		
		private final VaadinTextFieldAutocomplete delegate;
		
		public VaadinTextAutocompleteDelegate(List<String> choice, InputComponentListener listener) {
			this.delegate = new VaadinTextFieldAutocomplete(choice, listener);
		}

		@Override
		public void setValue(String value) {
			delegate.setValue(value);
		}

		@Override
		public String getValue() {
			return (String) delegate.getValue();
		}

		@Override
		public void setEditable(boolean editable) {
			delegate.setReadOnly(!editable);
		}
		
		@Override
		public Component getDelegate() {
			return delegate;
		}
	}
}