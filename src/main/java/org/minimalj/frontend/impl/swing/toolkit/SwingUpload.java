package org.minimalj.frontend.impl.swing.toolkit;

import java.awt.BorderLayout;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.Frontend.Input;
import org.minimalj.frontend.Frontend.InputComponentListener;
import org.minimalj.frontend.Frontend.NamedFile;
import org.minimalj.util.resources.Resources;

public class SwingUpload extends JPanel implements Input<NamedFile[]> {
    private static final long serialVersionUID = 1L;

    private final InputComponentListener changeListener;

    private NamedFile[] namedFiles;

    private final JFileChooser fileChooser;
    private final JButton selectButton;
    private final JLabel label;

    public SwingUpload(InputComponentListener changeListener, boolean multiple) {
        super(new BorderLayout());
        this.changeListener = Objects.requireNonNull(changeListener);

        fileChooser = new JFileChooser();
        fileChooser.setMultiSelectionEnabled(multiple);
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

        selectButton = new JButton(Resources.getString(multiple ? "Upload.files" : "Upload.file"));
        label = new JLabel();
        label.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
        
        selectButton.addActionListener(this::selectFiles);

        add(selectButton, BorderLayout.LINE_START);
        add(label, BorderLayout.CENTER);
        
        setDropTarget(new DropTarget(this, new DropTargetAdapter() {
            @Override
            public void drop(DropTargetDropEvent dtde) {
                dtde.acceptDrop(DnDConstants.ACTION_COPY);
                Transferable transferable = dtde.getTransferable();
                DataFlavor[] flavors = transferable.getTransferDataFlavors();
                for (DataFlavor flavor : flavors) {
                    if (flavor.isFlavorJavaFileListType()) {
                        try {
                            @SuppressWarnings("unchecked")
                            List<File> files = (List<File>) transferable.getTransferData(flavor);
                            handleDroppedFiles(files);
                        } catch (Exception ex) {
                            // just ignore
                        }
                        return;
                    }
                }
            }
        }));
    }
    
    private void handleDroppedFiles(List<File> files) throws IOException {
        List<NamedFile> namedFileList = new ArrayList<>();
        for (File file : files) {
            NamedFile namedFile = createNamedFile(file);
            namedFileList.add(namedFile);
        }
        namedFiles = namedFileList.toArray(new NamedFile[0]);

        fireChange();
    }
    
    private void selectFiles(ActionEvent e) {
    	int returnVal = fileChooser.showOpenDialog(SwingUpload.this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
    		try {
	        	if (fileChooser.isMultiSelectionEnabled()) {
	                File[] files = fileChooser.getSelectedFiles();
	                List<NamedFile> namedFileList = new ArrayList<>();
	                for (File file : files) {
	                    NamedFile namedFile = createNamedFile(file);
	                    namedFileList.add(namedFile);
	                }
	                namedFiles = namedFileList.toArray(new NamedFile[0]);
	        	} else {
	        		File file = fileChooser.getSelectedFile();
	        		namedFiles = new NamedFile[] {createNamedFile(file)};
	        	}
    		} catch (IOException ex) {
    			Frontend.showError("File couldn't be read (" + ex.getMessage() + ")");
    		}
    		
            fireChange();
        }
    }
    
    private void fireChange() {
        SwingFrontend.run(SwingUpload.this, () -> {
            updateLabel();
            changeListener.changed(SwingUpload.this);
        });
    }

	private NamedFile createNamedFile(File file) throws IOException {
		NamedFile namedFile = new NamedFile();
		namedFile.name = file.getName();
	    namedFile.content = Files.readAllBytes(file.toPath());
		return namedFile;
	}
    
    private void updateLabel() {
    	if (namedFiles.length == 0) {
    		label.setText(null);
    	} else if (namedFiles.length == 1) {
    		label.setText(namedFiles[0].name);
    	} else {
    		label.setText(namedFiles.length + " Files");
    	}
    }

    @Override
    public void setValue(NamedFile[] namedFiles) {
        this.namedFiles = namedFiles;
        if (namedFiles != null) {
            StringBuilder fileNames = new StringBuilder();
            for (NamedFile namedFile : namedFiles) {
                fileNames.append(namedFile.name).append("\n");
            }
        }
        updateLabel();
    }

    @Override
    public NamedFile[] getValue() {
        return namedFiles;
    }
    
    @Override
    public void setEditable(boolean editable) {
    	// ignore
    }
}