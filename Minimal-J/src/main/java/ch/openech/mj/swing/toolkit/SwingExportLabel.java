package ch.openech.mj.swing.toolkit;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import javax.swing.JFileChooser;
import javax.swing.JLabel;

import ch.openech.mj.toolkit.ExportHandler;
import ch.openech.mj.toolkit.IComponentDelegate;

public class SwingExportLabel implements IComponentDelegate {

	private final ExportHandler exportHandler;
	private final JLabel label;
	
	public SwingExportLabel(ExportHandler exportHandler, String text) {
		this.exportHandler = exportHandler;
		label = new JLabel("Export");
		label.setForeground(Color.BLUE);
		label.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		label.addMouseListener(new SwingExportMouseListener());
	}

	private class SwingExportMouseListener extends MouseAdapter {
		@Override
		public void mouseClicked(MouseEvent e) {
			JFileChooser chooser = new JFileChooser();
			chooser.setMultiSelectionEnabled(false);
			chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			if (JFileChooser.APPROVE_OPTION == chooser.showDialog(null, "Export")) {
				File outputFile = chooser.getSelectedFile();
				try {
					exportHandler.export(new FileOutputStream(outputFile));
				} catch (FileNotFoundException ex) {
					ex.printStackTrace();
				}
			}
		}
	}
	
	@Override
	public Object getComponent() {
		return label;
	}

}
