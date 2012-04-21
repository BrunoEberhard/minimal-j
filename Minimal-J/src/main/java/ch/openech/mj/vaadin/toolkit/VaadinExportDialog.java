package ch.openech.mj.vaadin.toolkit;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

import com.vaadin.terminal.StreamResource;
import com.vaadin.terminal.StreamResource.StreamSource;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Link;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Window;

public class VaadinExportDialog extends Window {
	
	private TextField textField;
	private Link link;
	private PipedOutputStream pipedOutputStream = new PipedOutputStream();
	
	public VaadinExportDialog(Window parentWindow, String title) {
		super(title);
		
		try {
			HorizontalLayout horizontalLayout = new HorizontalLayout();
			textField = new TextField();
			final PipedInputStream pipedInputStream = new PipedInputStream(pipedOutputStream);
            
    		StreamSource ss = new StreamSource() {
                @Override
                public InputStream getStream() {
                    return pipedInputStream;
                }
            };
            StreamResource sr = new StreamResource(ss, "export", parentWindow.getApplication());
			sr.setMIMEType("application/octet-stream");
			sr.setCacheTime(0);
			link = new Link("Download", sr);
			
			horizontalLayout.addComponent(textField);
			horizontalLayout.setExpandRatio(textField, 1.0F);
			
			horizontalLayout.addComponent(link);
			
			setContent(horizontalLayout);
			
			setModal(true);
			parentWindow.addWindow(this);
		} catch (IOException x) {
        	x.printStackTrace();
        }
	}

	protected OutputStream getOutputStream() {
		return pipedOutputStream;
	}

}
