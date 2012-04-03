package ch.openech.mj.swing.toolkit;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;
import ch.openech.mj.edit.validation.ValidationMessage;
import ch.openech.mj.toolkit.FlowField;
import ch.openech.mj.util.StringUtils;


public class SwingFlowField extends JPanel implements FlowField {
	private final boolean vertical;
	private JLabel lastLabel;
	
	public SwingFlowField(boolean vertical) {
		super(new MigLayout(vertical ? "ins 4 4 0 0, gap 0 0, wrap 1" : "ins 0, gap 0 0"));
		this.vertical = vertical;
	}
	
	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
	}

	@Override
	public void setValidationMessages(List<ValidationMessage> validationMessages) {
		SwingIndication.setValidationMessagesToCaption(validationMessages, this);
	}
	
	@Override
	public void addAction(Action action) {
		lastLabel = createLinkButton(action);
		add(lastLabel);
	}
	
	private static JLabel createLinkButton(final Action action) {
		final JLabel label = new JLabel();
		label.setIcon((Icon) action.getValue(Action.SMALL_ICON));
		label.setText((String) action.getValue(Action.NAME));
		label.setToolTipText((String) action.getValue(Action.SHORT_DESCRIPTION));
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
			lastLabel = new JLabel(object.toString());
			add(lastLabel);
		}
		revalidate();
	}

	@Override
	public void addHtml(String html) {
		if (!StringUtils.isBlank(html)) {
			lastLabel = new JLabel(html);
			add(lastLabel);
		}
		revalidate();
	}

	@Override
	public void addGap() {
		if (lastLabel != null) {
			if (vertical) {
				lastLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));
			} else {
				lastLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 8));
			}
		}
	}
}
