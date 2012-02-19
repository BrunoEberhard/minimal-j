package ch.openech.mj.swing.toolkit;

import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JScrollPane;

import ch.openech.mj.edit.validation.ValidationMessage;
import ch.openech.mj.swing.component.IndicatingList;
import ch.openech.mj.swing.component.UnselectableSelectionListModel;
import ch.openech.mj.toolkit.MultiLineTextField;


public class SwingMultiLineTextField extends JScrollPane implements MultiLineTextField {

	private final IndicatingList list;
	
	public SwingMultiLineTextField() {
		list = new IndicatingList(new DefaultListModel());
		list.setSelectionModel(new UnselectableSelectionListModel());
		setViewportView(list);
		
		setInheritsPopupMenu(true);
		list.setInheritsPopupMenu(true);
	}

	@Override
	public void setText(String text) {
		DefaultListModel model = (DefaultListModel) list.getModel();
		model.removeAllElements();
		model.addElement(text);
	}

	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		setInheritsPopupMenu(enabled);
		setViewportView(enabled ? list : null);
	}
	
	@Override
	public void setValidationMessages(List<ValidationMessage> validationMessages) {
		list.setValidationMessages(validationMessages);
	}
	
}
