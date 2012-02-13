package ch.openech.mj.swing.toolkit;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JTextField;

import ch.openech.mj.db.model.Constants;
import ch.openech.mj.edit.fields.AbstractEditField;

public class SwingDirectoryField extends AbstractEditField<String> {

	private final JTextField textField;
	private final JButton button;
	private final JPanel panel;
	
	public SwingDirectoryField(Object key) {
		super(Constants.getConstant(key));
		
		panel = new JPanel(new BorderLayout());

		textField = new JTextField();
		button = new JButton("...");
		
		panel.add(textField, BorderLayout.CENTER);
		panel.add(button, BorderLayout.LINE_END);

		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser chooser = new JFileChooser(getObject());
				chooser.setMultiSelectionEnabled(false);
				chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				if (JFileChooser.APPROVE_OPTION == chooser.showDialog(textField, "Verzeichnis wählen")) {
					setObject(chooser.getSelectedFile().getPath());
				}
			}
		});
	}

	@Override
	public String getObject() {
		return textField.getText();
	}

	@Override
	public void setObject(String value) {
		textField.setText(value);
	}

	@Override
	public Object getComponent() {
		return panel;
	}

}
