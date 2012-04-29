package ch.openech.mj.vaadin.toolkit;

import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

import ch.openech.mj.toolkit.ExportHandler;
import ch.openech.mj.toolkit.IComponentDelegate;

import com.vaadin.terminal.PaintException;
import com.vaadin.terminal.PaintTarget;
import com.vaadin.terminal.StreamResource;
import com.vaadin.terminal.StreamResource.StreamSource;
import com.vaadin.ui.Link;

public class VaadinExportLabel implements IComponentDelegate {
	
	private final ExportHandler exportHandler;
	private final Link link;
	
	public VaadinExportLabel(ExportHandler exportHandler, String label) {
		this.exportHandler = exportHandler;
		
		link = new Link("Download", null) {
			@Override
			public void paint(PaintTarget target) throws PaintException {
				if (getResource() == null && getParent() != null) {
					StreamSource ss = new VaadinExportStreamsource();
			        StreamResource sr = new StreamResource(ss, "export", getParent().getApplication());
					sr.setMIMEType("application/octet-stream");
					sr.setCacheTime(0);
					setResource(sr);
				}
				super.paint(target);
			}
		};
	}

	private class VaadinExportStreamsource implements StreamSource {
        @Override
        public InputStream getStream() {
        	try {
            	PipedOutputStream pipedOutputStream = new PipedOutputStream();
            	PipedInputStream pipedInputStream = new PipedInputStream(pipedOutputStream);
            	exportHandler.export(pipedOutputStream);
                return pipedInputStream;
        	} catch (IOException x) {
        		throw new RuntimeException(x);
        	}
        }
	}

	@Override
	public Object getComponent() {
		return link;
	}

}
