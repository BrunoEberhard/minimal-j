package ch.openech.mj.swing.component;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;

import javax.swing.JScrollPane;

public class SizedScrollPane extends JScrollPane {

	private final int widthFactor, heightFactor;
	private Dimension prefSize;
	
	public SizedScrollPane(Component view, int widthFactor, int heightFactor) {
		super(view);
		this.widthFactor = widthFactor;
		this.heightFactor = heightFactor;
		calcPrefSize();
	}

	private void calcPrefSize() {
		Component view = getViewport().getView();
		if (view == null) view = this;
		
		FontMetrics fm = view.getFontMetrics(view.getFont());
		prefSize = new Dimension(fm.stringWidth("Bruno") * widthFactor, fm.getHeight() * heightFactor);
	}
	
	@Override
	public Dimension getPreferredSize() {
		return prefSize;
	}

	@Override
	public void setFont(Font font) {
		super.setFont(font);
		calcPrefSize();
	}
	
}
