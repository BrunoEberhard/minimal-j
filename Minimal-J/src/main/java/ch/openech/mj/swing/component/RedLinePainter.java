package ch.openech.mj.swing.component;

import java.awt.Color;
import java.awt.Graphics;

import javax.swing.JComponent;
import javax.swing.text.JTextComponent;

import ch.openech.mj.util.StringUtils;


public class RedLinePainter {

	public static void underline(JTextComponent component, Graphics g) {
		underline(component, component.getText(), g);
	}
	
	public static void underline(JComponent component, String text, Graphics g) {
		int y = component.getBaseline(component.getWidth(), component.getHeight()) + 2;
		int x = component.getInsets().left;
		underline(component, text, x, y, g);
	}

	public static void underline(JComponent component, String text, int x, int y, Graphics g) {
		text = cutAtLineBreak(text);
		g.setColor(Color.RED);
		int width = StringUtils.pixelWidth(g, text, component) + component.getInsets().left;
		width = Math.max(width, StringUtils.pixelWidth(g, "Eberhard", component));
		while (x < width && x < component.getWidth() - component.getInsets().right) {
			g.drawLine(x, y, x + 1, y);
			x = x + 2;
			g.drawLine(x, y + 1, x + 1, y + 1);
			x = x + 2;
		}
	}
	
	private static String cutAtLineBreak(String text) {
		if (text == null) return null;
		int pos = text.indexOf("\n");
		if (pos > -1) {
			return text.substring(0, pos);
		} else {
			return text;
		}
	}
	
}
