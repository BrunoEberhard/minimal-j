package org.minimalj.frontend.impl.swing.component;

import java.util.ArrayList;
import java.util.List;

import javax.swing.text.JTextComponent;

import org.fife.ui.autocomplete.AutoCompletion;
import org.fife.ui.autocomplete.BasicCompletion;
import org.fife.ui.autocomplete.Completion;
import org.fife.ui.autocomplete.DefaultCompletionProvider;
import org.minimalj.frontend.Frontend.Search;
import org.minimalj.frontend.impl.swing.toolkit.SwingTextField;

public class SwingSuggestion {

	
	public static void apply(SwingTextField textField, Search<String> suggestionSearch) {
		DefaultCompletionProvider acp = new DefaultCompletionProvider() {
			@Override
			public boolean isAutoActivateOkay(JTextComponent tc) {
				return true;
			}				
			
			@Override
			protected List<Completion> getCompletionsImpl(JTextComponent comp) {
				List<Completion> retVal = new ArrayList<>();
				String text = getAlreadyEnteredText(comp);

				if (text != null) {
					List<String> suggestions = suggestionSearch.search(text);
					for (String s : suggestions) {
						retVal.add(new BasicCompletion(this, s));
					}
				}

				return retVal;
			}
		};
		AutoCompletion ac = new AutoCompletion(acp);
		ac.setAutoCompleteEnabled(true);
		ac.setAutoActivationDelay(100);
		ac.setAutoActivationEnabled(true);
		ac.setAutoCompleteSingleChoices(false);
		ac.install(textField);
	}
	
	public static boolean ping() {
		return true;
	}
}
