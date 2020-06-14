package org.minimalj.frontend.impl.vaadin.toolkit;

import java.util.List;

import org.minimalj.frontend.Frontend.IContent;
import org.minimalj.frontend.Frontend.Search;
import org.minimalj.frontend.Frontend.TableActionListener;
import org.minimalj.util.resources.Resources;

import com.vaadin.flow.component.Key;
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
		setPadding(false);

        text = new TextField();
        text.focus();

        searchButton = new Button(Resources.getString("SearchAction"));
        searchButton.setWidth("20em");
        table = new VaadinTable<>(keys, multiSelect, listener);

        HorizontalLayout northPanel = new HorizontalLayout();
        northPanel.setWidth("100%");
        northPanel.addAndExpand(text);
        northPanel.add(searchButton);
        text.setWidth("100%");

        add(northPanel);
        table.setHeight("50vh");
        table.setWidth("70vw");
        addAndExpand(table);

        searchButton.addClickListener(event -> setObjects(search));
        text.addKeyPressListener(Key.ENTER, event -> setObjects(search));

        width = keys.length * 20;
    }

    private void setObjects(Search<T> search) {
        List<T> objects = search.search((String) text.getValue());
        table.setObjects(objects);
        if (objects.size() == 1 && table.getSelectedItems().isEmpty()) {
            table.select(objects.get(0));
        }
    }

    @Override
    public int getDialogWidth() {
        return width;
    }

}
