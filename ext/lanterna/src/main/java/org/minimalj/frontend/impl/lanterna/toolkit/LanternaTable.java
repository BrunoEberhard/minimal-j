package org.minimalj.frontend.impl.lanterna.toolkit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.minimalj.frontend.Frontend.ITable;
import org.minimalj.frontend.Frontend.TableActionListener;
import org.minimalj.frontend.impl.lanterna.LanternaGUIScreen;
import org.minimalj.frontend.impl.lanterna.component.HighContrastLanternaTheme;
import org.minimalj.model.Keys;
import org.minimalj.model.Rendering;
import org.minimalj.model.Rendering.RenderType;
import org.minimalj.model.properties.PropertyInterface;
import org.minimalj.util.resources.Resources;

import com.googlecode.lanterna.gui.TextGraphics;
import com.googlecode.lanterna.gui.component.AbstractInteractableComponent;
import com.googlecode.lanterna.input.Key;
import com.googlecode.lanterna.terminal.TerminalSize;

public class LanternaTable<T> extends AbstractInteractableComponent implements ITable<T> {
	private static final Logger logger = Logger.getLogger(LanternaTable.class.getName());

	private final List<PropertyInterface> properties;
	private List<T> objects = Collections.emptyList();
	private List<T> objectsSubList = Collections.emptyList();
	private final int[] columnWidthArray;
	private final String[] columnTitleArray;
	private int scrollIndex, lines;
	private final List<T> selectedObjects;
	private int selectedLine;
	private final TableActionListener<T> listener;
	
	public LanternaTable(Object[] keys, boolean multiSelect, TableActionListener<T> listener) {
		this.properties = convert(keys);
		this.listener = listener;

		selectedObjects = new ArrayList<>();
		
		columnTitleArray = new String[keys.length];
		for (int i = 0; i<properties.size(); i++) {
			PropertyInterface property = properties.get(i);
			columnTitleArray[i] = Resources.getPropertyName(property);

		}
		columnWidthArray = new int[keys.length];
		updateColumnWidths();
	}
	
	private void updateColumnWidths() {
		for (int i = 0; i<columnWidthArray.length; i++) {
			int width = columnTitleArray[i].length();
			for (int row = 0; row<objectsSubList.size(); row++) {
				String value = getValue(row, i);
				width = Math.max(width, value.length());
			}
			columnWidthArray[i] = Math.max(width, 1);
		}
	}
	
//	public static <T> LanternaTable<T> create(Class<T> clazz, Object[] keys) {
//		int[] columnWidthArray = new int[keys.length];
//		String[] columnTitleArray = new String[keys.length];
//		List<PropertyInterface> properties = convert(keys);
//		for (int i = 0; i<properties.size(); i++) {
//			PropertyInterface property = properties.get(i);
//			columnWidthArray[i] = Math.max(AnnotationUtil.getSize(property), 5);
//			columnTitleArray[i] = Resources.getPropertyName(property);
//		}
//		return new LanternaTable<T>(properties, columnWidthArray, columnTitleArray);
//	}
	
	private static List<PropertyInterface> convert(Object[] keys) {
		List<PropertyInterface> properties = new ArrayList<PropertyInterface>(keys.length);
		for (Object key : keys) {
			PropertyInterface property = Keys.getProperty(key);
			if (property != null) {
				properties.add(property);
			} else {
				logger.log(Level.WARNING, "Key not a property: " + key);
			}
		}
		if (properties.size() == 0) {
			logger.log(Level.SEVERE, "PropertyTable without valid keys");
		}
		return properties;
	}
	
	private void setLines(int lines) {
		lines = Math.min(lines, objects.size() - scrollIndex);
		if (lines != this.lines) {
			this.lines = lines;
			updateSubList();
		}
	}
	
	private void updateSubList() {
		this.objectsSubList = objects.subList(scrollIndex, scrollIndex+lines);
	}
	
