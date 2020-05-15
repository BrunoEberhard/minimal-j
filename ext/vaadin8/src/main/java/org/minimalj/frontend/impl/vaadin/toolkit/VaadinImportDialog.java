package org.minimalj.frontend.impl.vaadin.toolkit;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.UI;
import com.vaadin.ui.Upload;
import com.vaadin.ui.Window;

public class VaadinImportDialog extends Window implements Upload.Receiver {
	private static final long serialVersionUID = 1L;

	private Upload upload;
	private CloseablePipedInputStream inputStream;
	
	public VaadinImportDialog(String title) {
		super(title);
		inputStream = new CloseablePipedInputStream();
		
		HorizontalLayout horizontalLayout = new HorizontalLayout();
		horizontalLayout.setSpacing(false);
		upload = new Upload(null, this);

		horizontalLayout.addComponent(upload);
		horizontalLayout.setExpandRatio(upload, 1.0F);

		setContent(horizontalLayout);
		setModal(true);
		UI.getCurrent().addWindow(this);
		
		addCloseListener(new CloseListener() {
			private static final long serialVersionUID = 1L;

			@Override
			public void windowClose(CloseEvent e) {
				try {
					inputStream.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
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
