package ch.openech.mj.vaadin.toolkit;

import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

import ch.openech.mj.toolkit.ExportHandler;

import com.vaadin.terminal.StreamResource;
import com.vaadin.terminal.StreamResource.StreamSource;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Link;
import com.vaadin.ui.Window;

public class VaadinExportDialog extends Window {
	private static final long serialVersionUID = 1L;

	private Link link;
	private PipedOutputStream pipedOutputStream = new PipedOutputStream();
	
	public VaadinExportDialog(Window parentWindow, String title, final ExportHandler exportHandler) {
		super(title);
		
		try {
			HorizontalLayout horizontalLayout = new HorizontalLayout();
			final PipedInputStream pipedInputStream = new PipedInputStream(pipedOutputStream);
            
    		StreamSource ss = new StreamSource() {
                private static final long serialVersionUID = 1L;

				@Override
                public InputStream getStream() {
                	VaadinExportDialog.this.close();
                	exportHandler.export(pipedOutputStream);
                    return pipedInputStream;
                }
            };
            StreamResource sr = new StreamResource(ss, "export.xml", parentWindow.getApplication());
			sr.setMIMEType("text/xml");
			sr.setCacheTime(0);
			link = new Link("Link to Download", sr);
			
			horizontalLayout.addComponent(link);
			
			setContent(horizontalLayout);
			
			setModal(true);
			parentWindow.addWindow(this);
		} catch (IOException x) {
        	x.printStackTrace();
        }
	}

}
