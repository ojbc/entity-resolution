/*
 * Copyright 2013 SEARCH Group, Incorporated. 
 * 
 * See the NOTICE file distributed with  this work for additional information 
 * regarding copyright ownership.  SEARCH Group Inc. licenses this file to You
 * under the Apache License, Version 2.0 (the "License"); you may not use this 
 * file except in compliance with the License.  You may obtain a copy of the 
 * License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package gov.nij.er.test;

import gov.nij.bundles.intermediaries.ers.osgi.ExternallyIdentifiableRecord;
import gov.nij.er.*;

import java.util.*;

import junit.framework.*;

import org.junit.*;
import org.junit.Test;

import serf.data.*;
import serf.deduplication.*;

public class BasicERTest extends TestCase
{
    
    private static final String JARO_DISTANCE_IMPL = "com.wcohen.ss.Jaro";
    private ConfigurableMatcherMerger configurableMatcherMerger;

    @Before
    public void setUp()
    {
        configurableMatcherMerger = new ConfigurableMatcherMerger(JARO_DISTANCE_IMPL);
        configurableMatcherMerger.init();
    }
    
    @Test
    public void testCompareEmptyAttributes()
    {
    	
    	// note: the reason we use ExternallyIdentifiableRecords in this test is because that class has a better implementation
    	// of hashCode() than the default serf Record class.  Basically, serf Records that have identical attributes and confidence
    	// return the same value for hashCode() and therefore can't be put in the same HashSet.
    	
    	// this test exposes a bug discovered when delivering the pilot ER Demo UI to Hawaii
    	
    	Attribute a1 = new Attribute("givenName");
    	assertEquals(a1.getValuesCount(), 0);
        Record r1 = new ExternallyIdentifiableRecord(makeAttributeMap(a1), "r1");
        Record r2 = new ExternallyIdentifiableRecord(makeAttributeMap(a1), "r2");
        Set<Record> merged = RSwoosh.execute(configurableMatcherMerger, makeRecords(r1, r2));
        assertEquals(1, merged.size());
        
    }
    
    @Test
    public void testMatch()
    {
        Attribute a1 = new Attribute("givenName", "Andrew");
        Attribute a2 = new Attribute("surName", "Owen");
        Record r1 = new Record(1.0, makeAttributes(a1, a2));
        Record r2 = new Record(1.0, makeAttributes(a1, a2));
        assertTrue(configurableMatcherMerger.match(r1, r2));
        a2 = new Attribute("surName", "Jackson");
        r2 = new Record(1.0, makeAttributes(a1, a2));
        assertFalse(configurableMatcherMerger.match(r1, r2));
        a2 = new Attribute("lastName", "Owen");
        r2 = new Record(1.0, makeAttributes(a1, a2));
        assertFalse(configurableMatcherMerger.match(r1, r2));
        a2 = new Attribute("surName", "Owens");
        r2 = new Record(1.0, makeAttributes(a1, a2));
        assertTrue(configurableMatcherMerger.match(r1, r2));
        configurableMatcherMerger.setThreshold(0.99);
        assertFalse(configurableMatcherMerger.match(r1, r2));
    }
    
    

    @Test
    public void testEqualityStuff()
    {
        int x = 2;
        assertTrue(x == 2);
        Integer i1 = new Integer(1);
        Integer i2 = new Integer(1);
        assertFalse(i1 == i2);
        assertTrue(i1.equals(i2));
    }
    
    @Test
    public void testIdempotentMerge()
    {
        Attribute a1 = new Attribute("givenName", "Andrew");
        Attribute a2 = new Attribute("surName", "Owen");
        Record r1 = new Record(1.0, makeAttributes(a1, a2));
        Record r2 = new Record(1.0, makeAttributes(a1, a2));
        Set<Record> merged = RSwoosh.execute(configurableMatcherMerger, makeRecords(r1, r2));
        assertEquals(1, merged.size());
    }
    
    @Test
    public void testNonIdempotentMerge()
    {
        Attribute a1 = new Attribute("givenName", "Andrew");
        Attribute a2 = new Attribute("surName", "Owen");
        Record r1 = new Record(1.0, makeAttributes(a1, a2));
        a1 = new Attribute("givenName", "Andrei");
        Record r2 = new Record(1.0, makeAttributes(a1, a2));
        configurableMatcherMerger.init(.85);
        Set<Record> merged = RSwoosh.execute(configurableMatcherMerger, makeRecords(r1, r2));
        assertEquals(1, merged.size());
    }
    
    @Test
    public void testNonMerge()
    {
        Attribute a1 = new Attribute("givenName", "Andrew");
        Attribute a2 = new Attribute("surName", "Owen");
        Record r1 = new Record(1.0, makeAttributes(a1, a2));
        a2 = new Attribute("surName", "Jackson");
        Record r2 = new Record(1.0, makeAttributes(a1, a2));
        Set<Record> merged = RSwoosh.execute(configurableMatcherMerger, makeRecords(r1, r2));
        assertEquals(2, merged.size());
    }
    
    private static Set<Attribute> makeAttributes(Attribute ... attributes)
    {
        Set<Attribute> ret = new HashSet<Attribute>();
        ret.addAll(Arrays.asList(attributes));
        return ret;
    }
    
    private static Set<Record> makeRecords(Record ... records)
    {
        Set<Record> ret = new HashSet<Record>();
        ret.addAll(Arrays.asList(records));
        return ret;
    }
    
    private static Map<String, Attribute> makeAttributeMap(Attribute a1)
    {
    	Map<String, Attribute> ret = new HashMap<String, Attribute>();
    	ret.put(a1.getType(), a1);
    	return ret;
    }
    
}
