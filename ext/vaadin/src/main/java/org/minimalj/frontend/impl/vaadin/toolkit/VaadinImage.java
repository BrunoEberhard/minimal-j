package org.minimalj.frontend.impl.vaadin.toolkit;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;

import org.minimalj.frontend.Frontend.Input;
import org.minimalj.frontend.Frontend.InputComponentListener;
import org.minimalj.frontend.Frontend.Size;

import com.vaadin.server.FileResource;
import com.vaadin.server.FontAwesome;
import com.vaadin.ui.Button;
import com.vaadin.ui.Image;
import com.vaadin.ui.Upload;
import com.vaadin.ui.Upload.FinishedEvent;
import com.vaadin.ui.Upload.StartedEvent;

public class VaadinImage extends com.vaadin.ui.GridLayout implements Input<byte[]> {
	private static final long serialVersionUID = 1L;

	private final InputComponentListener changeListener;
	
	private final Size size;

	private Image image;
	private Upload upload;
	private Button removeButton;
	
	private File uploadFile;
	private byte[] imageData;
	
	public VaadinImage(Size size, InputComponentListener changeListener) {
		super(3, 1);
		this.changeListener = changeListener;
		this.size = size;
		
		image = new Image();
		image.setHeight(size == Size.LARGE ? 30 : (size == Size.MEDIUM ? 9 : 3), Unit.EM);
		addComponent(image, 0, 0);
		
		upload = new Upload();
		addComponent(upload, 1, 0);
		
		removeButton = new Button(FontAwesome.REMOVE);
		addComponent(removeButton, 2, 0);
		
		upload.setReceiver(new Upload.Receiver() {
			private static final long serialVersionUID = 1L;

			@Override
			public OutputStream receiveUpload(String filename, String mimeType) {
				try {
					return new FileOutputStream(uploadFile);
				} catch (FileNotFoundException e) {
					throw new RuntimeException(e);
				}
			}
		});
		
		upload.addFinishedListener(new Upload.FinishedListener() {
			private static final long serialVersionUID = 1L;

			@Override
			public void uploadFinished(FinishedEvent event) {
				try {
					setValue(Files.readAllBytes(uploadFile.toPath()));
					changeListener.changed(VaadinImage.this);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		});
		
		upload.addStartedListener(new Upload.StartedListener() {
			private static final long serialVersionUID = 1L;

			@Override
			public void uploadStarted(StartedEvent event) {
				try {
					uploadFile = File.createTempFile("ImageUpload", "xyz");
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		});
		
		boolean editable = changeListener != null;
		upload.setVisible(editable);
		removeButton.setVisible(editable);
	}
	
	@Override
	public void setValue(byte[] imageData) {
		this.imageData = imageData;
		if (imageData != null) {
			File file;
			try {
				file = File.createTempFile("imageDisplay", "png");
				try (FileOutputStream fis = new FileOutputStream(file)) {
					fis.write(imageData);
				}
				image.setSource(new FileResource(file));
			} catch (IOException e) {
				e.printStackTrace();
				image.setSource(null);
			}
		} else {
			image.setSource(null);
		}
	}

	@Override
	public byte[] getValue() {
		return imageData;
	}

	@Override
	public void setEditable(boolean editable) {
		if (editable && !isEditable()) {
			if (changeListener == null) {
				throw new IllegalStateException("VaadinImage cannot set to editable without a changeListener");
			}
		}
		upload.setVisible(editable);
		removeButton.setVisible(editable);
	}
	
	public boolean isEditable() {
		return upload.isVisible();
	}

}
