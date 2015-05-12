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
package gov.nij.bundles.intermediaries.ers.osgi;

import java.util.HashSet;
import java.util.Set;

/**
 * An equivalent, serializable version of a SERF attribute.  We need one that is independent of SERF so we don't have to expose SERF packages to OSGi clients.
 * @author scott
 *
 */
public class AttributeWrapper
{
	
	private String type;
    private Set<String> values;

    /**
     * Create an instance for an attribute with the specified name, and an (initially) empty set of values.
     * @param type the name of the attribute
     */
    public AttributeWrapper(String type) {
        this.type = type;
        this.values = new HashSet<String>();
    }
    
    /**
     * Create an instance for an attribute with the specified name and list of values
     * @param type the name of the attribute
     * @param values
     */
    public AttributeWrapper(String type, String... values) {
        this(type);
        
        for (String value : values)
        {
            this.addValue(value);
        }
    }
    
    /**
     * Add a value to the list of values for this attribute.
     * @param text
     */
    public void addValue(String text)
    {
    	if (text == null || text.equals(""))
    	{
    		return;
    	}
        values.add(text);
    }
    
    public String getType()
    {
    	return type;
    }

	public Set<String> getValues()
	{
		return values;
	}
	
	public int hashCode()
	{
		return 17*type.hashCode() + (values == null ? 0 : values.hashCode());
	}
	
	public boolean equals(Object o)
	{
		return o != null && o instanceof AttributeWrapper && o.hashCode() == hashCode();
	}
	
	public String toString()
	{
		return super.toString() + " [type=" + type + ", values=" + values + "]";
	}
    
}
