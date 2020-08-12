package org.minimalj.frontend.impl.vaadin.toolkit;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;

import org.minimalj.frontend.Frontend.Input;
import org.minimalj.frontend.Frontend.InputComponentListener;

import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.upload.FinishedEvent;
import com.vaadin.flow.component.upload.Receiver;
import com.vaadin.flow.component.upload.StartedEvent;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.server.InputStreamFactory;
import com.vaadin.flow.server.StreamResource;

public class VaadinImage extends HorizontalLayout implements Input<byte[]> {
	private static final long serialVersionUID = 1L;

	private final InputComponentListener changeListener;
	
	private Image image;
	private Upload upload;
	private Button removeButton;
	
	private File uploadFile;
	private byte[] imageData;
	
	public VaadinImage(InputComponentListener changeListener) {
		this.changeListener = changeListener;
		
		image = new Image();
		// image.setHeight(size * 3, Unit.EM);
		
		upload = new Upload();
		add(upload);
		
		Icon icon = new Icon(VaadinIcon.FILE_REMOVE);
		removeButton = new Button(icon);
		add(removeButton);
		
		upload.setReceiver(new Receiver() {
			private static final long serialVersionUID = 1L;
			
			@Override
			public OutputStream receiveUpload(String fileName, String mimeType) {
				try {
					return new FileOutputStream(uploadFile);
				} catch (FileNotFoundException e) {
					throw new RuntimeException(e);
				}
			}
		});
		
		upload.addFinishedListener(new ComponentEventListener<FinishedEvent>() {
			private static final long serialVersionUID = 1L;

			@Override
			public void onComponentEvent(FinishedEvent event) {
				try {
					setValue(Files.readAllBytes(uploadFile.toPath()));
					changeListener.changed(VaadinImage.this);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		});
		
		upload.addStartedListener(new ComponentEventListener<StartedEvent>() {
			private static final long serialVersionUID = 1L;

			@Override
			public void onComponentEvent(StartedEvent event) {
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
		boolean visible = this.imageData != null;
		this.imageData = imageData;
		if (imageData != null) {
			image.setVisible(true);
		    StreamResource streamResource = new StreamResource("isr", new InputStreamFactory() {
				private static final long serialVersionUID = 1L;

				@Override
				public InputStream createInputStream() {
					return new ByteArrayInputStream(imageData);
				}
			});
		    image.setSrc(streamResource);
		    if (!visible) {
		    	// add(image);
		    	addComponentAsFirst(image);
		    }
		} else {
			if (visible) {
				remove(image);
			}
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
