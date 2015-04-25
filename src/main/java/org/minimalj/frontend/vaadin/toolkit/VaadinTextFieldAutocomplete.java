package org.minimalj.frontend.vaadin.toolkit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.minimalj.frontend.toolkit.ClientToolkit.IComponent;
import org.minimalj.frontend.toolkit.ClientToolkit.InputComponentListener;
import org.minimalj.frontend.toolkit.ClientToolkit.Search;
import org.minimalj.frontend.toolkit.TextField;
import org.minimalj.util.StringUtils;

import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;

public class VaadinTextFieldAutocomplete extends ComboBox implements IComponent {
	private static final long serialVersionUID = 1L;

	private final InputComponentListener listener;
	private final Search<String> autocomplete;
	private String filterstring;
	
	public VaadinTextFieldAutocomplete(Search<String> autocomplete, InputComponentListener listener) {
		this.listener = listener;
		this.autocomplete = autocomplete;
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
			List<String> items = autocomplete.search(filterstring);
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
	
	public static class VaadinTextAutocompleteDelegate implements TextField, VaadinDelegateComponent {
		
		private final VaadinTextFieldAutocomplete delegate;
		
		public VaadinTextAutocompleteDelegate(Search<String> autocomplete, InputComponentListener listener) {
			this.delegate = new VaadinTextFieldAutocomplete(autocomplete,listener);
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
		public void setCommitListener(Runnable runnable) {
			// TODO Auto-generated method stub
		}
		
		@Override
		public Component getDelegate() {
			return delegate;
		}
	}
}