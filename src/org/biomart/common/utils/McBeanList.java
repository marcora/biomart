package org.biomart.common.utils;

import java.beans.PropertyChangeEvent;
import java.util.Collection;
import java.util.List;
import java.util.ListIterator;

/**
 * This class wraps an existing collection, and causes
 * {@link PropertyChangeEvent} events to be fired whenever it changes.
 * <p>
 * Adding objects to the collection will result in events where the before value
 * is null and the after value is the value being added.
 * <p>
 * Removing them will result in events where the before value is they value
 * being removed and the after value is null.
 * <p>
 * Multiple add/remove events will have both before and after values of null.
 * <p>
 * All events will have a property of {@link BeanCollection#propertyName}.
 * 
 */
public class McBeanList<E> extends McBeanCollection<E> implements List<E> {

	public McBeanList(Collection<E> delegate) {
		super(delegate);
	}

	public void add(int index, E e) {
		((List<E>)this.delegate).add(index, e);
		this.firePropertyChange(McBeanCollection.property_AddItem, null, e);
	}

	public boolean addAll(int index, Collection<? extends E> c) {
		final boolean result = ((List<E>) this.delegate).addAll(index, c);
		if (result)
			this.firePropertyChange(McBeanCollection.property_AddAll, null, null);
		return result;
	}

	public E get(int index) {
		return ((List<E>) this.delegate).get(index);	}

	public int indexOf(Object o) {
		return ((List<E>) this.delegate).indexOf(o);
	}

	public int lastIndexOf(Object o) {
		return ((List<E>) this.delegate).lastIndexOf(o);
	}

	public ListIterator<E> listIterator() {
		return ((List<E>) this.delegate).listIterator();
	}

	public ListIterator<E> listIterator(int index) {
		return ((List<E>) this.delegate).listIterator(index);
	}

	public E remove(int index) {
		final E result = ((List<E>) this.delegate).remove(index);
		this.firePropertyChange(McBeanCollection.property_RemoveItem, result, null);
		return result;
	}

	public E set(int index, E e) {
		return ((List<E>) this.delegate).set(index, e);
	}

	public List<E> subList(int fromIndex, int toIndex) {
		return ((List<E>) this.delegate).subList(fromIndex, toIndex);
	}
	
}