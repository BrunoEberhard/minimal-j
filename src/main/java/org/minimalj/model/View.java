package org.minimalj.model;


/**
 * A class implementing View is a model class that holds only a part
 * of the fields of an other class.<p>
 * 
 * The objects of this class are never saved if they referenced
 * by other objects.<p>
 * 
 * Note that while the concept is similar to database views this
 * interface does <i>not</i> represent views on the database but
 * views on a java model entity.
 * 
 * @param <T> the class on which this view is based. Mandatory.
 */
public interface View<T> {

}
