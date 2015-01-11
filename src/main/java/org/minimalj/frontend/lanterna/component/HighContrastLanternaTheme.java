package org.minimalj.frontend.lanterna.component;

import com.googlecode.lanterna.gui.Theme;
import com.googlecode.lanterna.terminal.Terminal.Color;

public class HighContrastLanternaTheme extends Theme {
    private static final Definition DEFAULT = new Definition(Color.GREEN, Color.BLACK, false);
    private static final Definition BRIGHT = new Definition(Color.GREEN, Color.BLACK, true);

    public static final Definition TABLE_HEADER = new Definition(Color.GREEN, Color.BLACK, true);
    public static final Definition TABLE_ROW = new Definition(Color.GREEN, Color.BLACK, false);
    public static final Definition TABLE_ROW_FOCUS = new Definition(Color.YELLOW, Color.BLUE, false);
    
    public HighContrastLanternaTheme() {
        setDefinition(Category.DIALOG_AREA, DEFAULT);
        setDefinition(Category.SCREEN_BACKGROUND, DEFAULT);
        setDefinition(Category.SHADOW, new Definition(Color.BLACK, Color.BLACK, true));
        setDefinition(Category.BORDER, DEFAULT);
        setDefinition(Category.RAISED_BORDER, BRIGHT);
        setDefinition(Category.BUTTON_LABEL_ACTIVE, BRIGHT);
        setDefinition(Category.BUTTON_LABEL_INACTIVE, DEFAULT);
        setDefinition(Category.BUTTON_ACTIVE, BRIGHT);
        setDefinition(Category.BUTTON_INACTIVE, DEFAULT);
        setDefinition(Category.LIST_ITEM, DEFAULT);
        setDefinition(Category.LIST_ITEM_SELECTED, BRIGHT);
        setDefinition(Category.CHECKBOX, DEFAULT);
        setDefinition(Category.CHECKBOX_SELECTED, BRIGHT);
        setDefinition(Category.TEXTBOX, BRIGHT);
        setDefinition(Category.TEXTBOX_FOCUSED, new Definition(Color.YELLOW, Color.BLUE, true));
        setDefinition(Category.PROGRESS_BAR_COMPLETED, new Definition(Color.GREEN, Color.BLACK, false));
        setDefinition(Category.PROGRESS_BAR_REMAINING, new Definition(Color.RED, Color.BLACK, false));
    }
    
}
