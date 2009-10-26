/*
 Copyright (C) 2006 EBI
 
 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.
 
 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the itmplied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.
 
 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.biomart.common.utils;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collection;
import java.util.List;
import java.util.ListIterator;

/**
 * This class wraps an existing list, and causes {@link PropertyChangeEvent}
 * events to be fired whenever it changes.
 * <p>
 * Adding objects to the list will result in events where the before value is
 * null and the after value is the value being added.
 * <p>
 * Removing them will result in events where the before value is they value
 * being removed and the after value is null.
 * <p>
 * Multiple add/remove events will have both before and after values of null.
 * <p>
 * All events will have a property of {@link BeanList#propertyName}.
 * 
 * @author Richard Holland <holland@ebi.ac.uk>
 * @version $Revision: 1.4 $, $Date: 2007/10/31 10:32:56 $, modified by 
 * 			$Author: rh4 $
 * @since 0.7
 */
public class BeanList<E> extends BeanCollection<E> implements List<E> {

	private static final long serialVersionUID = 1L;

	/**
	 * Construct a new instance that wraps the delegate list and produces
	 * {@link PropertyChangeEvent} events whenever the delegate list changes.
	 * 
	 * @param delegate
	 *            the delegate list.
	 */
	public BeanList(final List<E> delegate) {
		super(delegate);
	}

	public E get(final int index) {
		return ((List<E>) this.delegate).get(index);
	}

	public int indexOf(final Object o) {
		return ((List<E>) this.delegate).indexOf(o);
	}

	public int lastIndexOf(final Object o) {
		return ((List<E>) this.delegate).lastIndexOf(o);
	}

	private final PropertyChangeListener iteratorListener = new PropertyChangeListener() {
		public void propertyChange(final PropertyChangeEvent evt) {
			BeanList.this.firePropertyChange(
					BeanCollection.propertyName, evt.getOldValue(),
					evt.getNewValue());
		}
	};
	
	public ListIterator listIterator() {
		// Wrap the entry set in a BeanIterator.
		final BeanListIterator beanListIterator = new BeanListIterator(
				((List) this.delegate).listIterator());
		// Add a PropertyChangeListener to the BeanSet
		// which fires events as if they came from us.
		beanListIterator
				.addPropertyChangeListener(this.iteratorListener);
		// Return the wrapped entry set.
		return beanListIterator;
	}

	public ListIterator listIterator(final int index) {
		// Wrap the entry set in a BeanIterator.
		final BeanListIterator beanListIterator = new BeanListIterator(
				((List) this.delegate).listIterator(index));
		// Add a PropertyChangeListener to the BeanSet
		// which fires events as if they came from us.
		beanListIterator
				.addPropertyChangeListener(this.iteratorListener);
		// Return the wrapped entry set.
		return beanListIterator;
	}

	public E remove(final int index) {
		final E result = ((List<E>) this.delegate).remove(index);
		this.firePropertyChange(BeanCollection.propertyName, result, null);
		return result;
	}

	private final PropertyChangeListener subListIterator = new PropertyChangeListener() {
		public void propertyChange(final PropertyChangeEvent evt) {
			BeanList.this.firePropertyChange(BeanCollection.propertyName,
					evt.getOldValue(), evt.getNewValue());
		}
	};
	
	public List subList(final int fromIndex, final int toIndex) {
		final BeanList subList = new BeanList(((List) this.delegate).subList(
				fromIndex, toIndex));
		subList.addPropertyChangeListener(this.subListIterator);
		return subList;
	}

	public void add(final int arg0, final E arg1) {
		((List<E>) this.delegate).add(arg0, arg1);
		this.firePropertyChange(BeanCollection.propertyName, null, arg1);
	}

	public boolean addAll(final int arg0, final Collection<? extends E> arg1) {
		final boolean result = ((List<E>) this.delegate).addAll(arg0, arg1);
		if (result)
			this.firePropertyChange(BeanCollection.propertyName, null, arg1);
		return result;
	}

	public E set(final int arg0, final E arg1) {
		final E result = ((List<E>) this.delegate).set(arg0, arg1);
		this.firePropertyChange(BeanCollection.propertyName, null, arg1);
		return result;
	}
}
