package ch.openech.mj.toolkit;

import java.io.OutputStream;

public interface ExportHandler extends IComponent {

	public void export(OutputStream stream);
	
}
