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

import static org.junit.Assert.*;

import gov.nij.bundles.intermediaries.ers.osgi.AttributeParameters;
import gov.nij.bundles.intermediaries.ers.osgi.EntityResolutionConversionUtils;
import gov.nij.bundles.intermediaries.ers.osgi.EntityResolutionResults;
import gov.nij.bundles.intermediaries.ers.osgi.EntityResolutionService;
import gov.nij.bundles.intermediaries.ers.osgi.ExternallyIdentifiableRecord;
import gov.nij.bundles.intermediaries.ers.osgi.SortOrderSpecification;
import gov.nij.er.StringDistanceScoreMatcher;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import serf.data.Attribute;

public class ServiceTest
{

	private static final String JARO_DISTANCE_IMPL = "com.wcohen.ss.Jaro";

	private EntityResolutionService service;
	private double andrewThresholdValue;

	@Before
	public void setUp() throws Exception
	{
		service = new EntityResolutionService();
		StringDistanceScoreMatcher jaroMatcher = new StringDistanceScoreMatcher(JARO_DISTANCE_IMPL);
		jaroMatcher.init(0.0);
		andrewThresholdValue = Math.min(jaroMatcher.score("Andrew", "Andruw"),
				Math.min(jaroMatcher.score("Andrew", "Andriw"), jaroMatcher.score("Andruw", "Andriw")));
	}
	
	@Test
	public void testRecordLimit() throws Exception {
	    
	    List<ExternallyIdentifiableRecord> records = new ArrayList<ExternallyIdentifiableRecord>();

        Attribute a1 = new Attribute("givenName", "Andrew");
        Attribute a2 = new Attribute("surName", "Owen");
        ExternallyIdentifiableRecord r1 = new ExternallyIdentifiableRecord(makeAttributes(a1, a2), "record1");

        a1 = new Attribute("givenName", "Andruw"); // as in the Braves' outfielder
        ExternallyIdentifiableRecord r2 = new ExternallyIdentifiableRecord(makeAttributes(a1, a2), "record2");

        records.add(r1);
        records.add(r2);

        Set<AttributeParameters> attributeParametersSet = new HashSet<AttributeParameters>();
        AttributeParameters ap = new AttributeParameters("givenName");
        ap.setAlgorithmClassName(JARO_DISTANCE_IMPL);
        ap.setThreshold(andrewThresholdValue - .01);
        attributeParametersSet.add(ap);
        ap = new AttributeParameters("surName");
        ap.setAlgorithmClassName(JARO_DISTANCE_IMPL);
        ap.setThreshold(andrewThresholdValue - .01);
        attributeParametersSet.add(ap);

        EntityResolutionResults results = service.resolveEntities(EntityResolutionConversionUtils.convertRecords(records), attributeParametersSet);
        List<ExternallyIdentifiableRecord> returnRecords = EntityResolutionConversionUtils.convertRecordWrappers(results.getRecords());
        assertEquals(1, returnRecords.size());
        assertFalse(results.isRecordLimitExceeded());

        results = service.resolveEntities(EntityResolutionConversionUtils.convertRecords(records), attributeParametersSet, 2);
        returnRecords = EntityResolutionConversionUtils.convertRecordWrappers(results.getRecords());
        assertEquals(1, returnRecords.size());
        assertFalse(results.isRecordLimitExceeded());
        
        results = service.resolveEntities(EntityResolutionConversionUtils.convertRecords(records), attributeParametersSet, 1);
        returnRecords = EntityResolutionConversionUtils.convertRecordWrappers(results.getRecords());
        assertEquals(2, returnRecords.size());
        assertTrue(results.isRecordLimitExceeded());
        
	}
	
