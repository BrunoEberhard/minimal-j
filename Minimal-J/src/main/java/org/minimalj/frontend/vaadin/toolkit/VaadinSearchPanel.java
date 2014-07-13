package org.minimalj.frontend.vaadin.toolkit;

import java.util.List;

import org.minimalj.frontend.toolkit.ClientToolkit.IComponent;
import org.minimalj.frontend.toolkit.ClientToolkit.Search;
import org.minimalj.frontend.toolkit.ITable.TableActionListener;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

public class VaadinSearchPanel<T> extends VerticalLayout implements IComponent, VaadinComponentWithWidth {
	private static final long serialVersionUID = 1L;
	private final TextField text;
	private final Button searchButton;
	private final VaadinTable<T> table;
	private final int width;
	
	public VaadinSearchPanel(final Search<T> search, Object[] keys, TableActionListener<T> listener) {
		setSizeFull();
		
		text = new TextField();
		searchButton = new Button("Search");
		table = new VaadinTable<T>(keys);

		HorizontalLayout northPanel = new HorizontalLayout();
		northPanel.setWidth("100%");
		northPanel.addComponent(text);
		northPanel.addComponent(searchButton);
		text.setWidth("100%");
		northPanel.setExpandRatio(text, 1.0f);
				
		addComponent(northPanel);
		addComponent(table);
		setExpandRatio(table, 1.0f);

		searchButton.addListener(new ClickListener() {
			private static final long serialVersionUID = 1L;
			@Override
			public void buttonClick(ClickEvent event) {
				List<T> objects = search.search((String) text.getValue());
				table.setObjects(objects);
			}
		});
		
		table.setClickListener(listener);
		
		width = keys.length * 20;
	}
	
	@Override
	public int getDialogWidth() {
		return width;
	}

}
