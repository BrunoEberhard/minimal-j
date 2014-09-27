package org.minimalj.frontend.swing.component;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
import java.util.List;
import java.util.WeakHashMap;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.plaf.LabelUI;

import org.minimalj.model.validation.ValidationMessage;
import org.minimalj.util.resources.ResourceHelper;

public class SwingCaption extends JPanel {
	private static final long serialVersionUID = 1L;
	
	private final JLabel captionLabel;
	
	public SwingCaption(Component component, String caption) {
		super(new BorderLayout());
		setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
		
		captionLabel = new CaptionLabel(caption);
		
		add(captionLabel, BorderLayout.NORTH);
		add(component, BorderLayout.CENTER);
		
		((JComponent) component).putClientProperty(SwingCaption.class, this);
	}

	public void setValidationMessages(List<String> validationMessages) {
		if (!validationMessages.isEmpty()) {
			captionLabel.setIcon(ResourceHelper.getIcon("field_error.png"));
			String validationMessage = ValidationMessage.formatHtmlString(validationMessages);
			captionLabel.setToolTipText(validationMessage);
		} else {
			captionLabel.setIcon(null);
			captionLabel.setToolTipText(null);
		}
	}
	
	
	private static class CaptionLabel extends JLabel {

		private static final long serialVersionUID = 1L;
		private static WeakHashMap<Font, Font> boldFonts = new WeakHashMap<Font, Font>();
		
		public CaptionLabel(String caption) {
			super(caption);
			setHorizontalTextPosition(SwingConstants.LEADING);
		}

		@Override
		public void setUI(LabelUI ui) {
			if (getFont() != null) {
				// set the Font to "unchanged", otherwise the font gets smaller and smaller
				// with every setUI
				setFont(null);
			}
			
			super.setUI(ui);
			Font font = getFont();
			Font boldFont = boldFonts.get(font);
			if (boldFont == null) {
				float fontSize = font.getSize();
				fontSize = (float)((int)(fontSize * 4.0F / 5.0F + 1.0F));
				boldFont = font.deriveFont(fontSize).deriveFont(Font.BOLD);
				boldFonts.put(font, boldFont);
			}
			setFont(boldFont);
		}
		
	}

}