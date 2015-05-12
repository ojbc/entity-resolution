package serf.data;



import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * ExistentialBooleanComparator compares two attributes by comparing their attribute values.
 *
 */
public class ExistentialBooleanComparator {

	private static final Log LOG = LogFactory.getLog( ExistentialBooleanComparator.class );
	
	AtomicMatch valueMatcher;
	
	public ExistentialBooleanComparator(AtomicMatch am) {
		valueMatcher = am;
	}
	
	public boolean attributesMatch(Attribute p1, Attribute p2)
	{
		return attributesMatch(p1, p2, valueMatcher);
	}
	
	/**
	 * Compare two attributes. Return true if they match.
	 * attributesMatch takes the cross product of values from each Attribute and 
	 * invokes valuesMatch() on each pair.  It returns true if any invocation of valuesMatch
	 * returns true.
	 * @param p1 Attribute 1
	 * @param p1 Attribute 2
	 * @param vm an object that compares Attribute values.
	 * @return true if any pair of attribute values matches.
	 * Note: if either Attribute parameter is null this method returns false.
	 * 
	 */
	public static boolean attributesMatch(Attribute p1, Attribute p2, AtomicMatch vm)
	{
		LOG.debug("attributesMatch, p1=" + p1 + ", p2=" + p2);
		
		if (p1 == null || p2 == null)
		{
			LOG.debug("Returning false due to null attribute " + (p1 == null ? "p1" : "p2"));
			return false;
		}
		
		if (p1.getValuesCount() == 0 && p2.getValuesCount() == 0)
		{
			LOG.debug("returning a match of two empty attributes, p1=" + p1 + ", p2=" + p2);
			return true;
		}
		
		Iterator<String> i1 = p1.iterator();
		
		while(i1.hasNext())
		{
			String s1 = (String)i1.next();
			Iterator<String> i2 = p2.iterator();
			
			while(i2.hasNext())
			{
				String s2 = (String)i2.next();
				
				if (vm.valuesMatch(s1, s2))
				{
					LOG.debug("Returning true due to string match: s1=" + s1 + ", s2=" + s2);
					return true;
				}
				LOG.debug("Values don't match, s1=" + s1 + ", s2=" + s2);
			}
		}
		
		LOG.debug("Default: returning false");
		return false;
		
	}

}