	@Test
	public void testDeterminativeNonMerge() throws Exception
	{

		List<ExternallyIdentifiableRecord> records = new ArrayList<ExternallyIdentifiableRecord>();

		Attribute a1 = new Attribute("givenName", "Andrew");
		Attribute a2 = new Attribute("sid", "123");
		ExternallyIdentifiableRecord r1 = new ExternallyIdentifiableRecord(makeAttributes(a1, a2), "record1");

		a2 = new Attribute("sid", "123");
		ExternallyIdentifiableRecord r2 = new ExternallyIdentifiableRecord(makeAttributes(a1, a2), "record2");

		a2 = new Attribute("sid", "124");
		ExternallyIdentifiableRecord r3 = new ExternallyIdentifiableRecord(makeAttributes(a1, a2), "record3");

		records.add(r1);
		records.add(r2);
		records.add(r3);
		
		Set<AttributeParameters> attributeParametersSet = new HashSet<AttributeParameters>();
		AttributeParameters ap = new AttributeParameters("givenName");
		ap.setAlgorithmClassName(JARO_DISTANCE_IMPL);
		ap.setThreshold(0.7);
		attributeParametersSet.add(ap);
		ap = new AttributeParameters("sid");
		ap.setAlgorithmClassName(JARO_DISTANCE_IMPL);
		ap.setThreshold(0.7);
		attributeParametersSet.add(ap);

		EntityResolutionResults results = service.resolveEntities(EntityResolutionConversionUtils.convertRecords(records), attributeParametersSet);
		List<ExternallyIdentifiableRecord> returnRecords = EntityResolutionConversionUtils.convertRecordWrappers(results.getRecords());
		assertEquals(1, returnRecords.size()); // verifies that if the sid is not determinative, we'd get 1 resultant record
		
		attributeParametersSet.remove(ap);
		
		ap = new AttributeParameters("sid");
		ap.setAlgorithmClassName(JARO_DISTANCE_IMPL);
		ap.setThreshold(0.7);
		ap.setDeterminative(true);
		attributeParametersSet.add(ap);
		results = service.resolveEntities(EntityResolutionConversionUtils.convertRecords(records), attributeParametersSet);
		returnRecords = EntityResolutionConversionUtils.convertRecordWrappers(results.getRecords());
		assertEquals(2, returnRecords.size());
		
	}

	
	@Test
	public void testEmptyRecordInput() throws Exception
	{
		List<ExternallyIdentifiableRecord> records = new ArrayList<ExternallyIdentifiableRecord>();
		Set<AttributeParameters> attributeParametersSet = new HashSet<AttributeParameters>();
		AttributeParameters ap = new AttributeParameters("givenName");
		ap.setAlgorithmClassName(JARO_DISTANCE_IMPL);
		ap.setThreshold(0.7);
		attributeParametersSet.add(ap);
		EntityResolutionResults results = service.resolveEntities(EntityResolutionConversionUtils.convertRecords(records), attributeParametersSet);
		List<ExternallyIdentifiableRecord> returnRecords = EntityResolutionConversionUtils.convertRecordWrappers(results.getRecords());
		assertEquals(0, returnRecords.size());
	}
	
	@Test
	public void testDeterminativeMerge() throws Exception
	{

		List<ExternallyIdentifiableRecord> records = new ArrayList<ExternallyIdentifiableRecord>();

		Attribute a1 = new Attribute("givenName", "Andrew");
		Attribute a2 = new Attribute("sid", "123");
		ExternallyIdentifiableRecord r1 = new ExternallyIdentifiableRecord(makeAttributes(a1, a2), "record1");

		a1 = new Attribute("givenName", "Gerry");
		ExternallyIdentifiableRecord r2 = new ExternallyIdentifiableRecord(makeAttributes(a1, a2), "record2");

		records.add(r1);
		records.add(r2);
		
		Set<AttributeParameters> attributeParametersSet = new HashSet<AttributeParameters>();
		AttributeParameters ap = new AttributeParameters("givenName");
		ap.setAlgorithmClassName(JARO_DISTANCE_IMPL);
		ap.setThreshold(0.7);
		attributeParametersSet.add(ap);
		ap = new AttributeParameters("sid");
		ap.setAlgorithmClassName(JARO_DISTANCE_IMPL);
		ap.setThreshold(0.7);
		attributeParametersSet.add(ap);

		EntityResolutionResults results = service.resolveEntities(EntityResolutionConversionUtils.convertRecords(records), attributeParametersSet);
		List<ExternallyIdentifiableRecord> returnRecords = EntityResolutionConversionUtils.convertRecordWrappers(results.getRecords());
		assertEquals(2, returnRecords.size()); // verifies that if the sid is not determinative, we'd get 2 resultant records
		
		attributeParametersSet.remove(ap);
		
		ap = new AttributeParameters("sid");
		ap.setAlgorithmClassName(JARO_DISTANCE_IMPL);
		ap.setThreshold(0.7);
		ap.setDeterminative(true);
		attributeParametersSet.add(ap);
		results = service.resolveEntities(EntityResolutionConversionUtils.convertRecords(records), attributeParametersSet);
		returnRecords = EntityResolutionConversionUtils.convertRecordWrappers(results.getRecords());
		assertEquals(1, returnRecords.size());
		
	}
	
