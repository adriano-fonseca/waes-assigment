package com.company.app.infra;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Composition with ArrayList in order to not allow null elements
 * 
 * @author adriano-fonseca
 *
 * @param <E>
 */
public class ArrayListNoNull<E> implements List<E>, Serializable {

	private static final long serialVersionUID = 1L;
	
	private final List<E> wrappedList = new ArrayList<E>();
  private final Log logger = LogFactory.getLog(getClass());
	
	@Override
	public int size() {
		return wrappedList.size();
	}

	@Override
	public boolean isEmpty() {
		return wrappedList.isEmpty();
	}

	@Override
	public boolean contains(Object o) {
		return wrappedList.contains(o);
	}

	@Override
	public Iterator<E> iterator() {
		return wrappedList.iterator();
	}

	@Override
	public Object[] toArray() {
		return wrappedList.toArray();
	}

	@Override
	public <T> T[] toArray(T[] a) {
		return wrappedList.toArray(a);
	}

	@Override
	public boolean remove(Object o) {
		return wrappedList.remove(o);
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return wrappedList.containsAll(c);
	}
	
	private boolean isNullElement(E e) {
		if (e == null) {
			final String message = "Add an element null it is not allowed to an instance of " + ArrayListNoNull.class.getSimpleName();
			logger.warn(message);
			return true;
		}
		return false;
	}
	
	@Override
	public boolean add(E e) {
		return isNullElement(e) ? false : wrappedList.add(e);
	}

	@Override
	public boolean addAll(Collection<? extends E> c) {
		if (c == null || c.isEmpty())
			return false;
		boolean addedElement = false;
		for (E e : c) {
			if (add(e))
				addedElement = true;
			else 
				logger.warn(ArrayListNoNull.class.getSimpleName() + "#addAll(Collection) has tryed to add a element null in the collection: " + c);
		}
		return addedElement;
	}

	@Override
	public boolean addAll(int index, Collection<? extends E> c) {
		for (E e : c) {
			if (isNullElement(e))
				throw new IllegalArgumentException("It is not allowed to add, via #addAll(int,Collection), collection with a element null in a instance of " + ArrayListNoNull.class.getSimpleName() + ". Collection: " + c);
		}
		return wrappedList.addAll(index, c);
	}
	
	@Override
	public void add(int index, E element) {
		if (!isNullElement(element)) 
			wrappedList.add(index, element);		
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		return wrappedList.removeAll(c);
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		return wrappedList.retainAll(c);
	}

	@Override
	public void clear() {
		wrappedList.clear();
	}

	@Override
	public E get(int index) {
		return wrappedList.get(index);
	}

	@Override
	public E set(int index, E element) {
		return wrappedList.set(index, element);
	}

	@Override
	public E remove(int index) {
		return wrappedList.remove(index);
	}

	@Override
	public int indexOf(Object o) {
		return wrappedList.indexOf(o);
	}

	@Override
	public int lastIndexOf(Object o) {
		return wrappedList.lastIndexOf(o);
	}

	@Override
	public ListIterator<E> listIterator() {
		return wrappedList.listIterator();
	}

	@Override
	public ListIterator<E> listIterator(int index) {
		return wrappedList.listIterator(index);
	}

	@Override
	public List<E> subList(int fromIndex, int toIndex) {
		return wrappedList.subList(fromIndex, toIndex);
	}

	@Override
	public boolean equals(Object obj) {
		return wrappedList.equals(obj);
	}
	
	@Override
	public int hashCode() {
		return wrappedList.hashCode();
	}

	@Override
	public String toString() {
		return wrappedList.toString();
	}
	
}
