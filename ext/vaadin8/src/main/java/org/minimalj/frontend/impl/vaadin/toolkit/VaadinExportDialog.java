package org.minimalj.frontend.impl.vaadin.toolkit;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

import com.vaadin.server.StreamResource;
import com.vaadin.server.StreamResource.StreamSource;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Link;
import com.vaadin.ui.UI;
import com.vaadin.ui.Window;

public class VaadinExportDialog extends Window {
	private static final long serialVersionUID = 1L;

	private Link link;
	private PipedOutputStream pipedOutputStream = new PipedOutputStream();
	
	public VaadinExportDialog(String title) {
		super(title);
		
		try {
			HorizontalLayout horizontalLayout = new HorizontalLayout();
			horizontalLayout.setMargin(false);
			final PipedInputStream pipedInputStream = new PipedInputStream(pipedOutputStream);
            
    		StreamSource ss = new StreamSource() {
                private static final long serialVersionUID = 1L;
                                                            
				@Override
                public InputStream getStream() {
                	VaadinExportDialog.this.close();
                    return pipedInputStream;
                }
            };
            StreamResource sr = new StreamResource(ss, "export.xml");
			sr.setMIMEType("application/octet-stream");
			sr.setCacheTime(0);
			link = new Link("Link to Download", sr);
			
			horizontalLayout.addComponent(link);
			
			setContent(horizontalLayout);
			
			setModal(true);
			UI.getCurrent().addWindow(this);
		} catch (IOException x) {
        	x.printStackTrace();
        }
	}
	
	public OutputStream getOutputStream() {
		return pipedOutputStream;
	}
	
}
