package com.jaewanyun.omoc.net;

import java.io.Serializable;
import java.util.Arrays;
import java.util.NoSuchElementException;

/*
 * The MIT License
 *
 * Copyright (c) 2016 Jaewan Yun <jay50@pitt.edu>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

/**
 * A blocking dequeue.
 *
 * @author Jaewan Yun (Jay50@pitt.edu)
 * @version 1.0.0
 */

public class JayList<T> implements Serializable {

	private static final long serialVersionUID = 913209149835234L;
	private volatile T[] jayList = null;
	private final int DEFAULT_CAPACITY = 1024;
	private final double EXPANSION_FACTOR = 2.0;
	private final double REDUCTION_FACTOR = 2.0;
	private final int REDUCTION_REQUIREMENT_MIN = 1025;
	private final int REDUCTION_REQUIREMENT_FACTOR = 4;
	private final int MAX_CAPACITY = (2147483647 / (int) EXPANSION_FACTOR);
	private volatile int size = 0;
	private volatile int capacity = 0;
	private volatile boolean initialized = false;
	private volatile int headCursor = 0;
	private volatile int tailIndex = 0;

	/**
	 * @since 1.0.0
	 * @author Jaewan Yun (Jay50@pitt.edu)
	 */
	public JayList() {
		initialized = true;
		jayList = constructArray(DEFAULT_CAPACITY);
		capacity = DEFAULT_CAPACITY;
	}

	/**
	 * @param capacity The desired capacity of the underlying data structure.
	 * @throws IllegalArgumentException When the size of the accepted value exceeds a predetermined maximum capacity or is less than one.
	 * @since 1.0.0
	 * @author Jaewan Yun (Jay50@pitt.edu)
	 */
	public JayList(int capacity) {
		initialized = true;
		jayList = constructArray(capacity);
		this.capacity = capacity;
	}

	/**
	 * @param input An array used as a template.
	 * @return True when storage was successful, and false if otherwise.
	 * @since 1.0.0
	 * @author Jaewan Yun (Jay50@pitt.edu)
	 */
	public JayList(T[] input) {
		initialized = true;
		setArray(input, input.length);
	}

	/**
	 * @param entry An entry to be added.
	 * @throws IllegalStateException When this has not been properly initialized or when entry cannot be added due to a predetermined maximum capacity.
	 * @throws IllegalArgumentException When entry is null.
	 * @since 1.0.0
	 * @author Jaewan Yun (Jay50@pitt.edu)
	 */
	public synchronized T addFirst(T entry) {
		checkInitialization();

		if(entry == null)
			throw new IllegalArgumentException();

		// Add the entry to the headCursor position and increment headCursor using modulo.
		if(isFull())
			increaseCapacity(EXPANSION_FACTOR, -1);

		jayList[headCursor] = entry;
		headCursor = (headCursor + 1) % capacity;
		size++;

		if(headCursor == 0)
			return jayList[capacity - 1];
		else
			return jayList[headCursor - 1];
	}

	/**
	 * @param entry An entry to be added.
	 * @throws IllegalStateException When this has not been properly initialized or when entry cannot be added due to a predetermined maximum capacity.
	 * @throws IllegalArgumentException When entry is null.
	 * @since 1.0.0
	 * @author Jaewan Yun (Jay50@pitt.edu)
	 */
	public synchronized T addLast(T entry) {
		checkInitialization();

		if(entry == null)
			throw new IllegalArgumentException();

		if(isFull())
			increaseCapacity(EXPANSION_FACTOR, -1);

		if(tailIndex == 0) {
			tailIndex = capacity - 1;
			jayList[tailIndex] = entry;
		} else {
			jayList[--tailIndex] = entry;
		}
		size++;

		return jayList[tailIndex];
	}

