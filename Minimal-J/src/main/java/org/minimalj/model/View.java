package org.minimalj.model;


/**
 * A class implementing View is a model class that holds only a part
 * of the fields of an other class. 
 * 
 * The objects of this class are never saved
 * 
 * @param <T> the class on which this view is based. Mandatory.
 */
public interface View<T> {

//	Das sollte wirklich auch für codes in comboboxen verwendet werden.
//	Ev sogar bei enums -> CodeItem extends ViewOf?
//	
//	1. Display in einer eigenen Klasse mit return object, wo entweder String oder ein Multiobjekt zurückgegeben werden kann
//	2. Display als eine Klassenhierachie (Display & TooltipDisplay/Display2/XDisplay & HtmlDisplay)
//	3. Display mit java 8 default mehtoden
//	4. Display mit einem generic type, die Rückgabeart bestimmt
//	5. display methode, die nach einer art der Rückgabe gefragt werden kann
//	
//	public String display();
	
}
