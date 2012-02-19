package ch.openech.mj.swing.component;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.List;

import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JPopupMenu;
import javax.swing.ListModel;

import ch.openech.mj.edit.validation.Indicator;
import ch.openech.mj.edit.validation.ValidationMessage;

public class IndicatingList extends JList implements Indicator {
	private String validationMessage;
	
	public IndicatingList() {
		this(new DefaultListModel());
		installCellRenderer();
	}
	
	public IndicatingList(ListModel listModel) {
		super(listModel);
		installCellRenderer();
	}
	
	/**
	 * 
	 * The selected Cell should not be highlighted whe the List hasnt the focus
	 */
	private void installCellRenderer() {
		DefaultListCellRenderer cellRenderer = new DefaultListCellRenderer() {
			@Override
			public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
					boolean cellHasFocus) {
				return super.getListCellRendererComponent(list, value, index,
						isSelected && (IndicatingList.this.hasFocus() || IndicatingList.this.isPopupVisible()), cellHasFocus);
			}

		};
		setCellRenderer(cellRenderer);
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
	}
	
	private boolean isPopupVisible() {
		JPopupMenu popupMenu = getComponentPopupMenu();
		return popupMenu != null && popupMenu.isVisible();
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
		if ((hasFocus() || isPopupVisible()) && getSelectionModel().isSelectionEmpty()) {
			DotCornerPainter.paint(this, g);
		}
	}

}
