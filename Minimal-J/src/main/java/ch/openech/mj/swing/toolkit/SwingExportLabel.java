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
import ch.openech.mj.toolkit.IComponent;

@Deprecated // to be moved
public class SwingExportLabel extends JLabel implements IComponent {

	private final ExportHandler exportHandler;
	
	public SwingExportLabel(ExportHandler exportHandler, String text) {
		super("Export");
		this.exportHandler = exportHandler;
		setForeground(Color.BLUE);
		setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		addMouseListener(new SwingExportMouseListener());
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

}
