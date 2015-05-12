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

import uk.ac.shef.wit.simmetrics.similaritymetrics.InterfaceStringMetric;

import com.wcohen.ss.AbstractStringDistance;
import com.wcohen.ss.api.StringWrapper;

/**
 * This class is a bridge between the simmetrics and secondstring toolkits, allowing simmetrics algorithms to be
 * used in place of their secondstring equivalents (e.g., in SERF).
 *
 */
public class StringMetricStringDistance extends AbstractStringDistance {
	
	private InterfaceStringMetric stringMetric;

	/**
	 * Create a bridge metric object wrapping the specified metric implementation.
	 * @param stringMetric
	 */
	public StringMetricStringDistance(InterfaceStringMetric stringMetric)
	{
		this.stringMetric = stringMetric;
	}

	/**
	 * Compute the distance between the two specified strings
	 */
	@Override
	public double score(StringWrapper s, StringWrapper t) {
		return stringMetric.getSimilarity(s == null ? null : s.unwrap(), t == null ? null : t.unwrap());
	}

	/**
	 * Create a string with explanatory text about how the two specified strings would have their distance computed
	 */
	@Override
	public String explainScore(StringWrapper s, StringWrapper t) {
		return stringMetric.getSimilarityExplained(s == null ? null : s.unwrap(), t == null ? null : t.unwrap());
	}

}