	@Test
	public void test2DeterminativeAttributesNonMerge() throws Exception
	{

		List<ExternallyIdentifiableRecord> records = new ArrayList<ExternallyIdentifiableRecord>();

		Attribute a1 = new Attribute("givenName", "Andrew");
		Attribute a2 = new Attribute("sid", "123");
		Attribute a3 = new Attribute("ssn", "123456789");
		ExternallyIdentifiableRecord r1 = new ExternallyIdentifiableRecord(makeAttributes(a1, a2, a3), "record1");

		a3 = new Attribute("ssn", "987654321");
		ExternallyIdentifiableRecord r2 = new ExternallyIdentifiableRecord(makeAttributes(a1, a2, a3), "record2");

		records.add(r1);
		records.add(r2);
		
		Set<AttributeParameters> attributeParametersSet = new HashSet<AttributeParameters>();
		AttributeParameters ap = new AttributeParameters("givenName");
		ap.setAlgorithmClassName(JARO_DISTANCE_IMPL);
		ap.setThreshold(0.7);
		attributeParametersSet.add(ap);
		ap = new AttributeParameters("sid");
		ap.setAlgorithmClassName(JARO_DISTANCE_IMPL);
		ap.setThreshold(0.7);
		ap.setDeterminative(true);
		attributeParametersSet.add(ap);
		ap = new AttributeParameters("ssn");
		ap.setAlgorithmClassName(JARO_DISTANCE_IMPL);
		ap.setThreshold(0.7);
		ap.setDeterminative(true);
		attributeParametersSet.add(ap);

		EntityResolutionResults results = service.resolveEntities(EntityResolutionConversionUtils.convertRecords(records), attributeParametersSet);
		List<ExternallyIdentifiableRecord> returnRecords = EntityResolutionConversionUtils.convertRecordWrappers(results.getRecords());
		assertEquals(2, returnRecords.size());
		
	}
	
	@Test
	public void test2DeterminativeAttributesMerge() throws Exception
	{

		List<ExternallyIdentifiableRecord> records = new ArrayList<ExternallyIdentifiableRecord>();

		Attribute a1 = new Attribute("givenName", "Andrew");
		Attribute a2 = new Attribute("sid", "123");
		Attribute a3 = new Attribute("ssn", "123456789");
		ExternallyIdentifiableRecord r1 = new ExternallyIdentifiableRecord(makeAttributes(a1, a2, a3), "record1");
		a1 = new Attribute("givenName", "Gerry");
		ExternallyIdentifiableRecord r2 = new ExternallyIdentifiableRecord(makeAttributes(a1, a2, a3), "record2");

		records.add(r1);
		records.add(r2);
		
		Set<AttributeParameters> attributeParametersSet = new HashSet<AttributeParameters>();
		AttributeParameters ap = new AttributeParameters("givenName");
		ap.setAlgorithmClassName(JARO_DISTANCE_IMPL);
		ap.setThreshold(andrewThresholdValue);
		attributeParametersSet.add(ap);
		ap = new AttributeParameters("sid");
		ap.setAlgorithmClassName(JARO_DISTANCE_IMPL);
		ap.setThreshold(0.7);
		ap.setDeterminative(true);
		attributeParametersSet.add(ap);
		ap = new AttributeParameters("ssn");
		ap.setAlgorithmClassName(JARO_DISTANCE_IMPL);
		ap.setThreshold(0.7);
		ap.setDeterminative(true);
		attributeParametersSet.add(ap);

		EntityResolutionResults results = service.resolveEntities(EntityResolutionConversionUtils.convertRecords(records), attributeParametersSet);
		List<ExternallyIdentifiableRecord> returnRecords = EntityResolutionConversionUtils.convertRecordWrappers(results.getRecords());
		assertEquals(1, returnRecords.size());
		
	}

