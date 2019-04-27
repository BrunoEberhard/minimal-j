package org.minimalj.frontend.impl.swing.toolkit;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.LayoutManager;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import javax.swing.JPanel;
import javax.swing.UIManager;

import org.minimalj.frontend.Frontend.Input;
import org.minimalj.frontend.action.Action;
import org.minimalj.frontend.impl.swing.SwingTab;
import org.minimalj.model.Rendering;


public class SwingList<T> extends JPanel implements Input<List<T>> {
	private static final long serialVersionUID = 1L;
	
	private final Function<T, CharSequence> renderer;
	private final Function<T, List<Action>> itemActions;
	private final Action[] listActions;
	private List<T> value;
	
	public SwingList(Function<T, CharSequence> renderer, Function<T, List<Action>> itemActions, Action... listActions) {
		super(null, true);
		setLayout(new VerticalLayoutManager());
		this.renderer = renderer != null ? renderer : Rendering::toString;
		this.itemActions = itemActions;
		this.listActions = listActions;

		if (listActions != null) {
			setComponentPopupMenu(SwingTab.createMenu(Arrays.asList(listActions)));
		}
	}
	
	@Override
	public void updateUI() {
		super.updateUI();
		setBackground(UIManager.getColor("TextField.background"));
		setOpaque(true);
	}

	@Override
	public void setValue(List<T> value) {
		this.value = value;
		if (value != null && !value.isEmpty()) {
			super.removeAll();
			for (T item : value) {
				CharSequence rendered = renderer.apply(item);
				SwingText text = new SwingText(rendered.toString());

				List<Action> actions = new ArrayList<>();
				if (this.itemActions != null) {
					actions.addAll(this.itemActions.apply(item));
				}
				if (listActions != null) {
					Arrays.stream(listActions).forEach(actions::add);
				}
				text.setComponentPopupMenu(SwingTab.createMenu(actions));

				add(text);
			}
		} else {
			super.removeAll();
		}

		revalidate();
		repaint();
	}

	@Override
	public List<T> getValue() {
		return value;
	}

	@Override
	public void setEditable(boolean editable) {
		// TODO Auto-generated method stub
	}

	private static class VerticalLayoutManager implements LayoutManager {

		private Dimension preferredSize;
		
		public VerticalLayoutManager() {
		}

		@Override
		public Dimension preferredLayoutSize(Container parent) {
			layoutContainer(parent);
			return preferredSize;
		}

		@Override
		public Dimension minimumLayoutSize(Container parent) {
			layoutContainer(parent);
			return preferredSize;
		}

		@Override
		public void layoutContainer(Container parent) {
			int preferredHeight = 0;
			for (Component component : parent.getComponents()) {
				int height = component.getPreferredSize().height;
				preferredHeight += height;
			}
			int verticalRest = parent.getHeight() - preferredHeight;
			int verticalInset = verticalRest > 8 ? 4 : verticalRest / 2;
			int y = verticalInset;
			int x = verticalInset > 0 ? 1 : 0;
			int width = parent.getWidth();
			int widthWithoutIns = width - x;
			for (Component component : parent.getComponents()) {
				int height = component.getPreferredSize().height;
				component.setBounds(x, y, widthWithoutIns, height);
				y += height;
			}
			preferredSize = new Dimension(width, preferredHeight);
		}

		@Override
		public void addLayoutComponent(String name, Component comp) {
		}

		@Override
		public void removeLayoutComponent(Component comp) {
		}
	}
}