	@Override
	public void repaint(TextGraphics graphics) {
		setLines(graphics.getHeight() - 1);
		
		
		StringBuilder s = new StringBuilder(graphics.getWidth());
		s.append("   ");
		for (int i = 0; i<properties.size(); i++) {
			s.append(' ');
			String value = columnTitleArray[i];
			int columnWidth = columnWidthArray[i];
			if (columnWidth < value.length()) {
				value = value.substring(0, columnWidth);
				s.append(value);
			} else {
				s.append(value);
				for (int j = value.length(); j < columnWidth; j++) {
					s.append(' ');
				}
			}
		}
		graphics.applyTheme(HighContrastLanternaTheme.TABLE_HEADER);
		graphics.drawString(0, 0, s.length() > graphics.getWidth() ? s.substring(0, graphics.getWidth()) : s.toString());
		
		int line = 0;
		while (line < objectsSubList.size()) {
			Object object = objectsSubList.get(line);
			s = new StringBuilder(graphics.getWidth());
			s.append('[');
			s.append(selectedObjects.contains(object) ? 'x' : ' ');
			s.append(']');
			
			for (int i = 0; i<properties.size(); i++) {
				s.append(' ');
				String value = getValue(line, i);
				int columnWidth = columnWidthArray[i];
				if (columnWidth < value.length()) {
					value = value.substring(0, columnWidth);
					s.append(value);
				} else {
					s.append(value);
					for (int j = value.length(); j < columnWidth; j++) {
						s.append(' ');
					}
				}
			}
			
			graphics.applyTheme(hasFocus() && selectedLine == line ? HighContrastLanternaTheme.TABLE_ROW_FOCUS : HighContrastLanternaTheme.TABLE_ROW);
			graphics.drawString(0, line + 1, s.length() > graphics.getWidth() ? s.substring(0, graphics.getWidth()) : s.toString());
			line++;
		}
	}

	protected String getValue(int row, int column) {
		PropertyInterface property = properties.get(column);
		Object value = property.getValue(objectsSubList.get(row));
		return Rendering.render(value, RenderType.PLAIN_TEXT, property);
	}

	// Interactable

	@Override
	public Result keyboardInteraction(Key key) {
		switch (key.getKind()) {
		case Enter:
			LanternaFrontend.setGui((LanternaGUIScreen) getWindow().getOwner());
			listener.action(getSelectedObject());
			LanternaFrontend.setGui(null);
			return Result.EVENT_HANDLED;

		case NormalKey:
			if (key.getCharacter() == ' ') {
				toggleSelection();
			}
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
	
	//
	
	private Result toggleSelection() {
		if (selectedLine >= objectsSubList.size()) {
			return Result.EVENT_NOT_HANDLED;
		}
		T object = objectsSubList.get(selectedLine);
		if (selectedObjects.contains(object)) {
			selectedObjects.remove(object);
		} else {
			selectedObjects.add(object);
		}
		return Result.EVENT_HANDLED;
	}
	
	private Result selectNext() {
		int selectedRow = scrollIndex + selectedLine;
		if (selectedRow == objects.size() - 1) {
			return Result.EVENT_NOT_HANDLED;
		} else if (selectedLine == lines-1) {
			scrollIndex = scrollIndex + lines;
			selectedLine = 0;
			updateSubList();
		} else {
			selectedLine++;
		}
		return Result.EVENT_HANDLED;
	}

	private Result selectPrevious() {
		int selectedRow = scrollIndex + selectedLine;
		if (selectedRow == 0) {
			return Result.EVENT_NOT_HANDLED;
		} else if (selectedLine == 0) {
			scrollIndex = scrollIndex - lines;
			if (scrollIndex < 0) scrollIndex = 0;
			selectedLine = lines - 1;
			if (selectedLine + scrollIndex >= objects.size()) {
				selectedLine = objects.size() - scrollIndex - 1;
			}
			updateSubList();
		} else {
			selectedLine--;
		}
		return Result.EVENT_HANDLED;
	}
	
	@Override
	public void setObjects(List<T> objects) {
		if (objects != null) {
			this.objects = objects;
		} else {
			this.objects = Collections.emptyList();
		}
		updateSubList();
		updateColumnWidths();
	}

	public List<T> getSelectedObjects() {
		return selectedObjects;
	}

	public T getSelectedObject() {
		return objectsSubList.get(selectedLine);
	}

	@Override
	protected TerminalSize calculatePreferredSize() {
		int width = 3;
		for (int columnWidth: columnWidthArray) {
			width += columnWidth + 1;
		}
		return new TerminalSize(width, objects.size() + 1);
	}
}
