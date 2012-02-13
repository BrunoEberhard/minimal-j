package ch.openech.mj.swing.component;

import java.awt.Graphics;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.List;

import javax.swing.JTextField;
import javax.swing.text.Document;

import ch.openech.mj.edit.validation.Indicator;
import ch.openech.mj.edit.validation.ValidationMessage;
import ch.openech.mj.util.StringUtils;

public class IndicatingTextField extends JTextField implements Indicator {
	private String validationMessage;

	public IndicatingTextField() {
		this(null);
	}

	public IndicatingTextField(Document document) {
		super(document, null, 0);
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
			RedLinePainter.underline(this, g);
			setToolTipText(validationMessage);
		}
		if (hasFocus() && (!isEditable() || !isEnabled())) {
			DotCornerPainter.paint(this, g);
		}
	}

}
