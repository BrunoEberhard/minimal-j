package org.minimalj.frontend.impl.vaadin.toolkit;

import java.util.List;

import org.minimalj.frontend.Frontend.IContent;
import org.minimalj.frontend.Frontend.Search;
import org.minimalj.frontend.Frontend.TableActionListener;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;

public class VaadinSearchPanel<T> extends VerticalLayout implements IContent, VaadinComponentWithWidth {
	private static final long serialVersionUID = 1L;
	private final TextField text;
	private final Button searchButton;
	private final VaadinTable<T> table;
	private final int width;
	
	public VaadinSearchPanel(final Search<T> search, Object[] keys, boolean multiSelect, TableActionListener<T> listener) {
		setMargin(false);
		setSpacing(false);
		setSizeFull();
		
		text = new TextField();
		searchButton = new Button("Search");
		table = new VaadinTable<>(keys, multiSelect, listener);

		HorizontalLayout northPanel = new HorizontalLayout();
		northPanel.setWidth("100%");
		northPanel.addAndExpand(text);
		northPanel.add(searchButton);
		text.setWidth("100%");
				
		add(northPanel);
		table.setSizeFull();
		addAndExpand(table);

		searchButton.addClickListener(new ComponentEventListener<ClickEvent<Button>>() {
			private static final long serialVersionUID = 1L;

			@Override
			public void onComponentEvent(ClickEvent<Button> event) {
				List<T> objects = search.search((String) text.getValue());
				table.setObjects(objects);
			}
		});
		
		width = keys.length * 20;
	}
	
	@Override
	public int getDialogWidth() {
		return width;
	}

}
