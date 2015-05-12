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
package gov.nij.bundles.intermediaries.ers;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.namespace.NamespaceContext;

/**
 * 
 * This class is a generic namespace context implementation that takes a hashmap as a constructor.
 * 
 * See this stackoverflow question for the code for this implementation:
 * http://stackoverflow.com/questions/6390339/how-to-query-xml-using-namespaces-in-java-with-xpath
 * 
 */

public class EntityResolutionNamespaceContextMapImpl implements NamespaceContext
{
	private final Map<String, String> PREF_MAP = new HashMap<String, String>();

    public EntityResolutionNamespaceContextMapImpl(final Map<String, String> prefMap) {
        PREF_MAP.putAll(prefMap);       
    }

    public String getNamespaceURI(String prefix) {
    	return PREF_MAP.get(prefix);
    }

    public String getPrefix(String uri) {
        throw new UnsupportedOperationException();
    }

    public Iterator getPrefixes(String uri) {
        throw new UnsupportedOperationException();
    }
}