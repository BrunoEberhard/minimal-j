package org.minimalj.frontend.impl.swing.component;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
import java.util.List;
import java.util.WeakHashMap;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.plaf.LabelUI;

import org.minimalj.frontend.impl.swing.toolkit.SwingFrontend;
import org.minimalj.util.StringUtils;

public class SwingCaption extends JPanel {
	private static final long serialVersionUID = 1L;
	
	private final JLabel captionLabel;
	
	public SwingCaption(Component component, String caption) {
		super(new BorderLayout());
		setBorder(null);
		setOpaque(false);
		
		captionLabel = new CaptionLabel(caption);
		
		add(captionLabel, BorderLayout.NORTH);
		add(component, BorderLayout.CENTER);
		
		((JComponent) component).putClientProperty(SwingCaption.class, this);
	}
	
	@Override
	public boolean isVisible() {
		return getComponent(1).isVisible();
	}
	
	private boolean isEmpty(Component component) {
		if (!component.isVisible()) {
			return true;
		} else if (component instanceof JPanel) {
			JPanel panel = (JPanel) component;
			for (int i = 0; i<panel.getComponentCount(); i++) {
				if (!isEmpty(panel.getComponent(i))) {
					return false;
				}
			}
			return true;
		} else if (component instanceof JLabel) {
			return StringUtils.isBlank(((JLabel) component).getText());
		} else if (component instanceof JTextField) {
			return StringUtils.isBlank(((JTextField) component).getText());
		} else {
			return false;
		}
	}
	
	public boolean isEmpty() {
		Component component = getComponent(1);
		return isEmpty(component);
	}

	public void setValidationMessages(List<String> validationMessages) {
		if (!validationMessages.isEmpty()) {
			captionLabel.setIcon(SwingFrontend.getIcon("FieldError.icon"));
			StringBuilder s = new StringBuilder();
			s.append("<html>");
			for (int i = 0; i<validationMessages.size(); i++) {
				s.append(validationMessages.get(i));
				if (i < validationMessages.size() - 1) {
					s.append("<br>");
				}
			}
			s.append("</html>");
			captionLabel.setToolTipText(s.toString());
		} else {
			captionLabel.setIcon(null);
			captionLabel.setToolTipText(null);
		}
	}
	
	
	private static class CaptionLabel extends JLabel {

		private static final long serialVersionUID = 1L;
		private static WeakHashMap<Font, Font> boldFonts = new WeakHashMap<>();
		
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
				fontSize = ((int)(fontSize * 4.0F / 5.0F + 1.0F));
				boldFont = font.deriveFont(fontSize).deriveFont(Font.BOLD);
				boldFonts.put(font, boldFont);
			}
			setFont(boldFont);
		}
		
	}

}