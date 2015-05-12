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

import gov.nij.er.StringDistanceScoreMatcher;

import java.util.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import serf.data.*;

/**
 *
 * An extension of the SERF BasicMatcherMerger class that uses a specified StringDistanceScoreMatcher.
 * The rules for matching records are that two records match if:
 * They are both null, or
 * They are both empty, or
 *  They contain the same attributes (the "type" of each attribute is the same) and the StringDistanceScoreMatcher
 *      scores the attributes higher than its threshold
 *
 */
class ConfigurableMatcherMerger extends BasicMatcherMerger
{
	
	private static final Log LOG = LogFactory.getLog( ConfigurableMatcherMerger.class );

    private StringDistanceScoreMatcher stringDistanceScoreMatcher;

    /**
     * Create an instance that uses the specified string distance algorithm
     * @param stringDistanceImplementationClassName the algorithm to use
     */
    public ConfigurableMatcherMerger(String stringDistanceImplementationClassName)
    {
        this.stringDistanceScoreMatcher = new StringDistanceScoreMatcher(stringDistanceImplementationClassName);
        super._factory = new SimpleRecordFactory();
    }
    
    /**
     * Initialize the object, using the default threshold defined by gov.nij.er.StringDistanceScoreMatcher
     */
    public void init()
    {
        try
        {
            stringDistanceScoreMatcher.init();
        } catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    /**
     * Initialize the object, using the specified threshold
     * @param scoreThreshold
     */
    public void init(double scoreThreshold)
    {
        try
        {
            stringDistanceScoreMatcher.init(scoreThreshold);
        } catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }
    
    /**
     * Set the distance threshold for matching
     * @param stringDistanceThreshold
     */
    public void setThreshold(double stringDistanceThreshold)
    {
        stringDistanceScoreMatcher.setThreshold(stringDistanceThreshold);
    }
    
    /**
     * Get the threshold being used for matching
     * @return
     */
    public double getThreshold()
    {
        return stringDistanceScoreMatcher.getThreshold();
    }

    /**
     * Perform a match on the two specified records
     */
    @Override
    protected boolean matchInternal(Record r1, Record r2)
    {
    	LOG.debug("Comparing r1=" + r1 + " to r2=" + r2);
        ExistentialBooleanComparator ebc = new ExistentialBooleanComparator(stringDistanceScoreMatcher);
        Map<String, Attribute> r1attr = r1.getAttributes();
        Map<String, Attribute> r2attr = r2.getAttributes();
        if (!haveSameAttributes(r1attr, r2attr))
        {
        	LOG.debug("Records do not have same attributes");
            return false;
        }
        for (String s1 : r1attr.keySet())
        {
            Attribute a1 = r1attr.get(s1);
            Attribute a2 = r2attr.get(s1);
            if (!ebc.attributesMatch(a1, a2))
            {
            	LOG.debug("Mismatched attributes a1=" + a1 + ", a2=" + a2);
                return false;
            }
        }
        LOG.debug("Records match");
        return true;
    }

    private boolean haveSameAttributes(Map<String, Attribute> r1attr, Map<String, Attribute> r2attr)
    {
        return (r1attr == null && r2attr == null) || (r1attr.isEmpty() && r2attr.isEmpty())
                || (r1attr.keySet().containsAll(r2attr.keySet()) && r2attr.keySet().containsAll(r1attr.keySet()));
    }

}
