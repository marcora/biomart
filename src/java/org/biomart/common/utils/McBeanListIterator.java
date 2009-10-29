package org.biomart.common.utils;

import java.beans.PropertyChangeEvent;
import java.util.Iterator;
import java.util.ListIterator;
/**
 * This class wraps an existing iterator, and causes {@link PropertyChangeEvent}
 * events to be fired whenever it changes.
 * <p>
 * Adding values will result in events where the before value is null and the
 * after value is the value being added.
 * <p>
 * Removing values will result in events where the before value is the value
 * being removed and the after value is null.
 * <p>
 * All events will have a property of {@link BeanListIterator#propertyName}.
 * 
 */
public class McBeanListIterator<E> extends McBeanIterator<E> implements ListIterator<E> {

	public McBeanListIterator(Iterator<E> delegate) {
		super(delegate);
	}

	public void add(E arg0) {
		((ListIterator<E>) this.delegate).add(arg0);
		this.firePropertyChange(McBeanIterator.property_Add, null, arg0);
	}

	public boolean hasPrevious() {
		return ((ListIterator<E>) this.delegate).hasPrevious();
	}

	public int nextIndex() {
		return ((ListIterator<E>) this.delegate).nextIndex();
	}

	public E previous() {
		this.currentObj = ((ListIterator<E>) this.delegate).previous();		
		return this.currentObj;
	}

	public int previousIndex() {
		return ((ListIterator<E>) this.delegate).previousIndex();
	}

	public void set(E arg0) {
		((ListIterator<E>) this.delegate).set(arg0);
	}	
}