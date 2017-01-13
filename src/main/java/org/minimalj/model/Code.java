package org.minimalj.model;

/**
 * Codes are small base entities of an application. Like currencies
 * or cities. They are mostly stable but there could be updates from
 * time to time. Typically there is an initial load after the
 * repository (database) is created.<p>
 * 
 * For things like 'YES'/'NO' where there is no chance of change
 * or updates at all you better use enums.<p>
 * 
 * A code is always a view. Meaning if a code is referenced in an
 * entity the code is not updated when persisting that entity.<p>
 * 
 * As speciality codes can have a String or an Integer id field. 
 * 
 */
public interface Code {
	
}
