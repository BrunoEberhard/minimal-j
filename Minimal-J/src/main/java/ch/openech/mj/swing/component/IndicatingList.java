package ch.openech.mj.swing.component;

import java.awt.Graphics;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.ListModel;

import ch.openech.mj.edit.validation.Indicator;
import ch.openech.mj.edit.validation.ValidationMessage;

public class IndicatingList extends JList implements Indicator {
	private String validationMessage;
	private int savedSelectionIndex;
	
	public IndicatingList() {
		this(new DefaultListModel());
		installCellRenderer();
	}
	
	public IndicatingList(ListModel listModel) {
		super(listModel);
		addFocusListener(new FocusListener() {
			@Override
			public void focusGained(FocusEvent arg0) {
				repaint();
			}
			
			@Override
			public void focusLost(FocusEvent arg0) {
				repaint();
			}
		});	
		installCellRenderer();
	}
	
	private void installCellRenderer() {
		addFocusListener(new FocusListener() {
			
			@Override
			public void focusLost(FocusEvent e) {
				savedSelectionIndex = getSelectedIndex();
				clearSelection();
			}
			
			@Override
			public void focusGained(FocusEvent e) {
				setSelectedIndex(Math.max(0, savedSelectionIndex));
			}
		});
	}

	@Override
	public void setValidationMessages(List<ValidationMessage> validationMessages) {
		validationMessage = ValidationMessage.formatHtml(validationMessages);
		repaint();
	}

	@Override
	public void paint(Graphics g) {
		super.paint(g);
		if (validationMessage == null || !isEnabled()) {
			setToolTipText(null);
		} else {
			int x = getInsets().left;
			int y = 15;  // TODO height of font
			RedLinePainter.underline(this, "Eberhard", x, y, g);
			setToolTipText(validationMessage);
		}
		if (hasFocus() && getSelectionModel().isSelectionEmpty()) {
			DotCornerPainter.paint(this, g);
		}
	}

}
