package ch.openech.mj.swing.toolkit;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Rectangle;

import javax.swing.JPanel;
import javax.swing.JViewport;
import javax.swing.Scrollable;

public class ScrollablePanel extends JPanel implements Scrollable {

	public ScrollablePanel(Component content) {
		super(new BorderLayout());
		add(content, BorderLayout.CENTER);
	}
	
    @Override
	public Dimension getPreferredScrollableViewportSize() {
    	return getPreferredSize();
    }

    @Override
    public int getScrollableUnitIncrement(final Rectangle visibleRect, final int orientation, final int direction) {
        return 30;
    }

    @Override
    public int getScrollableBlockIncrement(final Rectangle visibleRect, final int orientation, final int direction) {
        return visibleRect.width;
    }

    @Override
    public boolean getScrollableTracksViewportWidth() {
		return true;
    }
    
    @Override
    public boolean getScrollableTracksViewportHeight() {
		if (getParent() instanceof JViewport) {
			return (((JViewport) getParent()).getHeight() > getPreferredSize().height);
		}
		return false;
    }

}