package ch.openech.mj.swing.component;

import java.awt.Component;
import java.awt.Graphics;
import java.util.List;
import java.util.Vector;

import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JList;

import ch.openech.mj.edit.validation.Indicator;
import ch.openech.mj.edit.validation.ValidationMessage;
import ch.openech.mj.util.StringUtils;

public class IndicatingComboBox extends JComboBox implements Indicator {
	private String validationMessage;

	private static JList dummyList = new JList();

	public IndicatingComboBox() {
		super();
	}
	
	public IndicatingComboBox(ComboBoxModel comboBoxModel) {
		super(comboBoxModel);
	}
	
	public IndicatingComboBox(Vector<?> items) {
		super(items);
	}

	@Override
	public void setValidationMessages(List<ValidationMessage> validationMessages) {
		validationMessage = ValidationMessage.formatHtml(validationMessages);
		repaint();
	}

	@Override
	public void paint(Graphics g) {
		super.paint(g);
		if (StringUtils.isBlank(validationMessage) || !isEnabled()) {
			setToolTipText(null);
		} else {
			int left = getInsets().left;
			int top = getInsets().top + getBaseline(getWidth(), getHeight()) + 2;
			Component component = getRenderer().getListCellRendererComponent(dummyList, getSelectedItem(), 0, false, false);
			if (component instanceof JComponent) {
				left += ((JComponent) component).getInsets().left;
			}
			RedLinePainter.underline(this, "Eberhard", left, top, g);
			setToolTipText(validationMessage);
		}
	}

}