	/**
	 * @param position The relative position (not the underlying index) at which the entry will be inserted into.
	 * @param entry An entry to be added.
	 * @throws IllegalStateException When this has not been properly initialized or when entry cannot be added due to a predetermined maximum capacity.
	 * @throws IllegalArgumentException When entry is null or if the position is invalid.
	 * @since 1.1.0
	 * @author Jaewan Yun (Jay50@pitt.edu)
	 */
	public synchronized T add(int position, T entry) {
		checkInitialization();

		if(entry == null || position < 0 || position > size)
			throw new IllegalArgumentException();

		if(size < 3)
			increaseCapacity(EXPANSION_FACTOR, -1);

		if(isFull()) {
			increaseCapacity(EXPANSION_FACTOR, position);
			jayList[size - position] = entry;
			size++;
		} else if(size == 0 || position == size) {
			addLast(entry);
		} else if(position == 0) {
			addFirst(entry);
		} else {
			int addIndex = ((tailIndex + capacity - 1) - position) % capacity;
			if(addIndex < size / 2) {
				for(int j = 0; j < position; j++) {
					addLast(removeFirst());
				}
				addFirst(entry);
				for(int j = 0; j < position; j++) {
					addFirst(removeLast());
				}
			} else {
				for(int j = 0; j < size - position; j++) {
					addFirst(removeLast());
				}
				addLast(entry);
				for(int j = 0; j < size - 1 - position; j++) {
					addLast(removeFirst());
				}
			}
		}

		return get(position);
	}

	/**
	 * @return The element that was removed.
	 * @throws NoSuchElementException If data structure is empty.
	 * @throws NullPointerException If removed value is null.
	 * @since 1.0.0
	 * @author Jaewan Yun (Jay50@pitt.edu)
	 */
	public synchronized T removeLast() {
		checkInitialization();

		// Check that data structure is non-empty
		if(isEmpty())
			throw new NoSuchElementException();

		// Remove an item from the tailIndex and increment tailIndex using modulo.
		T toReturn = jayList[tailIndex];
		jayList[tailIndex] = null;
		tailIndex = ++tailIndex % capacity;
		size--;

		// Reduce capacity.
		if((size < (capacity / REDUCTION_REQUIREMENT_FACTOR)) && (capacity > REDUCTION_REQUIREMENT_MIN))
			decreaseCapacity(REDUCTION_FACTOR);

		if(toReturn == null)
			throw new NullPointerException();

		return toReturn;
	}

	/**
	 * @return The element that was popped.
	 * @throws NoSuchElementException If data structure is empty.
	 * @throws NullPointerException If popped value is null.
	 * @since 1.0.0
	 * @author Jaewan Yun (Jay50@pitt.edu)
	 */
	public synchronized T removeFirst() {
		checkInitialization();

		// Check that data structure is non-empty
		if(isEmpty())
			throw new NoSuchElementException();

		T toReturn;
		if(headCursor == 0) {
			headCursor = capacity - 1;
			toReturn = jayList[headCursor];
			jayList[headCursor] = null;
		} else {
			toReturn = jayList[--headCursor];
			jayList[headCursor] = null;
		}
		size--;

		// Reduce capacity.
		if((size < (capacity / REDUCTION_REQUIREMENT_FACTOR)) && (capacity > REDUCTION_REQUIREMENT_MIN))
			decreaseCapacity(REDUCTION_FACTOR);

		if(toReturn == null)
			throw new NullPointerException();

		return toReturn;
	}

	/**
	 * @param position The relative position (not the underlying index) at which the entry will be removed from.
	 * @throws IllegalStateException When this has not been properly initialized.
	 * @throws IllegalArgumentException If the position is invalid.
	 * @throws NoSuchElementException If data structure is empty.
	 * @throws NullPointerException If popped value is null.
	 * @since 1.1.0
	 * @author Jaewan Yun (Jay50@pitt.edu)
	 */
	public synchronized T remove(int position) {
		checkInitialization();

		if(position < 0 || position >= size)
			throw new IllegalArgumentException();

		// Check that data structure is non-empty
		if(isEmpty())
			throw new NoSuchElementException();

		// Shift first or last positions into last or first positions until position is met, respectively, and insert at the open position.
		T toReturn = null;
		if(position < (size / 2)) {
			for(int j = 0; j < position; j++) {
				addLast(removeFirst());
			}
			toReturn = removeFirst();
			// TODO: Room for speed improvement
			for(int j = 0; j < position; j++) {
				addFirst(removeLast());
			}
		} else {
			for(int j = 0; j < position; j++) {
				addFirst(removeLast());
			}
			toReturn = removeLast();
			// TODO: Room for speed improvement
			for(int j = 0; j < position; j++) {
				addLast(removeFirst());
			}
		}

		// Reduce capacity.
		if((size < (capacity / REDUCTION_REQUIREMENT_FACTOR)) && (capacity > REDUCTION_REQUIREMENT_MIN))
			decreaseCapacity(REDUCTION_FACTOR);

		if(toReturn == null)
			throw new NullPointerException();

		return toReturn;
	}

