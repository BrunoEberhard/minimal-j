package ch.openech.mj.lanterna.toolkit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.joda.time.ReadablePartial;

import ch.openech.mj.lanterna.component.HighContrastLanternaTheme;
import ch.openech.mj.model.Keys;
import ch.openech.mj.model.PropertyInterface;
import ch.openech.mj.resources.Resources;
import ch.openech.mj.search.Lookup;
import ch.openech.mj.toolkit.ITable;
import ch.openech.mj.util.JodaFormatter;

import com.googlecode.lanterna.gui.TextGraphics;
import com.googlecode.lanterna.gui.component.AbstractInteractableComponent;
import com.googlecode.lanterna.input.Key;
import com.googlecode.lanterna.terminal.TerminalSize;

public class LanternaTable<T> extends AbstractInteractableComponent implements ITable<T> {
	private static final Logger logger = Logger.getLogger(LanternaTable.class.getName());

	private static final JodaFormatter formatter = new JodaFormatter();
	
	private final Lookup<T> lookup;
	private final Object[] keys;
	private final List<PropertyInterface> properties;
	private List<Integer> ids = Collections.emptyList();
	private final int[] columnWidthArray;
	private final String[] columnTitleArray;
	private int scrollIndex, lines;
	private final List<T> selectedObjects;
	private int selectedLine;
	private InsertListener insertListener;
	private TableActionListener<T> clickListener, deleteListener;
	
	public LanternaTable(Lookup<T> lookup, Object[] keys) {
		this.lookup = lookup;
		this.keys = keys;
		this.properties = convert(keys);
		selectedObjects = new ArrayList<T>();
		
		columnTitleArray = new String[keys.length];
		for (int i = 0; i<properties.size(); i++) {
			PropertyInterface property = properties.get(i);
			columnTitleArray[i] = Resources.getObjectFieldName(Resources.getResourceBundle(), property);

		}
		columnWidthArray = new int[keys.length];
		updateColumnWidths();
	}
	
	private void updateColumnWidths() {
		for (int i = 0; i<columnWidthArray.length; i++) {
			int width = columnTitleArray[i].length();
			for (int row = 0; row<ids.size(); row++) {
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
//			columnTitleArray[i] = Resources.getObjectFieldName(Resources.getResourceBundle(), property);
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
	
	@Override
	public void repaint(TextGraphics graphics) {
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
		while (line < graphics.getHeight()-1 && line + scrollIndex < ids.size()) {
			Object object = getObject(line + scrollIndex);
			s = new StringBuilder(graphics.getWidth());
			s.append('[');
			s.append(selectedObjects.contains(object) ? 'x' : ' ');
			s.append(']');
			
			for (int i = 0; i<properties.size(); i++) {
				s.append(' ');
				String value = getValue(line + scrollIndex, i);
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
		
		lines = graphics.getHeight() - 1;
	}

	private T getObject(int index) {
		int id = ids.get(index);
		return lookup.lookup(id);
	}
	
	protected String getValue(int row, int column) {
		Object value = properties.get(column).getValue(getObject(row));
		if (value instanceof ReadablePartial) {
			PropertyInterface property = properties.get(column);
			value = formatter.format(value, property);
		}
		return "" + value;
	}

	// Interactable

	@Override
	public Result keyboardInteraction(Key key) {
		switch (key.getKind()) {
		case Enter:
			clickListener.action(getSelectedObject(), getSelectedObjects());
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
		int selectedRow = scrollIndex + selectedLine;
		if (selectedRow >= ids.size()) {
			return Result.EVENT_NOT_HANDLED;
		}
		T object = getObject(selectedRow);
		if (selectedObjects.contains(object)) {
			selectedObjects.remove(object);
		} else {
			selectedObjects.add(object);
		}
		return Result.EVENT_HANDLED;
	}
	
	private Result selectNext() {
		int selectedRow = scrollIndex + selectedLine;
		if (selectedRow == ids.size() - 1) {
			return Result.EVENT_NOT_HANDLED;
		} else if (selectedLine == lines-1) {
			scrollIndex = scrollIndex + lines;
			selectedLine = 0;
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
			if (selectedLine + scrollIndex >= ids.size()) {
				selectedLine = ids.size() - scrollIndex - 1;
			}
		} else {
			selectedLine--;
		}
		return Result.EVENT_HANDLED;
	}
	
	@Override
	public void setIds(List<Integer> objects) {
		if (objects != null) {
			this.ids = objects;
		} else {
			this.ids = Collections.emptyList();
		}
		updateColumnWidths();
	}

	public List<T> getSelectedObjects() {
		return selectedObjects;
	}

	public T getSelectedObject() {
		return getObject(scrollIndex + selectedLine);
	}

	@Override
	public void setClickListener(TableActionListener<T> listener) {
		this.clickListener = listener;
	}

	@Override
	public void setDeleteListener(TableActionListener<T> listener) {
		this.deleteListener = listener;
	}

	@Override
	public void setInsertListener(InsertListener listener) {
		this.insertListener = listener;
	}

	@Override
	public void setFunctionListener(int function, TableActionListener<T> listener) {
		// TODO Function Action in Lanterna Table
	}

	@Override
	protected TerminalSize calculatePreferredSize() {
		int width = 3;
		for (int columnWidth: columnWidthArray) {
			width += columnWidth + 1;
		}
		return new TerminalSize(width, ids.size() + 1);
	}
}
