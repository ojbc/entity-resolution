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

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A container for the results of a particular invocation of entity resolution.  The container has a list of merged records, and a set of statistics.
 *
 */
public class EntityResolutionResults {
	
	private List<RecordWrapper> records;
	private Map<String, Set<AttributeStatistics>> statistics;
	private boolean recordLimitExceeded;
	
	public EntityResolutionResults(List<RecordWrapper> records,
			Map<String, Set<AttributeStatistics>> statistics, boolean recordLimitExceeded) {
		this.records = records;
        this.statistics = statistics;
        this.recordLimitExceeded = recordLimitExceeded;
	}

	public boolean isRecordLimitExceeded() {
        return recordLimitExceeded;
    }

    public List<RecordWrapper> getRecords() {
		return records;
	}

	/**
	 * Get the resolution statistics (strength of match measures) for a particular merged record
	 * @param recordId the id of the record of interest
	 * @return the statistics object
	 */
	public Set<AttributeStatistics> getStatisticsForRecord(String recordId) {
		return statistics.get(recordId);
	}

}
