package com.wcohen.ss;

import com.wcohen.ss.api.*;
import java.util.Iterator;

/** A simple DistanceInstanceIterator implementation. 
 */

public class BasicDistanceInstanceIterator implements DistanceInstanceIterator {
	private Iterator<DistanceInstance> myIterator;
	public BasicDistanceInstanceIterator(Iterator<DistanceInstance> i) { myIterator=i; }
	public boolean hasNext() { return myIterator.hasNext(); }
	public DistanceInstance next() { return myIterator.next(); }
	public DistanceInstance nextDistanceInstance() { return (DistanceInstance)next(); }
	public void remove() { myIterator.remove(); }
}
