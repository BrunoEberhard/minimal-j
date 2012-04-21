package ch.openech.mj.vaadin.toolkit;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

import ch.openech.mj.vaadin.CloseablePipedInputStream;

import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Upload;
import com.vaadin.ui.Window;

public class VaadinImportDialog extends Window implements Upload.Receiver {

	private Upload upload;
	private CloseablePipedInputStream inputStream;
	private Thread thread;
	
	public VaadinImportDialog(Window parentWindow, String title) {
		super(title);
		inputStream = new CloseablePipedInputStream();
		
		HorizontalLayout horizontalLayout = new HorizontalLayout();
		upload = new Upload(null, this);

		horizontalLayout.addComponent(upload);
		horizontalLayout.setExpandRatio(upload, 1.0F);

		setContent(horizontalLayout);
		setModal(true);
		parentWindow.addWindow(this);
		
		addListener(new CloseListener() {
			@Override
			public void windowClose(CloseEvent e) {
				inputStream.setClosed(true);
			}
		});
	}

	@Override
    public OutputStream receiveUpload(String filename, String MIMEType) {
		PipedOutputStream outputStream;
		try {
			outputStream = new PipedOutputStream(inputStream);
			return outputStream;
		} catch (IOException e) {
			e.printStackTrace();
			try {
				inputStream.connect(null);
				inputStream.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			return null;
		}
    }
    
	public PipedInputStream getInputStream() {
		return inputStream;
	}
	
}
