package org.minimalj.frontend.vaadin.toolkit;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

import org.minimalj.frontend.toolkit.ClientToolkit.IComponent;
import org.minimalj.frontend.toolkit.ImportHandler;

import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Upload;

public class VaadinImportField extends HorizontalLayout implements IComponent, Upload.Receiver {
	private static final long serialVersionUID = 1L;

	private final ImportHandler importHandler;
	
	public VaadinImportField(ImportHandler importHandler, String buttonText) {
		this.importHandler = importHandler;
		
		Upload upload = new Upload(null, this);

		addComponent(upload);
		setExpandRatio(upload, 1.0F);
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
