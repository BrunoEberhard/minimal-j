package org.minimalj.frontend.impl.vaadin.toolkit;

import java.util.List;

import org.minimalj.frontend.Frontend.IContent;
import org.minimalj.frontend.Frontend.Search;
import org.minimalj.frontend.Frontend.TableActionListener;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

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
		table = new VaadinTable<T>(keys, multiSelect, listener);

		HorizontalLayout northPanel = new HorizontalLayout();
		northPanel.setWidth("100%");
		northPanel.addComponent(text);
		northPanel.addComponent(searchButton);
		text.setWidth("100%");
		northPanel.setComponentAlignment(text, Alignment.MIDDLE_LEFT);
		northPanel.setExpandRatio(text, 1.0f);
				
		addComponent(northPanel);
		setExpandRatio(northPanel, 0.0f);
		table.setSizeFull();
		addComponent(table);
		setExpandRatio(table, 1.0f);

		searchButton.addClickListener(new ClickListener() {
			private static final long serialVersionUID = 1L;
			@Override
			public void buttonClick(ClickEvent event) {
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
