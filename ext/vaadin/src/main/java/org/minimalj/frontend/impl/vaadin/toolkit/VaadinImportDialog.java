package org.minimalj.frontend.impl.vaadin.toolkit;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.upload.Receiver;
import com.vaadin.flow.component.upload.Upload;

public class VaadinImportDialog extends Dialog implements Receiver {
	private static final long serialVersionUID = 1L;

	private Upload upload;
	private CloseablePipedInputStream inputStream;
	
	public VaadinImportDialog(String title) {
		// super(title);
		inputStream = new CloseablePipedInputStream();
		
		HorizontalLayout horizontalLayout = new HorizontalLayout();
		horizontalLayout.setSpacing(false);
		upload = new Upload(this);

		horizontalLayout.add(upload);
		horizontalLayout.setSizeFull();

		add(horizontalLayout);
		setModal(true);
		open();
		
		this.addDialogCloseActionListener(event -> {
			try {
				inputStream.close();
			} catch (IOException e1) {
				e1.printStackTrace();
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
	
	private class CloseablePipedInputStream extends PipedInputStream {

		private boolean closed = false;

		@Override
		public synchronized int available() throws IOException {
			if (!closed) {
				return super.available();
			} else {
				throw new IllegalStateException();
			}
		}

		@Override
		public void close() throws IOException {
			if (!closed) {
				closed = true;
				VaadinImportDialog.this.close();
			}
			super.close();
		}
		
	}
	
}
