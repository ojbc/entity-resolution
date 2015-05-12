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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import serf.data.Attribute;

/**
 * A utility class for managing conversion of SERF-independent objects to/from SERF objects
 *
 */
public class EntityResolutionConversionUtils
{
	
	private static final Log LOG = LogFactory.getLog( EntityResolutionConversionUtils.class );

	/**
	 * Convert a SERF-independent AttributeWrapper to an equivalent SERT Attribute
	 * @param aw the wrapper
	 * @return the attribute
	 */
	public static Attribute convertAttributeWrapper(AttributeWrapper aw)
	{
		Attribute ret = new Attribute(aw.getType());
		ret.addValues(aw.getValues());
		return ret;
	}

	/**
	 * Convert a collectio of SERF-independent AttributeWrappers to equivalent SERF Attributes
	 * @param attributes a collection of attribute wrappers - as a map keyed by the attribute name
	 * @return equvalent map of SERF attributes, keyed by attribute name
	 */
	public static Map<String, Attribute> convertAttributeWrappers(Map<String, AttributeWrapper> attributes)
	{
		Map<String, Attribute> ret = new HashMap<String, Attribute>();
		for (String key : attributes.keySet())
		{
			AttributeWrapper aw = attributes.get(key);
			LOG.debug("In convertAttributeWrappers, key=" + key + ", aw=" + aw);
			ret.put(key, convertAttributeWrapper(aw));
		}
		return ret;
	}

	/**
	 * Convert a list of SERF-independent record wrappers to an equivalent list of SERF records
	 * @param recordWrappers a list of record wrappers
	 * @return equivalent list of records
	 */
	public static List<ExternallyIdentifiableRecord> convertRecordWrappers(List<RecordWrapper> recordWrappers)
	{
		List<ExternallyIdentifiableRecord> records = new ArrayList<ExternallyIdentifiableRecord>();
		for (RecordWrapper rw : recordWrappers)
		{
			ExternallyIdentifiableRecord record = new ExternallyIdentifiableRecord(
					convertAttributeWrappers(rw.getAttributes()), rw.getExternalId());
			record.setRelatedIds(rw.getRelatedIds());
			LOG.debug("In convertRecordWrappers, record.hashCode()=" + record.hashCode());
			records.add(record);
		}
		return records;
	}

	/**
	 * Convert a single SERF attribute to an equivalent SERF-independent attribute wrapper
	 * @param a the SERF attribute
	 * @return the wrapper
	 */
	public static AttributeWrapper convertAttribute(Attribute a)
	{
		AttributeWrapper ret = new AttributeWrapper(a.getType());
		for (String value : a)
		{
			ret.addValue(value);
		}
		return ret;
	}

	/**
	 * Convert a collection of SERF attributes to an equivalent collection of SERF-independent attribute wrappers
	 * @param attributes the attributes
	 * @return the equivalent wrappers
	 */
	public static Map<String, AttributeWrapper> convertAttributes(Map<String, Attribute> attributes)
	{
		Map<String, AttributeWrapper> ret = new HashMap<String, AttributeWrapper>();
		for (String key : attributes.keySet())
		{
			Attribute a = attributes.get(key);
			ret.put(key, convertAttribute(a));
		}
		return ret;
	}

	/**
	 * Convert a list of SERF records to an equivalent list of SERF-independent record wrappers
	 * @param records the SERF records
	 * @return the equivalent record wrappers
	 */
	public static List<RecordWrapper> convertRecords(List<ExternallyIdentifiableRecord> records)
	{
		LOG.debug(" !#!#! In convertRecords, in=" + records);
		List<RecordWrapper> ret = new ArrayList<RecordWrapper>();
		for (ExternallyIdentifiableRecord record : records)
		{
			RecordWrapper rw = new RecordWrapper(convertAttributes(record.getAttributes()), record.getExternalId());
			rw.setRelatedIds(record.getRelatedIds());
			ret.add(rw);
		}
		LOG.debug(" !#!#! In convertRecords, out=" + ret);
		return ret;
	}

}