	/**
	 * @return The element that is last.
	 * @throws NoSuchElementException If data structure is empty.
	 * @throws NullPointerException If next value is null.
	 * @since 1.0.0
	 * @author Jaewan Yun (Jay50@pitt.edu)
	 */
	public synchronized T getLast() {
		checkInitialization();

		// Check that data structure is non-empty
		if(isEmpty())
			throw new NoSuchElementException();

		// Get next.
		T toReturn = jayList[tailIndex];

		if(toReturn == null)
			throw new NullPointerException();

		return toReturn;
	}

	/**
	 * @return The element that is first.
	 * @throws NoSuchElementException If data structure is empty.
	 * @throws NullPointerException If next value is null.
	 * @since 1.0.0
	 * @author Jaewan Yun (Jay50@pitt.edu)
	 */
	public synchronized T getFirst() {
		checkInitialization();

		// Check that data structure is non-empty
		if(isEmpty())
			throw new NoSuchElementException();

		// Get next.
		T toReturn;

		if(headCursor == 0)
			toReturn = jayList[capacity - 1];
		else
			toReturn = jayList[headCursor - 1];

		if(toReturn == null)
			throw new NullPointerException();

		return toReturn;
	}

	/**
	 * @param position The entry to return at this relative position.
	 * @return The element at the relative position.
	 * @throws NoSuchElementException If data structure is empty.
	 * @throws NullPointerException If the value is null.
	 * @throws IllegalArgumentException If the position is invalid.
	 * @since 1.0.0
	 * @author Jaewan Yun (Jay50@pitt.edu)
	 */
	public synchronized T get(int position) {
		checkInitialization();

		if(position < 0 || position >= size)
			throw new IllegalArgumentException(position + " " + size);

		// Check that data structure is non-empty
		if(isEmpty())
			throw new NoSuchElementException();

		T toReturn = null;
		int relativePosition = (tailIndex + size - position - 1) % capacity;
		toReturn = jayList[relativePosition];

		if(toReturn == null)
			throw new NullPointerException();

		return toReturn;
	}

	/**
	 * @param factor The multiplicative expansion coefficient.
	 * @throws IllegalArgumentException When capacity cannot increase due to a predetermined maximum capacity.
	 * @since 1.0.0
	 * @author Jaewan Yun (Jay50@pitt.edu)
	 */
	private void increaseCapacity(double factor, int position) {
		// Increase capacity.
		if((int) (capacity * EXPANSION_FACTOR + 1) > MAX_CAPACITY)
			throw new IllegalStateException();

		int originalCapacity = capacity;
		capacity = (int) (capacity * factor);

		T[] temporaryRef = constructArray(capacity);
		if(position == -1) {
			for(int j = 0; j < size; j++) {
				temporaryRef[j] = jayList[tailIndex % originalCapacity];
				tailIndex++;
			}
			tailIndex = 0;
			headCursor = size;
			jayList = temporaryRef;
		} else {
			position = size - position;
			for(int j = 0; j <= size; j++) {
				if(j == position) {
					if(tailIndex == 0)
						tailIndex = capacity - 1;
					else
						tailIndex--;
				}
				temporaryRef[j] = jayList[tailIndex % originalCapacity];
				tailIndex++;
			}
			tailIndex = 0;
			headCursor = size + 1;
			jayList = temporaryRef;
		}
	}

	/**
	 * @param factor The multiplicative reduction coefficient.
	 * @throws IllegalArgumentException When capacity cannot increase due to a predetermined maximum capacity.
	 * @since 1.0.0
	 * @author Jaewan Yun (Jay50@pitt.edu)
	 */
	private void decreaseCapacity(double factor) {
		// Decrease capacity.
		int originalCapacity = capacity;

		capacity = (int) (capacity / factor);

		T[] temporaryRef = constructArray(capacity);
		for(int j = 0; j < capacity - 1; j++) {
			temporaryRef[j] = jayList[tailIndex++ % originalCapacity];
		}
		tailIndex = 0;
		headCursor = size;
		jayList = temporaryRef;
	}

	/**
	 * @param find The object to find the index of.
	 * @return The index of the object; null if not found.
	 * @throws IllegalStateException When this has not been properly initialized.
	 * @since 1.2.0
	 * @author Jaewan Yun (Jay50@pitt.edu)
	 */
	public synchronized Integer index(T find) {
		checkInitialization();

		for(int j = 0; j < size; j++) {
			if(get(j).equals(find))
				return new Integer(j);
		}

		return null;
	}

