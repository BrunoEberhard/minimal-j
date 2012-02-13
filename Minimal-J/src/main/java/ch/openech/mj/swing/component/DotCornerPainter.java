package ch.openech.mj.swing.component;

import java.awt.Color;
import java.awt.Graphics;

import javax.swing.JComponent;


public class DotCornerPainter {

	public static void paint(JComponent component, Graphics g) {
		int y = component.getInsets().top;
		int x = component.getInsets().left;
		int yEnd = component.getHeight() - component.getInsets().bottom;
		int xEnd = component.getWidth() - component.getInsets().right;
		int height = yEnd - y;
		int width = xEnd - x;
		paint(component, x, y, width, height, g);
	}

	public static void paint(JComponent component, int x, int y, int width, int height, Graphics g) {
		g.setColor(Color.BLACK);
		g.setXORMode(Color.WHITE);
		for (int i = 0; i < width; i = i + 2) {
			g.drawLine(x + i, y, x + i, y);
		}
		for (int i = 2; i < height; i = i + 2) {
			g.drawLine(x, y + i, x, y + i);
		}
	}
	
}
