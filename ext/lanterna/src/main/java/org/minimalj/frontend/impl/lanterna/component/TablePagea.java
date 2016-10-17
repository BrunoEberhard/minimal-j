package org.minimalj.frontend.impl.lanterna.component;

import java.util.Arrays;

import com.googlecode.lanterna.gui.Action;
import com.googlecode.lanterna.gui.Border;
import com.googlecode.lanterna.gui.Component;
import com.googlecode.lanterna.gui.component.Button;
import com.googlecode.lanterna.gui.component.EmptySpace;
import com.googlecode.lanterna.gui.component.Label;
import com.googlecode.lanterna.gui.component.Panel;
import com.googlecode.lanterna.gui.component.Table;

public abstract class TablePagea extends Panel {

	private static final int CMD_COLUMN_WIDTH = 5;
	private static final String CMD_COLUMN_NEW = "ADD";
	private static final String CMD_COLUMN_EDIT = "EDIT";

	private String title;
	private int[] columnWidthArray;
	private String[] columnTitleArray;
	boolean allowNew;
	boolean allowEdit;
	boolean showCmdColumn;
	int colOffset;
	int columnCount;

	private Button btnPrev;
	private Label lblPage;
	private Button btnNext;
	private Button btnNew;

	private Panel pnlShuttle;
	private Table tbl;

	private int rowsPerPage;
	private int currentIdx;
	private Component[] rowHeaderArray;
	private Component[] rowDivArray;

	public TablePagea(String title, int[] columnWidthArray,
			String[] columnTitleArray) {
		this(title, columnWidthArray, columnTitleArray, 10, true, true);
	}

	public TablePagea(int[] columnWidthArray, String[] columnTitleArray) {
		this("", columnWidthArray, columnTitleArray, 10, true, true);
	}

	public TablePagea(String title, int[] columnWidthArray,
			String[] columnTitleArray, int rowsPerPage, boolean allowNew,
			boolean allowEdit) {
		if (title == null) {
			title = "";
		}
		if (columnWidthArray == null) {
			throw new IllegalArgumentException(
					"ERROR: you must specify an array of column widths");
		}
		if (columnTitleArray == null) {
			throw new IllegalArgumentException(
					"ERROR: you must specify an array of column titles");
		}
		if (columnWidthArray.length != columnTitleArray.length) {
			throw new IllegalArgumentException(
					"ERROR: there must be the same number of column widths as column titles.");
		}
		this.title = title;
		this.columnWidthArray = columnWidthArray;
		this.columnTitleArray = columnTitleArray;
		this.rowsPerPage = rowsPerPage;
		this.allowNew = allowNew;
		this.allowEdit = allowEdit;
		this.showCmdColumn = allowNew || allowEdit;
		this.columnCount = columnWidthArray.length
				+ (this.showCmdColumn ? 1 : 0);
		this.colOffset = this.showCmdColumn ? 1 : 0;
		init();
	}