	/**
	 * @param jayList The JayList to add to the last index of this.
	 * @return The extended JayList.
	 * @throws IllegalStateException When this has not been properly initialized.
	 * @since 1.2.0
	 * @author Jaewan Yun (Jay50@pitt.edu)
	 */
	public synchronized JayList<T> extend(JayList<T> jayList) {
		checkInitialization();

		for(int j = 0, k = jayList.size(); j < k; j++) {
			addLast(jayList.removeFirst());
		}

		return this;
	}

	/**
	 * @return True if values were cleared; false if no valued exists.
	 * @throws IllegalStateException When this has not been properly initialized.
	 * @since 1.0.0
	 * @author Jaewan Yun (Jay50@pitt.edu)
	 */
	public synchronized boolean clear() {
		checkInitialization();

		if(isEmpty())
			return false;

		jayList = null;
		jayList = constructArray(DEFAULT_CAPACITY);

		capacity = DEFAULT_CAPACITY;
		size = 0;

		headCursor = 0;
		tailIndex = 0;

		return true;
	}

	/**
	 * @param input An array to be used as a template.
	 * @return True if storage was successful, and false if otherwise.
	 * @throws IllegalStateException If this has not been properly initialized.
	 * @throws IllegalArgumentException If capacity cannot increase due to a predetermined maximum capacity.
	 * @since 1.0.0
	 * @author Jaewan Yun (Jay50@pitt.edu)
	 */
	public synchronized boolean setArray(T[] input) {
		return setArray(input, input.length);
	}
	/**
	 * @param input An array used as a template.
	 * @return True if storage was successful, and false if otherwise.
	 * @throws IllegalStateException If this has not been properly initialized.
	 * @throws IllegalArgumentException If capacity cannot increase due to a predetermined maximum capacity.
	 * @since 1.0.0
	 * @author Jaewan Yun (Jay50@pitt.edu)
	 */
	private boolean setArray(T[] input, int length) {
		checkInitialization();

		if(input == null)
			return false;

		if(length + 1 > MAX_CAPACITY)
			throw new IllegalArgumentException();

		jayList = constructArray(length + 1);
		capacity = length + 1;

		// Copy references
		size = 0;
		for(int j = 0; j < length; j++) {
			if(input[j] != null) {
				jayList[(length - 1) - j] = input[j];
				size++;
			}
		}

		tailIndex = 0;
		headCursor = length;

		return true;
	}

	/**
	 * @return A copy of this array.
	 * @throws IllegalStateException When this has not been properly initialized.
	 * @throws NullPointerException When jayList is null.
	 * @since 1.0.0
	 * @author Jaewan Yun (Jay50@pitt.edu)
	 */
	@SuppressWarnings("unchecked") public synchronized Object[] toArray() {
		checkInitialization();

		int newTailIndex = tailIndex;
		T[] toReturn = (T[]) new Object[size];
		for(int j = size - 1; j >= 0; j--) {
			toReturn[j] = jayList[newTailIndex++ % capacity];
		}

		return toReturn;
	}

	/**
	 *  TODO: Test this method.
	 *
	 *  @param template The template with which the array will be constructed.
	 * @return A copy of this array.
	 * @throws IllegalStateException When this has not been properly initialized.
	 * @throws NullPointerException When jayList is null.
	 * @throws NullPointerException When input parameter is null or its size is different.
	 * @since 1.0.0
	 * @author Jaewan Yun (Jay50@pitt.edu)
	 */
	public synchronized T[] toArray(T[] template) {
		checkInitialization();

		if(template == null)
			throw new NullPointerException();
		if(template.length < size() || template.length == 0)
			throw new IllegalArgumentException();

		int newTailIndex = tailIndex;
		for(int j = size - 1; j >= 0; j--) {
			template[j] = jayList[newTailIndex++ % capacity];
		}

		return template;
	}

	/**
	 * @param capacity The capacity of the array to be constructed.
	 * @return An initialized array of T types with the accepted value as its capacity.
	 * @throws IllegalArgumentException When the size of the accepted value exceeds a predetermined maximum capacity.
	 * @throws IllegalArgumentException When the size of the accepted value is less than one.
	 * @since 1.0.0
	 * @author Jaewan Yun (Jay50@pitt.edu)
	 */
	@SuppressWarnings("unchecked") private T[] constructArray(int capacity) {
		if(capacity > MAX_CAPACITY || capacity < 1)
			throw new IllegalArgumentException();

		// Initialize an array of type T
		T[] toReturn = (T[]) new Object[capacity];

		// Setting the states
		initialized = true;

		return toReturn;
	}

