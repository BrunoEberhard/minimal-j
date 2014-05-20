package org.minimalj.frontend.swing;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.minimalj.backend.db.DbVersion;
import org.minimalj.model.validation.ValidationMessage;
import org.minimalj.util.StringUtils;

/**
 * Was the the selection dialog of a derby database file. Not used at the moment
 * 
 */
public class DerbyOptionsDialogController {

	public static void showOptions() {
		final DerbyOptionsDialog dialog = new DerbyOptionsDialog(null, true);
		dialog.setLocationRelativeTo(null);
		// note: these are just normal preferences, not as they are used in the application
		final Preferences preferences = Preferences.userNodeForPackage(DerbyOptionsDialogController.class).node(DerbyOptionsDialogController.class.getSimpleName());
		String directory = preferences.get("dbDirectory", System.getProperty("user.home") + File.separator + "OpenEchDB");
		dialog.getDirectoryTextField().setText(directory);
		if (preferences.getBoolean("dbMemory", true)) {
			dialog.getChoiceButtonGroup().setSelected(dialog.getButtonMemory().getModel(), true);
		} else {
			dialog.getChoiceButtonGroup().setSelected(dialog.getButtonDisc().getModel(), true);
		}

		dialog.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});

		dialog.getDirectoryTextField().addPropertyChangeListener("enabled", new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				dialog.getButtonDirectory().setEnabled(dialog.getDirectoryTextField().isEnabled());
			}
		});

		dialog.getDirectoryTextField().setEnabled(!dialog.getButtonMemory().isSelected());
		dialog.getButtonMemory().addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				dialog.getDirectoryTextField().setEnabled(!dialog.getButtonMemory().isSelected());
			}
		});

		dialog.getButtonDisc().addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				dialog.getDirectoryTextField().setEnabled(dialog.getButtonDisc().isSelected());
			}
		});

		dialog.getDirectoryTextField().getDocument().addDocumentListener(new DirectoryValidation(dialog));

		dialog.getButtonDirectory().addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser chooser = new JFileChooser(dialog.getDirectoryTextField().getText());
				chooser.setMultiSelectionEnabled(false);
				chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				if (JFileChooser.APPROVE_OPTION == chooser.showDialog(dialog, "Verzeichnis wählen")) {
					dialog.getDirectoryTextField().setText(chooser.getSelectedFile().getPath());
				}
			}
		});

		ActionListener startActionListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (!dialog.getButtonStart().isEnabled()) return;
				
				boolean memory = dialog.getChoiceButtonGroup().isSelected(dialog.getButtonMemory().getModel());
				if (memory) {
					preferences.putBoolean("dbMemory", true);
					// nothing to do, everything is preset
					dialog.setVisible(false);
				} else {
					if (checkDirectory(dialog, dialog.getDirectoryTextField().getText())) {
						preferences.putBoolean("dbMemory", false);
						preferences.put("dbDirectory", dialog.getDirectoryTextField().getText());
						dialog.setVisible(false);
					} else {
						dialog.getDirectoryTextField().requestFocus();
					}
				}
			}
		};

		dialog.getButtonStart().addActionListener(startActionListener);
		dialog.getDirectoryTextField().addActionListener(startActionListener);

		dialog.getButtonStart().requestFocus();
		dialog.setVisible(true);
	}
	
	private static boolean checkDirectory(Dialog dialog, String directory) {
		int version = 0;
		SQLException exception = null;
		try {
			Connection connection = DriverManager.getConnection("jdbc:derby:" + directory + ";create=true", "APP", "APP");
			version = DbVersion.getVersionOf(connection);
			connection.close();
		} catch (SQLException x) {
			exception = x;
		}
		
		if (exception != null) {
			if (exception.getNextException() != null && exception.getNextException().getMessage() != null
					&& exception.getNextException().getMessage().startsWith("Another instance of Derby")) {
				JOptionPane.showMessageDialog(dialog, "Dieses Verzeichnis wird bereits verwendet.\nParalleles Arbeiten ist in dieser Version nicht möglich.\n",
						"Hinweis", JOptionPane.ERROR_MESSAGE);
			} else {
				JOptionPane.showMessageDialog(dialog, exception.getLocalizedMessage(), "Hinweis", JOptionPane.ERROR_MESSAGE);
			}
			return false;
		} else {
			if (version < DbVersion.getMinimalVersionAsInt()) {
				JOptionPane.showMessageDialog(dialog, "Dieses Verzeichnis wurde mit einer älteren\nVersion erstellt und kann nicht verwendet werden.\n",
						"Hinweis", JOptionPane.ERROR_MESSAGE);
				return false;
			}
		}
		
		return true;
	}

	private static class DirectoryValidation implements DocumentListener {
		private DerbyOptionsDialog dialog;

		public DirectoryValidation(DerbyOptionsDialog dialog) {
			this.dialog = dialog;
		}

		public void validate() {
			List<ValidationMessage> resultList = new ArrayList<ValidationMessage>();
			String directory = dialog.getDirectoryTextField().getText();
			if (StringUtils.isBlank(directory)) {
				resultList.add(new ValidationMessage("directory", "Bitte Verzeichnis angeben"));
			} else {
				File file = new File(directory);
				if (file.getParentFile() == null || !file.getParentFile().exists()) {
					resultList.add(new ValidationMessage("directory", "Verzeichnis existiert nicht"));
				} else if (!file.getParentFile().canWrite()) {
					resultList.add(new ValidationMessage("directory", "Sie haben keine Berechtigung in dieses Verzeichnis zu schreiben"));
				}
			}
			
			dialog.getButtonStart().setEnabled(resultList.isEmpty());
			// TODO
			// dialog.getDirectoryTextField().setValidationMessages(resultList);
		}

		@Override
		public void changedUpdate(DocumentEvent e) {
			validate();
		}

		@Override
		public void insertUpdate(DocumentEvent e) {
			validate();
		}

		@Override
		public void removeUpdate(DocumentEvent e) {
			validate();
		}
	}

}