	@Test
	public void testBasicMerge() throws Exception
	{

		List<ExternallyIdentifiableRecord> records = new ArrayList<ExternallyIdentifiableRecord>();

		Attribute a1 = new Attribute("givenName", "Andrew");
		Attribute a2 = new Attribute("surName", "Owen");
		ExternallyIdentifiableRecord r1 = new ExternallyIdentifiableRecord(makeAttributes(a1, a2), "record1");

		a1 = new Attribute("givenName", "Andruw"); // as in the Braves' outfielder
		ExternallyIdentifiableRecord r2 = new ExternallyIdentifiableRecord(makeAttributes(a1, a2), "record2");

		records.add(r1);
		records.add(r2);

		Set<AttributeParameters> attributeParametersSet = new HashSet<AttributeParameters>();
		AttributeParameters ap = new AttributeParameters("givenName");
		ap.setAlgorithmClassName(JARO_DISTANCE_IMPL);
		ap.setThreshold(andrewThresholdValue - .01);
		attributeParametersSet.add(ap);
		ap = new AttributeParameters("surName");
		ap.setAlgorithmClassName(JARO_DISTANCE_IMPL);
		ap.setThreshold(andrewThresholdValue - .01);
		attributeParametersSet.add(ap);

		EntityResolutionResults results = service.resolveEntities(EntityResolutionConversionUtils.convertRecords(records), attributeParametersSet);
		List<ExternallyIdentifiableRecord> returnRecords = EntityResolutionConversionUtils.convertRecordWrappers(results.getRecords());
		assertEquals(1, returnRecords.size());

		ExternallyIdentifiableRecord record = returnRecords.iterator().next();
		Attribute attr = record.getAttribute("surName");
		assertNotNull(attr);
		Iterator<String> it = attr.iterator();
		String value = it.next();
		assert (!it.hasNext());
		assertEquals("Owen", value);

		attr = record.getAttribute("givenName");
		assertNotNull(attr);
		List<String> values = new ArrayList<String>();
		for (it = attr.iterator(); it.hasNext();)
		{
			values.add(it.next());
		}
		assertEquals(2, values.size());
		assert (values.contains("Andrew"));
		assert (values.contains("Andruw"));

		Set<String> relatedIds = record.getRelatedIds();
		assertEquals(1, relatedIds.size());

	}

	@Test
	public void test3WayMerge() throws Exception
	{

		List<ExternallyIdentifiableRecord> records = new ArrayList<ExternallyIdentifiableRecord>();

		Attribute a1 = new Attribute("givenName", "Andrew");
		Attribute a2 = new Attribute("surName", "Owen");
		ExternallyIdentifiableRecord r1 = new ExternallyIdentifiableRecord(makeAttributes(a1, a2), "record1");

		a1 = new Attribute("givenName", "Andruw"); // as in the Braves' outfielder
		ExternallyIdentifiableRecord r2 = new ExternallyIdentifiableRecord(makeAttributes(a1, a2), "record2");

		a1 = new Attribute("givenName", "Andriw"); // who knows, maybe somebody has this name...
		ExternallyIdentifiableRecord r3 = new ExternallyIdentifiableRecord(makeAttributes(a1, a2), "record3");

		records.add(r1);
		records.add(r2);
		records.add(r3);

		Set<AttributeParameters> attributeParametersSet = new HashSet<AttributeParameters>();
		AttributeParameters ap = new AttributeParameters("givenName");
		ap.setAlgorithmClassName(JARO_DISTANCE_IMPL);
		ap.setThreshold(andrewThresholdValue - .01);
		attributeParametersSet.add(ap);
		ap = new AttributeParameters("surName");
		ap.setAlgorithmClassName(JARO_DISTANCE_IMPL);
		ap.setThreshold(andrewThresholdValue - .01);
		attributeParametersSet.add(ap);

		EntityResolutionResults results = service.resolveEntities(EntityResolutionConversionUtils.convertRecords(records), attributeParametersSet);
		List<ExternallyIdentifiableRecord> returnRecords = EntityResolutionConversionUtils.convertRecordWrappers(results.getRecords());
		assertEquals(1, returnRecords.size());

		ExternallyIdentifiableRecord record = returnRecords.iterator().next();
		Attribute attr = record.getAttribute("surName");
		assertNotNull(attr);
		Iterator<String> it = attr.iterator();
		String value = it.next();
		assert (!it.hasNext());
		assertEquals("Owen", value);

		attr = record.getAttribute("givenName");
		assertNotNull(attr);
		List<String> values = new ArrayList<String>();
		for (it = attr.iterator(); it.hasNext();)
		{
			values.add(it.next());
		}
		assertEquals(3, values.size());
		assert (values.contains("Andrew"));
		assert (values.contains("Andruw"));
		assert (values.contains("Andriw"));

		Set<String> relatedIds = record.getRelatedIds();
		assertEquals(2, relatedIds.size());

	}

