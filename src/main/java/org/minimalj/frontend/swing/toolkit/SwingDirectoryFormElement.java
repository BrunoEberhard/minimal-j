package org.minimalj.frontend.swing.toolkit;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.minimalj.frontend.form.element.FormElement;
import org.minimalj.frontend.toolkit.ClientToolkit.IComponent;
import org.minimalj.model.properties.PropertyInterface;

public class SwingDirectoryFormElement extends JPanel implements FormElement<String>, IComponent {
	private static final long serialVersionUID = 1L;
	
	private final PropertyInterface property;
	private final JTextField textField;
	private final JButton button;
	
	private FormElementListener listener;
	
	public SwingDirectoryFormElement(PropertyInterface property) {
		super(new BorderLayout());
		this.property = property;
		
		textField = new JTextField();
		button = new JButton("...");
		
		add(textField, BorderLayout.CENTER);
		add(button, BorderLayout.LINE_END);

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
		if (listener != null) {
			listener.changed(this);
		}
	}

	@Override
	public PropertyInterface getProperty() {
		return property;
	}

	@Override
	public void setChangeListener(FormElementListener listener) {
		this.listener = listener;
	}

	@Override
	public IComponent getComponent() {
		return this;
	}

}
