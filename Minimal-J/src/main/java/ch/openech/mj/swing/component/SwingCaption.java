package ch.openech.mj.swing.component;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.plaf.LabelUI;

import ch.openech.mj.edit.validation.Indicator;
import ch.openech.mj.edit.validation.ValidationMessage;
import ch.openech.mj.resources.ResourceHelper;
import ch.openech.mj.toolkit.IComponent;

public class SwingCaption extends JPanel implements IComponent, Indicator {

	private final JLabel captionLabel;
	
	public SwingCaption(Component component, String caption) {
		super(new BorderLayout());
		setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
		
		captionLabel = new CaptionLabel(caption);
		
		add(captionLabel, BorderLayout.NORTH);
		add(component, BorderLayout.CENTER);
		
		((JComponent) component).putClientProperty(SwingCaption.class, this);
	}

	@Override
	public void setValidationMessages(List<ValidationMessage> validationMessages) {
		if (!validationMessages.isEmpty()) {
			captionLabel.setIcon(ResourceHelper.getIcon("field_error.png"));
			String validationMessage = ValidationMessage.formatHtml(validationMessages);
			captionLabel.setToolTipText(validationMessage);
		} else {
			captionLabel.setIcon(null);
			captionLabel.setToolTipText(null);
		}
	}
	
	
	private static class CaptionLabel extends JLabel {

		public CaptionLabel(String caption) {
			super(caption);
			setHorizontalTextPosition(SwingConstants.LEADING);
		}

		@Override
		public void setUI(LabelUI ui) {
			// set the Font to "unchanged", otherwise the font gets smaller and smaller
			// with every setUI
			setFont(null);
			
			super.setUI(ui);
			Font font = getFont();
			float fontSize = font.getSize();
			fontSize = (float)((int)(fontSize * 4.0F / 5.0F + 1.0F));
			setFont(font.deriveFont(fontSize).deriveFont(Font.BOLD));
		}
		
	}

}