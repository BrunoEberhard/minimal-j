package ch.openech.mj.example;

import java.util.ResourceBundle;

import ch.openech.mj.db.model.Code;
import ch.openech.mj.db.model.Formats;
import ch.openech.mj.db.model.PlainFormat;

public class ExampleFormats {

	public static final ResourceBundle codeResourceBundle = ResourceBundle.getBundle((ExampleFormats.class.getPackage().getName() + ".Code"));

	public static final Code media = new Code(codeResourceBundle, "media");

	public static final String baseName = "baseName";
			
	static {
		Formats.getInstance().register(baseName, new PlainFormat(30));
		Formats.getInstance().register("media", media);
	}
	
	public static void initialize() {
		// nothing to do, everything done in static initializer
	}
	
}
