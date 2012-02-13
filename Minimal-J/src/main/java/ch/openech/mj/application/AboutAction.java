package ch.openech.mj.application;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;

import ch.openech.mj.resources.ResourceHelper;

public class AboutAction extends AbstractAction {

	public AboutAction() {
		super("Über");
	}

	@Override
	public void actionPerformed(ActionEvent actionEvent) {
		String m = ResourceHelper.getApplicationTitle() + "\nVersion " + ResourceHelper.getApplicationVersion() + //
			"\n© " + ResourceHelper.getApplicationVendor() + " - " + ResourceHelper.getApplicationHomepage();

		// ClientToolkit.getToolkit().showError(component, text)
		JOptionPane.showMessageDialog(null, m, ResourceHelper.getApplicationTitle(), JOptionPane.INFORMATION_MESSAGE, null);
	}

}
