package org.minimalj.frontend.swing.lookAndFeel;

import java.awt.Color;
import java.awt.Font;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Logger;

import javax.swing.UIDefaults;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.MatteBorder;
import javax.swing.plaf.BorderUIResource;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.FontUIResource;
import javax.swing.plaf.basic.BasicBorders;
import javax.swing.plaf.metal.DefaultMetalTheme;
import javax.swing.plaf.metal.MetalLookAndFeel;

public class PrintLookAndFeel extends MetalLookAndFeel implements MetalThemeProvider {
	private static final long serialVersionUID = 1L;
	
	private static Logger logger = Logger.getLogger(PrintLookAndFeel.class.getName());
	static Properties properties = new Properties();

	static {
		try {
			properties.load(PrintLookAndFeel.class.getResourceAsStream("PrintLookAndFeel.properties"));
		} catch (IOException ioe) {
			logger.warning("Could not load PrintLookAndFeel.properties.");
		}
	}

	@Override
	public String getName() {
		return properties.getProperty("name");
	}

	@Override
	public String getDescription() {
		return properties.getProperty("description");
	}

	@Override
	public String getID() {
		return getClass().getName();
	}

	@Override
	protected void initComponentDefaults(UIDefaults table) {
		super.initComponentDefaults(table);

		Border lineBorder = new BorderUIResource(new MatteBorder(1, 1, 1, 1, Color.LIGHT_GRAY));
		Object textBorder = new BorderUIResource(new CompoundBorder(lineBorder, new BasicBorders.MarginBorder()));
		
		Object[] defaults = new Object[] { "ComboBox.selectionForeground", getHighlightedTextColor(), "Panel.font", getControlTextFont(), 
		        "ToolTip.border", lineBorder,
		        "TitledBorder.border", lineBorder,
		        "TextField.border", textBorder,
		        "PasswordField.border", textBorder,
		        "TextArea.border", textBorder,
		        "TextPane.border", textBorder,
		        "EditorPane.border", textBorder,
		        "Button.border", lineBorder,
				};
		table.putDefaults(defaults);
	}

	public static class MetalTheme extends DefaultMetalTheme {
		private FontUIResource font;

		private static ColorUIResource getColor(String key) {
			return new ColorUIResource(Integer.parseInt(properties.getProperty(key), 16));
		}

		public MetalTheme() {
			String fontName = properties.getProperty("fontName", "Dialog");
			int fontSize = 12;
			try {
				fontSize = Integer.parseInt(properties.getProperty("fontSize"));
			} catch (Exception exc) {
			}
			font = new FontUIResource(fontName, Font.PLAIN, fontSize);
		}

		@Override
		public ColorUIResource getMenuBackground() {
			return getWhite();
		}

		@Override
		public ColorUIResource getMenuDisabledForeground() {
			return getWhite();
		}

		@Override
		public ColorUIResource getMenuForeground() {
			return getBlack();
		}

		@Override
		protected ColorUIResource getWhite() {
			return getColor("backgroundColor");
		}

		@Override
		public ColorUIResource getControl() {
			return getColor("backgroundColor");
		}

		@Override
		protected ColorUIResource getBlack() {
			return getColor("foregroundColor");
		}

		@Override
		protected ColorUIResource getPrimary1() {
			return getColor("primaryColor1");
		}

		@Override
		protected ColorUIResource getPrimary2() {
			return getColor("primaryColor2");
		}

		@Override
		protected ColorUIResource getPrimary3() {
			return getColor("primaryColor3");
		}

		@Override
		protected ColorUIResource getSecondary1() {
			return getColor("secondaryColor1");
		}

		@Override
		protected ColorUIResource getSecondary2() {
			return getColor("secondaryColor2");
		}

		@Override
		protected ColorUIResource getSecondary3() {
			return getColor("secondaryColor3");
		}

		protected ColorUIResource getSelectionForeground() {
			return getColor("selectionForeground");
		}

		protected ColorUIResource getSelectionBackground() {
			return getColor("selectionBackground");
		}

		@Override
		public ColorUIResource getMenuSelectedBackground() {
			return getSelectionBackground();
		}

		@Override
		public ColorUIResource getMenuSelectedForeground() {
			return getSelectionForeground();
		}

		@Override
		public ColorUIResource getTextHighlightColor() {
			return getSelectionBackground();
		}

		@Override
		public ColorUIResource getHighlightedTextColor() {
			return getSelectionForeground();
		}

		@Override
		public FontUIResource getControlTextFont() {
			return font;
		}

		@Override
		public FontUIResource getMenuTextFont() {
			return font;
		}

		@Override
		public FontUIResource getSubTextFont() {
			return font;
		}

		@Override
		public FontUIResource getSystemTextFont() {
			return font;
		}

		@Override
		public FontUIResource getUserTextFont() {
			return font;
		}

		@Override
		public FontUIResource getWindowTitleFont() {
			return font;
		}
		
	}

	@Override
	public MetalTheme getMetalTheme() {
		return new MetalTheme();
	}
}