	/**
	 * @return true if sort was successful; false if no values exists.
	 * @throws IllegalArgumentException If object types are not comparable.
	 * @since 1.1.0
	 * @author Jaewan Yun (Jay50@pitt.edu)
	 */
	public synchronized boolean sort() {
		if(isEmpty())
			return false;

		try {
			@SuppressWarnings("unchecked")
			T[] a = (T[]) toArray();
			Arrays.sort(a);
			setArray(a);
			return true;
		} catch(Exception e) {
			throw new IllegalArgumentException();
		}
	}

	/**
	 * @return The number of elements contained within this data structure.
	 * @since 1.0.0
	 * @author Jaewan Yun (Jay50@pitt.edu)
	 */
	public synchronized int size() {
		return size;
	}

	/**
	 * @throws IllegalStateException When this has not been properly initialized.
	 * @since 1.0.0
	 * @author Jaewan Yun (Jay50@pitt.edu)
	 */
	private void checkInitialization() {
		if(!initialized)
			throw new IllegalStateException();
	}

	/**
	 * @return True if no elements exist in this data structure.
	 * @since 1.0.0
	 * @author Jaewan Yun (Jay50@pitt.edu)
	 */
	public synchronized boolean isEmpty() {
		return headCursor == tailIndex;
	}

	/**
	 * @return True if data represented is in full state.
	 * @since 1.0.0
	 * @author Jaewan Yun (Jay50@pitt.edu)
	 */
	private boolean isFull() {
		return ((headCursor + 1) % capacity) == tailIndex;
	}

	/**
	 * @since 1.0.0
	 * @author Jaewan Yun (Jay50@pitt.edu)
	 */
	@Override
	public synchronized String toString() {
		return Arrays.toString(jayList);
	}

	/**
	 * Package private debugging method.
	 *
	 * @param keyword Keyword that the method body portion execution is dependent on
	 * @since 1.1.0
	 * @author Jaewan Yun (Jay50@pitt.edu)
	 */
	synchronized void showState() {
		print(1, "jayList Address :\t" + jayList);
		print(1, "MAX_CAPACITY :\t\t" + MAX_CAPACITY);
		print(1, "DEFAULT_CAPACITY :\t" + DEFAULT_CAPACITY);
		print(1, "EXPANSION_FACTOR :\t" + EXPANSION_FACTOR);
		print(1, "REDUCTION_FACTOR :\t" + REDUCTION_FACTOR);
		print(1, "capacity :\t\t" + capacity + "\t<---B");
		print(1, "initialized :\t\t" + initialized);
		print(1, "headCursor :\t\t" + headCursor);
		print(1, "tailIndex :\t\t" + tailIndex);
		print(1, "\n\tEND OF JayList EXPLICIT STATE\n");

		if(jayList != null) {
			print(1, "length :\t\t" + jayList.length);
			if(jayList[tailIndex] != null)
				print(1, "tailIndex type :\t" + jayList[tailIndex].getClass().toString());
			else
				print(1, "tailIndex type :\tnull");
			if(jayList[headCursor] != null)
				print(1, "headCursor type :\t" + jayList[tailIndex].getClass().toString());
			else
				print(1, "headCursor type :\tnull");
			if(headCursor - 1 < 0)
				if(jayList[capacity - 1] != null)
					print(1, "headIndex type :\t" + jayList[tailIndex].getClass().toString());
			if(headCursor - 1 >= 0)
				if(jayList[headCursor - 1] != null)
					print(1, "headIndex type :\t" + jayList[tailIndex].getClass().toString());
			print(1, "\n\tEND OF T[] EXPLICIT STATE\n");

			for(int j = 0; j < jayList.length; j++) {
				print(0, "Index  " + j + ": \t[" + jayList[j]);
				if(jayList[j] != null)
					print(1, "\t] of type (" + jayList[j].getClass().toString() + ")");
				else
					print(0, "\t]\n");
			}
			print(1, "\n\tEND OF T[] ENUMERATION");
		} else {
			print(2, "jayList is null therefore unaccessible");
		}
	}

	/**
	 * @since 1.0.0
	 * @author Jaewan Yun (Jay50@pitt.edu)
	 */
	private static void print(int skip, String toPrint) {
		System.out.print(toPrint);

		if(skip == 0)
			return;

		for(int j = 0; j < skip; j++) {
			System.out.print("\n");
		}
	}
}