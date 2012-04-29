package ch.openech.mj.vaadin.toolkit;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

import ch.openech.mj.toolkit.IComponentDelegate;
import ch.openech.mj.toolkit.ImportHandler;

import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Upload;

public class VaadinImportField implements IComponentDelegate, Upload.Receiver {

	private final ImportHandler importHandler;
	private final HorizontalLayout horizontalLayout;
	
	public VaadinImportField(ImportHandler importHandler, String buttonText) {
		this.importHandler = importHandler;
		
		horizontalLayout = new HorizontalLayout();
		Upload upload = new Upload(null, this);

		horizontalLayout.addComponent(upload);
		horizontalLayout.setExpandRatio(upload, 1.0F);
	}

	@Override
	public Object getComponent() {
		return horizontalLayout;
	}

	@Override
    public OutputStream receiveUpload(String filename, String MIMEType) {
		PipedOutputStream outputStream;
		try {
			PipedInputStream inputStream = new PipedInputStream();
			outputStream = new PipedOutputStream(inputStream);
			importHandler.imprt(inputStream);
			return outputStream;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
    }
    
	
}
