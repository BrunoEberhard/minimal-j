package ch.openech.mj.swing.toolkit;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;
import ch.openech.mj.edit.validation.ValidationMessage;
import ch.openech.mj.toolkit.MultiLineTextField;
import ch.openech.mj.util.StringUtils;


public class SwingMultiLineTextField extends JPanel implements MultiLineTextField {

	public SwingMultiLineTextField() {
		super(new MigLayout("ins 4 4 0 0, gap 0 0, wrap 1"));
	}
	
	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
	}

	@Override
	public void setValidationMessages(List<ValidationMessage> validationMessages) {
		// super.setValidationMessages(validationMessages);
	}
	
	@Override
	public void addAction(Action action) {
		add(createLinkButton(action));
	}
	
	private static JComponent createLinkButton(final Action action) {
		final JLabel label = new JLabel();
		label.setText((String) action.getValue(Action.NAME));
		label.setForeground(Color.BLUE);
		label.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		label.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				action.actionPerformed(new ActionEvent(label, 0, null));
			}
		});
        return label;
	}

	@Override
	public void clear() {
		removeAll();
		revalidate();
	}

	@Override
	public void addObject(Object object) {
		if (object != null) {
			JLabel label = new JLabel(object.toString());
			add(label);
		}
		revalidate();
	}

	@Override
	public void addHtml(String html) {
		if (!StringUtils.isBlank(html)) {
			JLabel label = new JLabel(html);
			add(label);
		}
		revalidate();
	}

	@Override
	public void addGap() {
		JLabel label = new JLabel(" ");
		add(label);
		revalidate();
	}
}
