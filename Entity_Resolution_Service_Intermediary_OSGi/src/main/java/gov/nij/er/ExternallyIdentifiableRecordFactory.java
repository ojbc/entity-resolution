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
package gov.nij.er;

import gov.nij.bundles.intermediaries.ers.osgi.ExternallyIdentifiableRecord;

import java.util.Map;


import serf.data.Attribute;
import serf.data.Record;
import serf.data.RecordFactory;

/**
 * An implementation of the SERF record factory interface that creates externally identifiable records.
 *
 */
public class ExternallyIdentifiableRecordFactory implements RecordFactory {

    /**
     * Create a record from the two specified records
     */
    @Override
	public Record create(double confidence, Map<String, Attribute> attrs,
			Record r1, Record r2) {
		ExternallyIdentifiableRecord eir1 = (ExternallyIdentifiableRecord) r1;
		ExternallyIdentifiableRecord eir2 = (ExternallyIdentifiableRecord) r2;
		ExternallyIdentifiableRecord ret = new ExternallyIdentifiableRecord(attrs, eir1.getExternalId());
		linkUpRecords(eir1, eir2, ret);
		return ret;
	}

	private void linkUpRecords(ExternallyIdentifiableRecord eir1, ExternallyIdentifiableRecord eir2,
			ExternallyIdentifiableRecord ret)
	{
		// we have to link them all up, because we don't necessarily know the order of processing
		ret.relateRecord(eir2);
		eir2.relateRecord(ret);
		eir1.relateRecord(ret);
		ret.relateRecord(eir1);
		eir1.relateRecord(eir2);
	}

}