	@Test
	public void test3WayNonMerge() throws Exception
	{

		List<ExternallyIdentifiableRecord> records = new ArrayList<ExternallyIdentifiableRecord>();

		Attribute a1 = new Attribute("givenName", "Andrew");
		Attribute a2 = new Attribute("surName", "Owen");
		ExternallyIdentifiableRecord r1 = new ExternallyIdentifiableRecord(makeAttributes(a1, a2), "record1");

		a1 = new Attribute("givenName", "Andruw"); // as in the Braves' outfielder
		ExternallyIdentifiableRecord r2 = new ExternallyIdentifiableRecord(makeAttributes(a1, a2), "record2");

		a1 = new Attribute("givenName", "Androo"); // who knows, maybe somebody has this name...
		ExternallyIdentifiableRecord r3 = new ExternallyIdentifiableRecord(makeAttributes(a1, a2), "record3");

		records.add(r1);
		records.add(r2);
		records.add(r3);

		Set<AttributeParameters> attributeParametersSet = new HashSet<AttributeParameters>();
		AttributeParameters ap = new AttributeParameters("givenName");
		ap.setAlgorithmClassName(JARO_DISTANCE_IMPL);
		ap.setThreshold(andrewThresholdValue - .01);
		attributeParametersSet.add(ap);
		ap = new AttributeParameters("surName");
		ap.setAlgorithmClassName(JARO_DISTANCE_IMPL);
		ap.setThreshold(andrewThresholdValue - .01);
		attributeParametersSet.add(ap);

		EntityResolutionResults results = service.resolveEntities(EntityResolutionConversionUtils.convertRecords(records), attributeParametersSet);
		List<ExternallyIdentifiableRecord> returnRecords = EntityResolutionConversionUtils.convertRecordWrappers(results.getRecords());
		assertEquals(2, returnRecords.size());

		boolean andrewFound = false;
		boolean andruwFound = false;
		boolean androoFound = false;
		
		ExternallyIdentifiableRecord andrewAndruwRecord = null;
		ExternallyIdentifiableRecord androoRecord = null;

		for (ExternallyIdentifiableRecord record : returnRecords)
		{
			
			boolean recAndrewFound = false;
			boolean recAndruwFound = false;
			boolean recAndrooFound = false;

			Attribute attr = record.getAttribute("surName");
			Iterator<String> it = attr.iterator();
			assertEquals("Owen", it.next());
			assert (!it.hasNext());
			attr = record.getAttribute("givenName");
			for (it = attr.iterator(); it.hasNext();)
			{
				String next = it.next();
				if ("Andrew".equals(next))
				{
					andrewFound = true;
					recAndrewFound = true;
					andrewAndruwRecord = record;
				} else if ("Andruw".equals(next))
				{
					andruwFound = true;
					recAndruwFound = true;
				} else if ("Androo".equals(next))
				{
					androoFound = true;
					recAndrooFound = true;
					androoRecord = record;
				}
			}
			assert(
					(recAndrooFound && !(recAndrewFound || recAndruwFound)) ||
					((recAndrewFound && recAndruwFound) && !recAndrooFound));
		}

		assert (andrewFound);
		assert (andruwFound);
		assert (androoFound);
		
		assertEquals(0, androoRecord.getRelatedIds().size());
		Set<String> andrewAndruwRecordRelatedIds = andrewAndruwRecord.getRelatedIds();
		assertEquals(1, andrewAndruwRecordRelatedIds.size());
		if (andrewAndruwRecord.getExternalId().equals("record1"))
		{
			assert(andrewAndruwRecordRelatedIds.contains("record2"));
		}
		else
		{
			assert(andrewAndruwRecordRelatedIds.contains("record1"));
		}

	}

