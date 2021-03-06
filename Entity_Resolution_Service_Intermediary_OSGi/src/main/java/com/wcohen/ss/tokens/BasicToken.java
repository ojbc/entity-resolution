package com.wcohen.ss.tokens;

import com.wcohen.ss.api.*;


/**
 * An interned version of a string.    
 *
 */

public class BasicToken implements Token, Comparable<Token>
{
	private final int index;
	private final String value;
	
	BasicToken(int index,String value) {
		this.index = index;
		this.value = value;
	}
	public String getValue() { return value; }
	public int getIndex() { return index; }
	public int compareTo(Token t) {
		return index - t.getIndex();
	} 
	public int hashCode() { return value.hashCode(); }
	public String toString() { return "[tok "+getIndex()+":"+getValue()+"]"; }
}
