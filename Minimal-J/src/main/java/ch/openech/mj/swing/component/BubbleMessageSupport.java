package ch.openech.mj.swing.component;

import java.awt.FontMetrics;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.Popup;
import javax.swing.PopupFactory;
import javax.swing.Timer;
import javax.swing.border.CompoundBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;

public class BubbleMessageSupport {

	public static void showBubble(final JComponent component, String message) {
		JLabel label = new JLabel(message);
		label.setBorder(new CompoundBorder(BorderFactory.createLineBorder(label.getForeground()), BorderFactory.createEmptyBorder(2, 4, 2, 4)));
		
		Point position = component.getLocationOnScreen();
		FontMetrics fm = component.getFontMetrics(component.getFont());
		int additionalWidth = component.getWidth() / 2;
		if (component instanceof JTextComponent) {
			JTextComponent textComponent = (JTextComponent) component;
			additionalWidth = (int)fm.getStringBounds(textComponent.getText(), component.getGraphics()).getWidth();
			additionalWidth = Math.min(additionalWidth, component.getWidth() - component.getInsets().right - component.getInsets().left);
		}
		int baseLine = fm.getAscent();
		int posX = position.x + additionalWidth + component.getInsets().left + 5;
		int posY = position.y + baseLine + component.getInsets().top + 2;
		
		Popup popup = PopupFactory.getSharedInstance().getPopup(component, label, posX, posY);
		
		Bubbler bubbler = (Bubbler) component.getClientProperty(Bubbler.class);
		if (bubbler == null) {
			bubbler = new Bubbler(component);
			component.putClientProperty(Bubbler.class, bubbler);
		}
		
		bubbler.showPopup(popup);
	}
	
	private static class Bubbler implements ActionListener {
		private Popup popup;
		private Timer messageTimer;
		
		public Bubbler(JComponent component) {
			messageTimer = new Timer(4000, this);
			messageTimer.setRepeats(false);
			
			if (component instanceof JTextComponent) {
				JTextComponent textComponent = (JTextComponent) component;
				textComponent.getDocument().addDocumentListener(new DocumentListener() {
					@Override
					public void removeUpdate(DocumentEvent arg0) {
						hidePopup();
					}
					
					@Override
					public void insertUpdate(DocumentEvent arg0) {
						hidePopup();				
					}
					
					@Override
					public void changedUpdate(DocumentEvent arg0) {
						hidePopup();					
					}
				});
			}
			component.addFocusListener(new FocusAdapter() {
				@Override
				public void focusLost(FocusEvent e) {
					hidePopup();
				}
			});
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			hidePopup();
		}
		
		public void hidePopup() {
			if (popup != null) {
				popup.hide();
				popup = null;
			}
		}

		public void showPopup(Popup popup) {
			hidePopup();
			
			this.popup = popup;
			if (popup != null) {
				popup.show();
				messageTimer.restart();
			} 		
		}
		
	}
	
}
