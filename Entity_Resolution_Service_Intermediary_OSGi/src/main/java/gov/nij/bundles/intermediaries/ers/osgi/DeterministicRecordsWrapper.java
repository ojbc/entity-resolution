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

import java.util.ArrayList;
import java.util.List;

class DeterministicRecordsWrapper {

	List<ExternallyIdentifiableRecord> recordsThatAreNotMerged = new ArrayList<ExternallyIdentifiableRecord>();
	List<ExternallyIdentifiableRecord> recordsThatAreDeterministicallyMerged = new ArrayList<ExternallyIdentifiableRecord>();
	
	public List<ExternallyIdentifiableRecord> getRecordsThatAreNotMerged() {
		return recordsThatAreNotMerged;
	}

    public void setRecordsThatAreNotMerged(
			List<ExternallyIdentifiableRecord> recordsThatAreNotMerged) {
		this.recordsThatAreNotMerged = recordsThatAreNotMerged;
	}
	public List<ExternallyIdentifiableRecord> getRecordsThatAreDeterministicallyMerged() {
		return recordsThatAreDeterministicallyMerged;
	}
	public void setRecordsThatAreDeterministicallyMerged(
			List<ExternallyIdentifiableRecord> recordsThatAreDeterministicallyMerged) {
		this.recordsThatAreDeterministicallyMerged = recordsThatAreDeterministicallyMerged;
	}
	

}
