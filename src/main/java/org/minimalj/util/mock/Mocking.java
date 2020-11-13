package org.minimalj.util.mock;

/**
 * There are two kinds of classes implementing the mocking interface:
 * <UL>
 * <LI>Business classes</LI>
 * <LI>FormFields</LI>
 * <LI>Editors</LI>
 * </UL>
 * 
 * The classes declare with this interface that they are able to fill itself
 * with random but valid data. It's kind of a 'Lorem Ipsum' for things other 
 * than strings.<p>
 * 
 * This can be very helpful in JUnit tests. But also while manually testing
 * the GUIs. If DevMode is active there is a 'Demo' button in every dialog.
 * Instead of filling a complex form by hand a single click fills a fields
 * with mocked data.<p>
 * 
 * The other classes in this package provide help to create good looking
 * data.
 *
 */
public interface Mocking {

	/**
	 * The object should have random data after this call. And a
	 * possible validation should report no errors.
	 */
	public void mock();

}