	@Test
	public void testBasicNonMerge() throws Exception
	{

		List<ExternallyIdentifiableRecord> records = new ArrayList<ExternallyIdentifiableRecord>();

		Attribute a1 = new Attribute("givenName", "Andrew");
		Attribute a2 = new Attribute("surName", "Owen");
		ExternallyIdentifiableRecord r1 = new ExternallyIdentifiableRecord(makeAttributes(a1, a2), "record1");

		a1 = new Attribute("givenName", "Andruw"); // as in the Braves' outfielder
		ExternallyIdentifiableRecord r2 = new ExternallyIdentifiableRecord(makeAttributes(a1, a2), "record2");

		records.add(r1);
		records.add(r2);

		Set<AttributeParameters> attributeParametersSet = new HashSet<AttributeParameters>();
		AttributeParameters ap = new AttributeParameters("givenName");
		ap.setAlgorithmClassName(JARO_DISTANCE_IMPL);
		ap.setThreshold(andrewThresholdValue + .01);
		attributeParametersSet.add(ap);
		ap = new AttributeParameters("surName");
		ap.setAlgorithmClassName(JARO_DISTANCE_IMPL);
		ap.setThreshold(andrewThresholdValue + .01);
		attributeParametersSet.add(ap);

		EntityResolutionResults results = service.resolveEntities(EntityResolutionConversionUtils.convertRecords(records), attributeParametersSet);
		List<ExternallyIdentifiableRecord> returnRecords = EntityResolutionConversionUtils.convertRecordWrappers(results.getRecords());
		assertEquals(2, returnRecords.size());

		boolean andrewFound = false;
		boolean andruwFound = false;

		for (ExternallyIdentifiableRecord record : returnRecords)
		{
			Attribute attr = record.getAttribute("surName");
			Iterator<String> it = attr.iterator();
			assertEquals("Owen", it.next());
			assert (!it.hasNext());
			attr = record.getAttribute("givenName");
			it = attr.iterator();
			String next = it.next();
			if ("Andrew".equals(next))
			{
				andrewFound = true;
			} else if ("Andruw".equals(next))
			{
				andruwFound = true;
			}
			assert (!it.hasNext());
		}

		assert (andrewFound);
		assert (andruwFound);

	}
	
