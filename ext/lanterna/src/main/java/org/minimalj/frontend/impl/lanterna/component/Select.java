/*
 * This file is part of minimal-j (http://code.google.com/p/minimal-j/).
 * 
 * minimal-j is free software: you can redistribute it and/or modify
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
 * Copyright (C) 2013 Bruno Eberhard
 */
package org.minimalj.frontend.impl.lanterna.component;

import java.util.List;

import org.minimalj.util.StringUtils;

import com.googlecode.lanterna.gui.Interactable;
import com.googlecode.lanterna.gui.TextGraphics;
import com.googlecode.lanterna.gui.Theme;
import com.googlecode.lanterna.gui.component.AbstractInteractableComponent;
import com.googlecode.lanterna.gui.component.Label;
import com.googlecode.lanterna.gui.dialog.ListSelectDialog;
import com.googlecode.lanterna.input.Key;
import com.googlecode.lanterna.terminal.TerminalSize;

/**
 * 
 * @author Bruno Eberhard
 */
public class Select<T> extends AbstractInteractableComponent {
	private List<T> objects;
	private T selectedObject;
	private final Label label;
	private String dialogTitle;
	private String dialogDescription;
	
	public Select() {
		setAlignment(Alignment.FILL);
		label = new Label();
	}
	
	public String getDialogTitle() {
		return dialogTitle;
	}

	public void setDialogTitle(String dialogTitle) {
		this.dialogTitle = dialogTitle;
	}

	public String getDialogDescription() {
		return dialogDescription;
	}

	public void setDialogDescription(String dialogDescription) {
		this.dialogDescription = dialogDescription;
	}

	@Override
	public void repaint(TextGraphics graphics) {
		String text = selectedObject != null ? selectedObject.toString() : "";
		text = StringUtils.padRight(text, graphics.getWidth(), ' ');
		text = text.substring(0, graphics.getWidth());
		label.setText(text);
		
		if (hasFocus())
			graphics.applyTheme(graphics.getTheme().getDefinition(
					Theme.Category.BUTTON_ACTIVE));
		else
			graphics.applyTheme(graphics.getTheme().getDefinition(
					Theme.Category.BUTTON_INACTIVE));

		TerminalSize preferredSize = calculatePreferredSize();
		graphics = transformAccordingToAlignment(graphics, preferredSize);

		label.repaint(graphics);
		setHotspot(null);
	}

	public T getSelectedObject() {
		return selectedObject;
	}

	public void setSelectedObject(T selectedObject) {
		this.selectedObject = selectedObject;
	}

	public void setObjects(List<T> objects) {
		this.objects = objects;
	}
	
	@Override
	protected TerminalSize calculatePreferredSize() {
		int width = 1;
		for (T t : objects) {
			width = Math.max(width, t.toString().length());
		}
		return new TerminalSize(width, 1);
	}

	@Override
	public void afterEnteredFocus(FocusChangeDirection direction) {
		label.setStyle(Theme.Category.TEXTBOX_FOCUSED);
	}

	@Override
	public void afterLeftFocus(FocusChangeDirection direction) {
		label.setStyle(Theme.Category.TEXTBOX);
	}

	@Override
	public Interactable.Result keyboardInteraction(Key key) {
		switch (key.getKind()) {
		case Enter:
			showSelectDialog();
			return Result.EVENT_HANDLED;

		case ArrowDown:
			return selectNext();

		case ArrowRight:
		case Tab:
			return Result.NEXT_INTERACTABLE_RIGHT;

		case ArrowUp:
			return selectPrevious();

		case ArrowLeft:
		case ReverseTab:
			return Result.PREVIOUS_INTERACTABLE_LEFT;

		default:
			return Result.EVENT_NOT_HANDLED;
		}
	}

	private Result selectNext() {
		if (selectedObject != null) {
			int index = objects.indexOf(selectedObject) + 1;
			if (index < objects.size()) {
				selectedObject = objects.get(index);
				return Result.EVENT_HANDLED;
			} 
		} 
		if (objects.size() > 0) {
			selectedObject = objects.get(0);
			return Result.EVENT_HANDLED;
		} else {
			return Result.NEXT_INTERACTABLE_DOWN;
		}
	}

	private Result selectPrevious() {
		if (selectedObject != null) {
			int index = objects.indexOf(selectedObject) - 1;
			if (index >= 0) {
				selectedObject = objects.get(index);
				return Result.EVENT_HANDLED;
			}
		} 
		if (objects.size() > 0) {
			selectedObject = objects.get(objects.size()-1);
			return Result.EVENT_HANDLED;
		} else {
			return Result.PREVIOUS_INTERACTABLE_UP;
		}
	}

	private void showSelectDialog() {
		Object[] objectArray = objects.toArray(new Object[objects.size()]);
		@SuppressWarnings("unchecked")
		T selectedObject = (T) ListSelectDialog.showDialog(getGUIScreen(), dialogTitle, dialogDescription, objectArray);
		if (selectedObject != null) {
			this.selectedObject = selectedObject;
		}
	}
}
