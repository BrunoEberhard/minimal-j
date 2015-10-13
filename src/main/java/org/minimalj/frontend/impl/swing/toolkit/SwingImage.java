package org.minimalj.frontend.impl.swing.toolkit;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import org.minimalj.frontend.Frontend.Input;
import org.minimalj.frontend.Frontend.InputComponentListener;
import org.minimalj.frontend.Frontend.Size;

public class SwingImage extends JPanel implements Input<byte[]> {
	private static final long serialVersionUID = 1L;

	private final InputComponentListener changeListener;
	
	private final Size size;

	private final JLabel image;
	
	private final JPanel controlPanel;
	private final JLabel selectActionLabel;
	private final JLabel removeActionLabel;
	
	private byte[] imageData;
	private ImageIcon icon;
	
	public SwingImage(Size size, InputComponentListener changeListener) {
		super(new BorderLayout(6, 0));
		this.changeListener = changeListener;
		this.size = size;
		
		image = new JLabel();
		add(image, BorderLayout.LINE_START);

		boolean editable = changeListener != null;
		if (editable) {
			controlPanel = new JPanel();
			controlPanel.setLayout(new GridLayout(2, 1));

			removeActionLabel = new SwingRemoveActionLabel();
			removeActionLabel.setVerticalAlignment(SwingConstants.TOP);
			if (icon != null) {
				controlPanel.add(removeActionLabel);
			}
			
			selectActionLabel = new SwingSelectActionLabel();
			selectActionLabel.setVerticalAlignment(SwingConstants.BOTTOM);
			controlPanel.add(selectActionLabel);

			add(controlPanel, BorderLayout.CENTER);
		} else {
			controlPanel = null;
			selectActionLabel = null;
			removeActionLabel = null;
		}
	}
	
	@Override
	public void setValue(byte[] imageData) {
		this.imageData = imageData;
		if (imageData != null) {
			icon = new ImageIcon(imageData);
			int preferredHeight = getPreferredSize().height;
			if (icon.getIconHeight() > preferredHeight) {
				int newWidth = icon.getIconWidth() * preferredHeight / icon.getIconHeight();
				icon = new ImageIcon(icon.getImage().getScaledInstance(newWidth, preferredHeight,  Image.SCALE_SMOOTH));
			}
		} else {
			icon = null;
		}
		image.setIcon(icon);
		if (controlPanel != null) {
			if (icon != null && removeActionLabel.getParent() == null) {
				controlPanel.add(removeActionLabel, 0);
			} else if (icon == null && removeActionLabel.getParent() != null) {
				controlPanel.remove(removeActionLabel);
			}
		}
		if (getParent() != null) {
			getParent().invalidate();
			getParent().repaint();
		}
	}

	@Override
	public byte[] getValue() {
		return imageData;
	}

	@Override
	public void setEditable(boolean editable) {
		if (editable && !isEditable()) {
			if (changeListener == null) {
				throw new IllegalStateException("SwingImage cannot set to editable without a changeListener");
			}
			add(controlPanel, BorderLayout.CENTER);
		} else if (!editable && isEditable()) {
			remove(controlPanel);
		}
	}
	
	public boolean isEditable() {
		return controlPanel != null && controlPanel.getParent() == this;
	}
	
	@Override
	public Dimension getPreferredSize() {
		Dimension d = new Dimension(super.getPreferredSize());
		if (icon != null) {
			JTextField textField = new JTextField();
			int textFieldHeight = textField.getPreferredSize().height;
			if (size == Size.SMALL) {
				d.height = textFieldHeight;
			} else if (size == Size.MEDIUM) {
				d.height = textFieldHeight * 3;
			} else if (size == Size.LARGE) {
				d.height = textFieldHeight * 10;
			}
		}
		return d;
	}
	
	@Override
	public Dimension getMaximumSize() {
		return super.getPreferredSize();
	}

	private class SwingRemoveActionLabel extends JLabel {
		private static final long serialVersionUID = 1L;

		public SwingRemoveActionLabel() {
			super("[x]");
			setForeground(Color.BLUE);
			setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					setValue(null);
					changeListener.changed(SwingImage.this);
				}
			});
		}
	}
	
	private class SwingSelectActionLabel extends JLabel {
		private static final long serialVersionUID = 1L;

		public SwingSelectActionLabel() {
			super("[+]");
			setForeground(Color.BLUE);
			setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					JFileChooser chooser = new JFileChooser();
					chooser.setMultiSelectionEnabled(false);
					chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
					if (JFileChooser.APPROVE_OPTION == chooser.showDialog(SwingImage.this, "Bild wählen")) {
						File file = chooser.getSelectedFile();
						try (FileInputStream fis = new FileInputStream(file)) {
							byte[] bytes = new byte[fis.available()];
							fis.read(bytes);
							setValue(bytes);
							changeListener.changed(SwingImage.this);
						} catch (IOException e1) {
							e1.printStackTrace();
						}
					}
				}
			});
		}
	}

}