	@Test
	public void testSorting() throws Exception {
	    
	    List<ExternallyIdentifiableRecord> records = new ArrayList<ExternallyIdentifiableRecord>();

        Attribute a1 = new Attribute("givenName", "Andruw");
        Attribute a2 = new Attribute("surName", "Owen");
        ExternallyIdentifiableRecord r1 = new ExternallyIdentifiableRecord(makeAttributes(a1, a2), "record1");

        a1 = new Attribute("givenName", "Andrew");
        ExternallyIdentifiableRecord r2 = new ExternallyIdentifiableRecord(makeAttributes(a1, a2), "record2");

        records.add(r1);
        records.add(r2);

        Set<AttributeParameters> attributeParametersSet = new HashSet<AttributeParameters>();
        AttributeParameters ap = new AttributeParameters("givenName");
        ap.setAlgorithmClassName(JARO_DISTANCE_IMPL);
        ap.setThreshold(andrewThresholdValue + .01);
        attributeParametersSet.add(ap);
        ap = new AttributeParameters("surName");
        ap.setAlgorithmClassName(JARO_DISTANCE_IMPL);
        ap.setThreshold(andrewThresholdValue + .01);
        attributeParametersSet.add(ap);

        EntityResolutionResults results = service.resolveEntities(EntityResolutionConversionUtils.convertRecords(records), attributeParametersSet);
        List<ExternallyIdentifiableRecord> returnRecords = EntityResolutionConversionUtils.convertRecordWrappers(results.getRecords());
        assertEquals(2, returnRecords.size());
        
        r1 = returnRecords.get(0);
        assertEquals("Andruw", r1.getAttribute("givenName").iterator().next()); // not sorted
        
        attributeParametersSet = new HashSet<AttributeParameters>();
        ap = new AttributeParameters("givenName");
        ap.setAlgorithmClassName(JARO_DISTANCE_IMPL);
        ap.setThreshold(andrewThresholdValue + .01);
        ap.setSortOrder(new SortOrderSpecification(1, SortOrderSpecification.SORT_ORDER_ASCENDING));
        attributeParametersSet.add(ap);
        ap = new AttributeParameters("surName");
        ap.setAlgorithmClassName(JARO_DISTANCE_IMPL);
        ap.setThreshold(andrewThresholdValue + .01);
        attributeParametersSet.add(ap);
        
        results = service.resolveEntities(EntityResolutionConversionUtils.convertRecords(records), attributeParametersSet);
        returnRecords = EntityResolutionConversionUtils.convertRecordWrappers(results.getRecords());
        assertEquals(2, returnRecords.size());
        
        r1 = returnRecords.get(0);
        assertEquals("Andrew", r1.getAttribute("givenName").iterator().next()); // sorted
        
        attributeParametersSet = new HashSet<AttributeParameters>();
        ap = new AttributeParameters("givenName");
        ap.setAlgorithmClassName(JARO_DISTANCE_IMPL);
        ap.setThreshold(andrewThresholdValue + .01);
        ap.setSortOrder(new SortOrderSpecification(1, SortOrderSpecification.SORT_ORDER_DESCENDING));
        attributeParametersSet.add(ap);
        ap = new AttributeParameters("surName");
        ap.setAlgorithmClassName(JARO_DISTANCE_IMPL);
        ap.setThreshold(andrewThresholdValue + .01);
        attributeParametersSet.add(ap);
        
        results = service.resolveEntities(EntityResolutionConversionUtils.convertRecords(records), attributeParametersSet);
        returnRecords = EntityResolutionConversionUtils.convertRecordWrappers(results.getRecords());
        assertEquals(2, returnRecords.size());
        
        r1 = returnRecords.get(0);
        assertEquals("Andruw", r1.getAttribute("givenName").iterator().next()); // sorted descending
        
        a1 = new Attribute("givenName", "Andruw");
        a2 = new Attribute("surName", "Jones");
        r1 = new ExternallyIdentifiableRecord(makeAttributes(a1, a2), "record1");
        
        records = new ArrayList<ExternallyIdentifiableRecord>();
        records.add(r1);
        records.add(r2);
        
        attributeParametersSet = new HashSet<AttributeParameters>();
        ap = new AttributeParameters("givenName");
        ap.setAlgorithmClassName(JARO_DISTANCE_IMPL);
        ap.setThreshold(andrewThresholdValue + .01);
        ap.setSortOrder(new SortOrderSpecification(2, SortOrderSpecification.SORT_ORDER_ASCENDING));
        attributeParametersSet.add(ap);
        ap = new AttributeParameters("surName");
        ap.setAlgorithmClassName(JARO_DISTANCE_IMPL);
        ap.setThreshold(andrewThresholdValue + .01);
        ap.setSortOrder(new SortOrderSpecification(1, SortOrderSpecification.SORT_ORDER_ASCENDING));
        attributeParametersSet.add(ap);
        
        results = service.resolveEntities(EntityResolutionConversionUtils.convertRecords(records), attributeParametersSet);
        returnRecords = EntityResolutionConversionUtils.convertRecordWrappers(results.getRecords());
        assertEquals(2, returnRecords.size());
        
        r1 = returnRecords.get(0);
        // sorted ascending, but by last name first (Jones comes before Owen)
        assertEquals("Andruw", r1.getAttribute("givenName").iterator().next());
        assertEquals("Jones", r1.getAttribute("surName").iterator().next());
        
	    
	}

	private static Map<String, Attribute> makeAttributes(Attribute... attributes)
	{
		Map<String, Attribute> ret = new HashMap<String, Attribute>();
		for (Attribute a : attributes)
		{
			ret.put(a.getType(), a);
		}
		return ret;
	}

}
