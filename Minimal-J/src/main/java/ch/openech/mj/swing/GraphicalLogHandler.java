package ch.openech.mj.swing;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JToggleButton;
import javax.swing.JViewport;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;

import ch.openech.mj.util.StringUtils;


public class GraphicalLogHandler extends Handler {

	private JFrame frame;
	private JPanel filterPanel;
	private JTable table;
	private GraphicalLogHandlerTableModel tableModel;
	private List<LogRecord> logRecords = new ArrayList<LogRecord>();
	private DateFormat dateFormat;
	
	public GraphicalLogHandler() {
		setLevel(Level.ALL);
		
		tableModel = new GraphicalLogHandlerTableModel();
		dateFormat = DateFormat.getDateTimeInstance();
		
		frame = new JFrame("Logging");
		frame.setLayout(new BorderLayout());
		
		filterPanel = new JPanel(new FlowLayout(FlowLayout.LEADING));
		filterPanel.setAlignmentX(0);
		JButton clearButton = new JButton("Log löschen");
		clearButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				tableModel.clear();
			}
		});
		filterPanel.add(clearButton);
		
		table = new JTable(tableModel);
		table.setFillsViewportHeight(true);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		
		table.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
		table.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				updateFirstColumnSize(table);
				scrollToBottom();
			}
		});
		
		final JTextArea messageArea = new JTextArea();
		messageArea.setEditable(false);
		messageArea.setLineWrap(true);
		messageArea.setWrapStyleWord(true);
		
		table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				int selectedRow = table.getSelectedRow();
				if (selectedRow > -1) {
					String message = (String)tableModel.getValueAt(selectedRow, 1);
					messageArea.setText(message);
				} else {
					messageArea.setText(null);
				}
			}
		});
		
		final JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, new JScrollPane(table), new JScrollPane(messageArea));
		splitPane.setBorder(null);
		splitPane.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				splitPane.setDividerLocation(Math.min(splitPane.getHeight() - 100, splitPane.getHeight() * 2 / 3));
			}
		});
		
		frame.add(filterPanel, BorderLayout.NORTH);
		frame.add(splitPane, BorderLayout.CENTER);

		moveToLowerRight(frame);
		
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		setVisible(PreferencesHelper.preferences().getBoolean("showLog", false));
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				PreferencesHelper.preferences().putBoolean("showLog", false);
			}
		});
	}
	
	private void moveToLowerRight(JFrame frame) {
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		int width = screenSize.width / 3;
		int height = screenSize.height / 3;
		frame.setBounds(screenSize.width - width - 30, screenSize.height - height - 40, width, height);
	}
	
	public void addFilterButton(String text, Icon icon, String pakage) {
		JToggleButton toggleButton = new JToggleButton(text, icon);
		toggleButton.setSelected(true);
		toggleButton.putClientProperty("pakage", pakage);
		filterPanel.add(toggleButton);
	}
	
	@SuppressWarnings("deprecation")
	private void updateFirstColumnSize(JTable table) {
		TableColumn firstColumn = table.getColumnModel().getColumn(0);
		
		int width = StringUtils.pixelWidth(table.getGraphics(), dateFormat.format(new Date(188, 9, 28, 23, 59, 59)), table) + 10;
		firstColumn.setMinWidth(width);
		firstColumn.setMaxWidth(width);
		firstColumn.setPreferredWidth(width);
	}
	
	@Override
	public void close() throws SecurityException {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				logRecords.clear();
				frame.setVisible(false);
			}
		});
	}

	@Override
	public void flush() {
		// nothing to do
	}

	@Override
	public void publish(final LogRecord record) {
		String loggerName = record.getLoggerName();
		if (!StringUtils.isEmpty(loggerName) && !buttonSelected(loggerName)) return;
		if (!SwingUtilities.isEventDispatchThread()) {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					tableModel.add(record);
				}
			});
		} else {
			tableModel.add(record);
		}
	}
	
	private boolean buttonSelected(String loggerName) {
		for (Component c : filterPanel.getComponents()) {
			if (!(c instanceof JToggleButton)) continue;
			JToggleButton toggleButton = (JToggleButton)c;
			String pakage = (String)toggleButton.getClientProperty("pakage");
			if (toggleButton.isSelected() && loggerName.startsWith(pakage)) {
				return true;
			}
		}
		return false;
	}
	
	private class GraphicalLogHandlerTableModel extends AbstractTableModel {

		@Override
		public String getColumnName(int column) {
			if (column == 0) return "Zeit";
			else return "Meldung";
		}

		@Override
		public int getColumnCount() {
			return 2;
		}

		public void add(LogRecord record) {
			// TODO Log size configuration
			if (logRecords.size() < 100) 	{
				logRecords.add(record);
				fireTableRowsInserted(logRecords.size()-1, logRecords.size()-1);
				scrollToBottom();
			} else {
				logRecords.remove(0);
				logRecords.add(record);
				fireTableDataChanged();
				scrollToBottom();
			}
		}

		@Override
		public int getRowCount() {
			return logRecords.size();
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			LogRecord logRecord = logRecords.get(rowIndex);
			if (columnIndex == 0) {
				return dateFormat.format(new Date(logRecord.getMillis()));
			} else {
				return logRecord.getMessage();
			}
		}
		
		public void clear() {
			int length = logRecords.size();
			logRecords.clear();
			fireTableRowsDeleted(0, length);
		}
		
	}
	
	// http://www.exampledepot.com/egs/javax.swing.table/Vis.html
	public void scrollToBottom() {
		JViewport viewport = (JViewport)table.getParent();
		Rectangle rect = table.getCellRect(tableModel.getRowCount()-1, 0, true);
		Point pt = viewport.getViewPosition(); 
		rect.setLocation(rect.x-pt.x, rect.y-pt.y);
		viewport.scrollRectToVisible(rect); 
	}

	//
	
	public void setVisible(boolean visible) {
		if (frame.isVisible() == visible) return;
		
		frame.setVisible(visible);
		if (visible) {
			Logger.getLogger("ch").addHandler(this);
			Logger.getLogger("ch").setLevel(Level.ALL);
		} else {
			Logger.getLogger("ch").removeHandler(this);
		}
	}

}