	private void init() {
		setBorder(new Border.Standard());
		pnlShuttle = new Panel(Panel.Orientation.HORISONTAL);
		pnlShuttle.setBetweenComponentsPadding(1);
		btnPrev = new Button("PREV", new Action() {
			@Override
			public void doAction() {
				int newIdx = (int) Math
						.floor((double) currentIdx / rowsPerPage)
						* rowsPerPage
						- rowsPerPage;
				if (newIdx >= 0) {
					currentIdx = newIdx;
					updateTable();
				}
			}
		});
		lblPage = new Label("Page 1  ");
		btnNext = new Button("NEXT", new Action() {
			@Override
			public void doAction() {
				int newIdx = (int) Math
						.floor((double) currentIdx / rowsPerPage)
						* rowsPerPage
						+ rowsPerPage;
				if (newIdx < getRowCount()) {
					currentIdx = newIdx;
					updateTable();
				}
			}
		});
		pnlShuttle.addComponent(btnPrev);
		pnlShuttle.addComponent(lblPage);
		pnlShuttle.addComponent(btnNext);
		addComponent(pnlShuttle);
		tbl = new Table(columnCount, title);
		tbl.setColumnPaddingSize(1);
		//
		rowHeaderArray = new Component[columnCount];
		rowDivArray = new Label[columnCount];
		for (int i = 0; i < columnCount; i++) {
			if (i == 0 && showCmdColumn) {
				if (allowNew) {
					rowHeaderArray[i] = new Button(CMD_COLUMN_NEW,
							new Action() {
								@Override
								public void doAction() {
									actionNew();
								}
							});
				} else {
					rowHeaderArray[i] = new EmptySpace();
				}
				rowDivArray[i] = new Label(fillString(CMD_COLUMN_WIDTH, "="));
				continue;
			}
			rowHeaderArray[i] = new Label(trim(columnWidthArray[i - colOffset],
					columnTitleArray[i - colOffset]));
			rowDivArray[i] = new Label(fillString(columnWidthArray[i
					- colOffset], "="));
		}
		//
		addComponent(tbl);
		//
		currentIdx = 0;
		updateTable();
	}

	private String padPage(int length, String str) {
		if (str == null) {
			str = "";
		}
		while (str.length() < length) {
			str = str + " ";
		}
		return str;
	}

	private String trim(int length, String str) {
		if (str == null) {
			return "";
		}
		if (str.length() > length) {
			str = str.substring(0, length);
		}
		return str;
	}

	private String fillString(int length, String str) {
		if (length < 1) {
			return "";
		}
		if (str == null || str.isEmpty()) {
			str = " ";

		}
		char[] chars = new char[length];
		Arrays.fill(chars, str.charAt(0));
		return new String(chars);
	}

	private Component[] getEmptyRow() {
		Component[] componentArray = new Component[columnWidthArray.length];
		for (int i = 0; i < columnWidthArray.length; i++) {
			componentArray[i] = new EmptySpace();
		}
		return componentArray;
	}

	public void updateTable() {
		if (currentIdx >= getRowCount()) {
			currentIdx = getRowCount() - 1;
		}
		if (currentIdx < 0) {
			currentIdx = 0;
		}
		int currentPage = (int) Math.floor((double) currentIdx / rowsPerPage);
		int maxPage = (int) Math.floor((double) (getRowCount() - 1)
				/ rowsPerPage);
		lblPage.setText(padPage(17, "Page " + (currentPage + 1) + " of "
				+ (maxPage + 1)));
		tbl.removeAllRows();
		tbl.addRow(rowHeaderArray);
		tbl.addRow(rowDivArray);
		int idx = currentPage * rowsPerPage;
		for (int pageIdx = 0; pageIdx < rowsPerPage; pageIdx++) {
			Component[] comAry = null;
			if (idx < getRowCount()) {
				comAry = renderRow(idx, pageIdx);
				if (showCmdColumn) {
					Component[] tmpAry = comAry;
					comAry = new Component[tmpAry.length + 1];
					if (allowEdit) {
						final int idxf = idx;
						final int pageIdxf = pageIdx;
						comAry[0] = new Button(CMD_COLUMN_EDIT, new Action() {
							@Override
							public void doAction() {
								actionEdit(idxf, pageIdxf);
							}
						});
					} else {
						comAry[0] = new EmptySpace();
					}
					for (int i = 1; i < comAry.length; i++) {
						comAry[i] = tmpAry[i - 1];
					}
				}
			}
			if (comAry == null) {
				comAry = getEmptyRow();
			}
			tbl.addRow(comAry);
			idx++;
		}
	}

	//

	public abstract int getRowCount();

	public abstract Component[] renderRow(int idx, int pageIdx);

	public abstract void actionEdit(int idx, int pageIdx);

	public abstract void actionNew();

	//

	public int getColumnCount() {
		return this.columnCount;
	}

	public Component getBtnPrev() {
		return btnPrev;
	}

}
