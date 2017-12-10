/*
 * This file is part of lanterna.
 * 
 * lanterna is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * Copyright (C) 2010-2017 Martin Berglund
 * Copyright (C) 2017 Bruno Eberhard
 */
package com.googlecode.lanterna.gui2.menu;

import com.googlecode.lanterna.gui2.Direction;
import com.googlecode.lanterna.gui2.LinearLayout;
import com.googlecode.lanterna.gui2.Panel;

/**
 * A menubar offering drop-down menus.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 * @author Bruno Eberhard
 * @version $Revision$
 */
public class MenuBar extends Panel {

    /**
     * Initializes the menubar.
     */
    public MenuBar() {
        super(new LinearLayout(Direction.HORIZONTAL));
    }

    /**
     * Adds the menu at the end.
     *
     * @param menu the menu to add
     */
    public void addMenu(Menu menu) {
        addComponent(menu);
    }
}
