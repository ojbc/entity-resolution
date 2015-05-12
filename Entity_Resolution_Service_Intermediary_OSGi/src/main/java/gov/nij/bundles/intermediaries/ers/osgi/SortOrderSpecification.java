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

import java.io.Serializable;

/**
 * A simple POJO tying together a sort order (ascending vs descending) and a rank of this criterion among a set of several criteria.
 * @author scott
 *
 */
public class SortOrderSpecification implements Serializable {
    
    public static final String SORT_ORDER_ASCENDING = "ascending";
    public static final String SORT_ORDER_DESCENDING = "descending";
    private static final long serialVersionUID = -2697143897879392761L;
    
    private int sortOrderRank;
    private String sortOrder;
    
    public SortOrderSpecification() {
        super();
    }
    
    public SortOrderSpecification(int sortOrderRank, String sortOrder) {
        this.sortOrderRank = sortOrderRank;
        this.sortOrder = sortOrder;
        if (!(SORT_ORDER_ASCENDING.equals(sortOrder) || SORT_ORDER_DESCENDING.equals(sortOrder))) {
            throw new IllegalArgumentException("Sort order must be " + SORT_ORDER_ASCENDING + " or " + SORT_ORDER_DESCENDING + ", not " + sortOrder);
        }
    }

    public int getSortOrderRank() {
        return sortOrderRank;
    }
    
    public void setSortOrderRank(int sortOrderRank) {
        this.sortOrderRank = sortOrderRank;
    }

    public String getSortOrder() {
        return sortOrder;
    }
    
    public void setSortOrder(String sortOrder) {
        this.sortOrder = sortOrder;
    }

}
