package org.minimalj.frontend.impl.vaadin.toolkit;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

import javax.xml.transform.stream.StreamSource;

import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;

public class VaadinExportDialog extends Dialog {
	private static final long serialVersionUID = 1L;

	private Anchor link;
	private PipedOutputStream pipedOutputStream = new PipedOutputStream();
	
	public VaadinExportDialog(String title) {		
		try {
			HorizontalLayout horizontalLayout = new HorizontalLayout();
			horizontalLayout.setMargin(false);
			final PipedInputStream pipedInputStream = new PipedInputStream(pipedOutputStream);
            
    		StreamSource ss = new StreamSource(pipedInputStream);
//            StreamResource sr = new StreamResource(ss, "export.xml");
//			sr.setContentType("application/octet-stream");
//			sr.setCacheTime(0);
//			link = new Anchor(sr, "Link to Download");
//			
//			horizontalLayout.add(link);
			
			add(horizontalLayout);
			
			setModal(true);
			open();
		} catch (IOException x) {
        	x.printStackTrace();
        }
	}
	
	public OutputStream getOutputStream() {
		return pipedOutputStream;
	}
	
}
