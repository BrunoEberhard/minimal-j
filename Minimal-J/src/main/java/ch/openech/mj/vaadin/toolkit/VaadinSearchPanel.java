package ch.openech.mj.vaadin.toolkit;

import ch.openech.mj.search.Search;
import ch.openech.mj.toolkit.IComponent;
import ch.openech.mj.toolkit.ITable.TableActionListener;

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
	private final VaadinTable table;
	private final int width;
	
	public VaadinSearchPanel(final Search<T> search, TableActionListener listener) {
		setSizeFull();
		
		text = new TextField();
		searchButton = new Button("Search");
		table = new VaadinTable(search.getKeys());

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
				table.setObjects(search.search((String) text.getValue()));
			}
		});
		
		table.setClickListener(listener);
		
		width = search.getKeys().length * 20;
	}
	
	@Override
	public int getDialogWidth() {
		return width;
	}

}
