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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Before;
import org.junit.Test;

import serf.data.Attribute;

/**
 * This test class will test the Deterministic Factors algorithm. It will test the DF functions in isolation and then as part of a larger ER service call.
 * 
 */
public class EntityResolutionServiceDeterministicFactorsTest {

    private static final Log LOG = LogFactory.getLog(EntityResolutionServiceDeterministicFactorsTest.class);

    private static final String JARO_DISTANCE_IMPL = "com.wcohen.ss.Jaro";

    private EntityResolutionService service;

    private Set<AttributeParameters> simpleAttributeParameterSet;
    private Set<AttributeParameters> realisticAttributeParameterSet;
    private Set<AttributeParameters> oldTestsAttributeParameterSet;
    private Set<AttributeParameters> onlyDeterministicAttributeParameterSet;

    @Before
    public void setUp() throws Exception {

        service = new EntityResolutionService();
        
        simpleAttributeParameterSet = new HashSet<AttributeParameters>();

        AttributeParameters ap = new AttributeParameters("A1");
        ap.setAlgorithmClassName(JARO_DISTANCE_IMPL);
        ap.setDeterminative(true);
        simpleAttributeParameterSet.add(ap);
        
        ap = new AttributeParameters("A2");
        ap.setAlgorithmClassName(JARO_DISTANCE_IMPL);
        ap.setDeterminative(true);
        simpleAttributeParameterSet.add(ap);

        ap = new AttributeParameters("A3");
        ap.setAlgorithmClassName(JARO_DISTANCE_IMPL);
        ap.setThreshold(0.8);
        simpleAttributeParameterSet.add(ap);

        onlyDeterministicAttributeParameterSet = new HashSet<AttributeParameters>();

        ap = new AttributeParameters("A1");
        ap.setAlgorithmClassName(JARO_DISTANCE_IMPL);
        ap.setDeterminative(true);
        onlyDeterministicAttributeParameterSet.add(ap);
        
        ap = new AttributeParameters("A2");
        ap.setAlgorithmClassName(JARO_DISTANCE_IMPL);
        ap.setDeterminative(true);
        onlyDeterministicAttributeParameterSet.add(ap);

        realisticAttributeParameterSet = new HashSet<AttributeParameters>();

        ap = new AttributeParameters("SID");
        ap.setAlgorithmClassName(JARO_DISTANCE_IMPL);
        ap.setDeterminative(true);
        realisticAttributeParameterSet.add(ap);
        
        ap = new AttributeParameters("FBI");
        ap.setAlgorithmClassName(JARO_DISTANCE_IMPL);
        ap.setDeterminative(true);
        realisticAttributeParameterSet.add(ap);

        ap = new AttributeParameters("LastName");
        ap.setAlgorithmClassName(JARO_DISTANCE_IMPL);
        ap.setThreshold(0.8);
        realisticAttributeParameterSet.add(ap);

        ap = new AttributeParameters("FirstName");
        ap.setAlgorithmClassName(JARO_DISTANCE_IMPL);
        ap.setThreshold(0.8);
        realisticAttributeParameterSet.add(ap);

        ap = new AttributeParameters("DOB");
        ap.setAlgorithmClassName(JARO_DISTANCE_IMPL);
        ap.setThreshold(0.8);
        realisticAttributeParameterSet.add(ap);

        oldTestsAttributeParameterSet = new HashSet<AttributeParameters>();
        ap = new AttributeParameters("givenName");
        ap.setAlgorithmClassName(JARO_DISTANCE_IMPL);
        ap.setThreshold(0.8);
        oldTestsAttributeParameterSet.add(ap);

        ap = new AttributeParameters("sid");
        ap.setAlgorithmClassName(JARO_DISTANCE_IMPL);
        ap.setDeterminative(true);
        oldTestsAttributeParameterSet.add(ap);

        ap = new AttributeParameters("ssn");
        ap.setAlgorithmClassName(JARO_DISTANCE_IMPL);
        ap.setDeterminative(true);
        oldTestsAttributeParameterSet.add(ap);
    }
    
    @Test
    public void testMismatchedDeterministicAttributesScenario() throws Exception {
        
        List<ExternallyIdentifiableRecord> records = new ArrayList<ExternallyIdentifiableRecord>();
        records.add(makeNewScenariosRecord(null, "Y", "Z", "record1"));
        records.add(makeNewScenariosRecord("X", null, "Z", "record2"));
        EntityResolutionResults results = service.resolveEntities(EntityResolutionConversionUtils.convertRecords(records), onlyDeterministicAttributeParameterSet);
        List<ExternallyIdentifiableRecord> returnRecords = EntityResolutionConversionUtils.convertRecordWrappers(results.getRecords());
        assertEquals(2, returnRecords.size());
    }
    
    @Test
    public void testOnlyDeterministicAttributesScenario() throws Exception {
        
        List<ExternallyIdentifiableRecord> records = new ArrayList<ExternallyIdentifiableRecord>();
        records.add(makeNewScenariosRecord(null, null, "Z", "record1"));
        records.add(makeNewScenariosRecord("X", "Y", "Z", "record2"));
        EntityResolutionResults results = service.resolveEntities(EntityResolutionConversionUtils.convertRecords(records), onlyDeterministicAttributeParameterSet);
        List<ExternallyIdentifiableRecord> returnRecords = EntityResolutionConversionUtils.convertRecordWrappers(results.getRecords());
        assertEquals(2, returnRecords.size());
        
    }
    
    @Test
    public void testScenarioM1() throws Exception {

        List<ExternallyIdentifiableRecord> records;
        EntityResolutionResults results;
        List<ExternallyIdentifiableRecord> returnRecords;

        records = new ArrayList<ExternallyIdentifiableRecord>();
        records.add(makeNewScenariosRecord("X", "Y", "Z", "record1"));
        records.add(makeNewScenariosRecord("X", "Y", "Z", "record2"));
        results = service.resolveEntities(EntityResolutionConversionUtils.convertRecords(records), simpleAttributeParameterSet);
        returnRecords = EntityResolutionConversionUtils.convertRecordWrappers(results.getRecords());
        assertEquals(1, returnRecords.size());
        
    }
    
    @Test
    public void testScenarioM2() throws Exception {

        List<ExternallyIdentifiableRecord> records;
        EntityResolutionResults results;
        List<ExternallyIdentifiableRecord> returnRecords;

        records = new ArrayList<ExternallyIdentifiableRecord>();
        records.add(makeNewScenariosRecord("X", "Y", "Z", "record1"));
        records.add(makeNewScenariosRecord("X", "Y", "Q", "record2"));
        results = service.resolveEntities(EntityResolutionConversionUtils.convertRecords(records), simpleAttributeParameterSet);
        returnRecords = EntityResolutionConversionUtils.convertRecordWrappers(results.getRecords());
        assertEquals(1, returnRecords.size());
        
    }

    @Test
    public void testScenarioM3() throws Exception {

        List<ExternallyIdentifiableRecord> records;
        EntityResolutionResults results;
        List<ExternallyIdentifiableRecord> returnRecords;

        records = new ArrayList<ExternallyIdentifiableRecord>();
        records.add(makeNewScenariosRecord("X", "Y", "Z", "record1"));
        records.add(makeNewScenariosRecord("X", "Y", null, "record2"));
        results = service.resolveEntities(EntityResolutionConversionUtils.convertRecords(records), simpleAttributeParameterSet);
        returnRecords = EntityResolutionConversionUtils.convertRecordWrappers(results.getRecords());
        assertEquals(1, returnRecords.size());
        
    }

    @Test
    public void testScenarioM4() throws Exception {

        List<ExternallyIdentifiableRecord> records;
        EntityResolutionResults results;
        List<ExternallyIdentifiableRecord> returnRecords;

        records = new ArrayList<ExternallyIdentifiableRecord>();
        records.add(makeNewScenariosRecord(null, null, "Z", "record1"));
        records.add(makeNewScenariosRecord(null, null, "Z", "record2"));
        results = service.resolveEntities(EntityResolutionConversionUtils.convertRecords(records), simpleAttributeParameterSet);
        returnRecords = EntityResolutionConversionUtils.convertRecordWrappers(results.getRecords());
        assertEquals(1, returnRecords.size());
        
    }

    @Test
    public void testScenarioM5() throws Exception {

        List<ExternallyIdentifiableRecord> records;
        EntityResolutionResults results;
        List<ExternallyIdentifiableRecord> returnRecords;

        records = new ArrayList<ExternallyIdentifiableRecord>();
        records.add(makeNewScenariosRecord("X", "Y", "Z", "record1"));
        records.add(makeNewScenariosRecord(null, "Y", "Z", "record2"));
        results = service.resolveEntities(EntityResolutionConversionUtils.convertRecords(records), simpleAttributeParameterSet);
        returnRecords = EntityResolutionConversionUtils.convertRecordWrappers(results.getRecords());
        assertEquals(1, returnRecords.size());
        
    }

    @Test
    public void testScenarioM6() throws Exception {

        List<ExternallyIdentifiableRecord> records;
        EntityResolutionResults results;
        List<ExternallyIdentifiableRecord> returnRecords;

        records = new ArrayList<ExternallyIdentifiableRecord>();
        records.add(makeNewScenariosRecord("X", "Y", "Z", "record1"));
        records.add(makeNewScenariosRecord(null, "Y", "Q", "record2"));
        results = service.resolveEntities(EntityResolutionConversionUtils.convertRecords(records), simpleAttributeParameterSet);
        returnRecords = EntityResolutionConversionUtils.convertRecordWrappers(results.getRecords());
        assertEquals(1, returnRecords.size());
        
    }

    @Test
    public void testScenarioM7() throws Exception {

        List<ExternallyIdentifiableRecord> records;
        EntityResolutionResults results;
        List<ExternallyIdentifiableRecord> returnRecords;

        records = new ArrayList<ExternallyIdentifiableRecord>();
        records.add(makeNewScenariosRecord("X", "Y", "Z", "record1"));
        records.add(makeNewScenariosRecord(null, null, "Z", "record2"));
        results = service.resolveEntities(EntityResolutionConversionUtils.convertRecords(records), simpleAttributeParameterSet);
        returnRecords = EntityResolutionConversionUtils.convertRecordWrappers(results.getRecords());
        assertEquals(1, returnRecords.size());
        
    }

    @Test
    public void testScenarioM8() throws Exception {

        List<ExternallyIdentifiableRecord> records;
        EntityResolutionResults results;
        List<ExternallyIdentifiableRecord> returnRecords;

        records = new ArrayList<ExternallyIdentifiableRecord>();
        records.add(makeNewScenariosRecord("X", null, "Z", "record1"));
        records.add(makeNewScenariosRecord(null, null, "Z", "record2"));
        results = service.resolveEntities(EntityResolutionConversionUtils.convertRecords(records), simpleAttributeParameterSet);
        returnRecords = EntityResolutionConversionUtils.convertRecordWrappers(results.getRecords());
        assertEquals(1, returnRecords.size());
        
    }

    @Test
    public void testScenarioN1() throws Exception {

        List<ExternallyIdentifiableRecord> records;
        EntityResolutionResults results;
        List<ExternallyIdentifiableRecord> returnRecords;

        records = new ArrayList<ExternallyIdentifiableRecord>();
        records.add(makeNewScenariosRecord("X", "Y", "Z", "record1"));
        records.add(makeNewScenariosRecord("Q", "Y", "Z", "record2"));
        results = service.resolveEntities(EntityResolutionConversionUtils.convertRecords(records), simpleAttributeParameterSet);
        returnRecords = EntityResolutionConversionUtils.convertRecordWrappers(results.getRecords());
        assertEquals(2, returnRecords.size());
        
    }

    @Test
    public void testScenarioN2() throws Exception {

        List<ExternallyIdentifiableRecord> records;
        EntityResolutionResults results;
        List<ExternallyIdentifiableRecord> returnRecords;

        records = new ArrayList<ExternallyIdentifiableRecord>();
        records.add(makeNewScenariosRecord("X", "Y", "Z", "record1"));
        records.add(makeNewScenariosRecord(null, null, "Q", "record2"));
        results = service.resolveEntities(EntityResolutionConversionUtils.convertRecords(records), simpleAttributeParameterSet);
        returnRecords = EntityResolutionConversionUtils.convertRecordWrappers(results.getRecords());
        assertEquals(2, returnRecords.size());
        
    }

    @Test
    public void testScenarioN3() throws Exception {

        List<ExternallyIdentifiableRecord> records;
        EntityResolutionResults results;
        List<ExternallyIdentifiableRecord> returnRecords;

        records = new ArrayList<ExternallyIdentifiableRecord>();
        records.add(makeNewScenariosRecord("X", null, "Z", "record1"));
        records.add(makeNewScenariosRecord(null, null, "Q", "record2"));
        results = service.resolveEntities(EntityResolutionConversionUtils.convertRecords(records), simpleAttributeParameterSet);
        returnRecords = EntityResolutionConversionUtils.convertRecordWrappers(results.getRecords());
        assertEquals(2, returnRecords.size());
        
    }

    @Test
    public void testScenarioN4() throws Exception {

        List<ExternallyIdentifiableRecord> records;
        EntityResolutionResults results;
        List<ExternallyIdentifiableRecord> returnRecords;

        records = new ArrayList<ExternallyIdentifiableRecord>();
        records.add(makeNewScenariosRecord(null, null, "Z", "record1"));
        records.add(makeNewScenariosRecord(null, null, "Q", "record2"));
        results = service.resolveEntities(EntityResolutionConversionUtils.convertRecords(records), simpleAttributeParameterSet);
        returnRecords = EntityResolutionConversionUtils.convertRecordWrappers(results.getRecords());
        assertEquals(2, returnRecords.size());
        
    }

    @Test
    public void testScenarioTR1() throws Exception {

        List<ExternallyIdentifiableRecord> records;
        EntityResolutionResults results;
        List<ExternallyIdentifiableRecord> returnRecords;

        records = new ArrayList<ExternallyIdentifiableRecord>();
        records.add(makeNewScenariosRecord("X", "Y", "Z", "record1"));
        records.add(makeNewScenariosRecord("X", "Y", "Q", "record2"));
        records.add(makeNewScenariosRecord("X", "Y", "P", "record3"));
        results = service.resolveEntities(EntityResolutionConversionUtils.convertRecords(records), simpleAttributeParameterSet);
        returnRecords = EntityResolutionConversionUtils.convertRecordWrappers(results.getRecords());
        assertEquals(1, returnRecords.size());
        
    }

    @Test
    public void testScenarioTR2() throws Exception {

        List<ExternallyIdentifiableRecord> records;
        EntityResolutionResults results;
        List<ExternallyIdentifiableRecord> returnRecords;

        records = new ArrayList<ExternallyIdentifiableRecord>();
        records.add(makeNewScenariosRecord("X", "Y", "Z", "record1"));
        records.add(makeNewScenariosRecord("X", "Y", "Q", "record2"));
        records.add(makeNewScenariosRecord(null, "Y", "P", "record3"));
        results = service.resolveEntities(EntityResolutionConversionUtils.convertRecords(records), simpleAttributeParameterSet);
        returnRecords = EntityResolutionConversionUtils.convertRecordWrappers(results.getRecords());
        assertEquals(1, returnRecords.size());
        
    }

    @Test
    public void testScenarioTR3() throws Exception {

        List<ExternallyIdentifiableRecord> records;
        EntityResolutionResults results;
        List<ExternallyIdentifiableRecord> returnRecords;

        records = new ArrayList<ExternallyIdentifiableRecord>();
        records.add(makeNewScenariosRecord("X", "Y", "Z", "record1"));
        records.add(makeNewScenariosRecord("X", "Y", "Q", "record2"));
        records.add(makeNewScenariosRecord(null, null, "P", "record3"));
        results = service.resolveEntities(EntityResolutionConversionUtils.convertRecords(records), simpleAttributeParameterSet);
        returnRecords = EntityResolutionConversionUtils.convertRecordWrappers(results.getRecords());
        assertEquals(2, returnRecords.size());
        
        for (ExternallyIdentifiableRecord r : returnRecords) {
            if (!isEmpty(r.getAttribute("A1"))) {
                assertEquals("record2", r.getExternalId());
                assertTrue(r.getRelatedIds().contains("record1"));
            }
            else {
                assertEquals("record3", r.getExternalId());
            }
        }
        
    }

    @Test
    public void testScenarioTR4() throws Exception {

        List<ExternallyIdentifiableRecord> records;
        EntityResolutionResults results;
        List<ExternallyIdentifiableRecord> returnRecords;

        records = new ArrayList<ExternallyIdentifiableRecord>();
        records.add(makeNewScenariosRecord("X", "Y", "Z", "record1"));
        records.add(makeNewScenariosRecord("X", "Y", "Q", "record2"));
        records.add(makeNewScenariosRecord(null, null, "Q", "record3"));
        results = service.resolveEntities(EntityResolutionConversionUtils.convertRecords(records), simpleAttributeParameterSet);
        returnRecords = EntityResolutionConversionUtils.convertRecordWrappers(results.getRecords());
        assertEquals(1, returnRecords.size());
        
    }

    @Test
    public void testScenarioTR5() throws Exception {

        List<ExternallyIdentifiableRecord> records;
        EntityResolutionResults results;
        List<ExternallyIdentifiableRecord> returnRecords;

        records = new ArrayList<ExternallyIdentifiableRecord>();
        records.add(makeNewRealWorldScenariosRecord("A123", "F987", "Smith", "Tom", "1/1/1970", "record1"));
        records.add(makeNewRealWorldScenariosRecord("A123", "F987", "Jones", "Jerry", "2/1/1969", "record2"));
        records.add(makeNewRealWorldScenariosRecord(null, null, "Jones", "Gerry", "12/11/1969", "record3"));
        results = service.resolveEntities(EntityResolutionConversionUtils.convertRecords(records), realisticAttributeParameterSet);
        returnRecords = EntityResolutionConversionUtils.convertRecordWrappers(results.getRecords());
        assertEquals(1, returnRecords.size());
        
    }

    @Test
    public void testScenarioTR6() throws Exception {

        List<ExternallyIdentifiableRecord> records;
        EntityResolutionResults results;
        List<ExternallyIdentifiableRecord> returnRecords;

        records = new ArrayList<ExternallyIdentifiableRecord>();
        records.add(makeNewRealWorldScenariosRecord("A123", null, "McCartney", "Paul", "6/18/1942", "record1"));
        records.add(makeNewRealWorldScenariosRecord("A123", "F987", "Lennon", "John", "10/9/1940", "record2"));
        records.add(makeNewRealWorldScenariosRecord(null, "F987", "Harrison", "George", "2/25/1943", "record3"));
        results = service.resolveEntities(EntityResolutionConversionUtils.convertRecords(records), realisticAttributeParameterSet);
        returnRecords = EntityResolutionConversionUtils.convertRecordWrappers(results.getRecords());
        assertEquals(1, returnRecords.size());
        
    }

    private boolean isEmpty(Attribute attribute) {
        boolean ret = true;
        if (attribute != null) {
            Iterator<String> it = attribute.iterator();
            while (it.hasNext() && ret) {
                String next = it.next();
                if (next != null && next.trim().length() > 0)
                {
                    ret = false;
                }
            }
        }
        return ret;
    }

    @Test
    public void testSomeNullDeterministicAttributeMerge() throws Exception {

        // test for a bug found during demo on 5/2/2013

        List<ExternallyIdentifiableRecord> records;
        EntityResolutionResults results;
        List<ExternallyIdentifiableRecord> returnRecords;

        // Scenario 1

        records = new ArrayList<ExternallyIdentifiableRecord>();
        records.add(makeRecord("Andrew", null, "123456789", "record1"));
        records.add(makeRecord("Joe", null, "123456789", "record2"));

        results = service.resolveEntities(EntityResolutionConversionUtils.convertRecords(records), oldTestsAttributeParameterSet);
        returnRecords = EntityResolutionConversionUtils.convertRecordWrappers(results.getRecords());
        assertEquals(1, returnRecords.size());

    }

    @Test
    public void testDeterministicAttributeMergeWithNullEquality() throws Exception {

        // test for new requirements found during demo on 5/31/2013

        List<ExternallyIdentifiableRecord> records;
        EntityResolutionResults results;
        List<ExternallyIdentifiableRecord> returnRecords;

        records = new ArrayList<ExternallyIdentifiableRecord>();
        records.add(makeRecord("Andrew", null, "123456789", "record1"));
        records.add(makeRecord("Joe", "123", "123456789", "record2"));

        results = service.resolveEntities(EntityResolutionConversionUtils.convertRecords(records), oldTestsAttributeParameterSet);
        returnRecords = EntityResolutionConversionUtils.convertRecordWrappers(results.getRecords());
        assertEquals(1, returnRecords.size());

    }

    // The following tests resulted from a bug from mid-April 2013. The functionality
    // should be (for two records):
    // Scenario 1: Two records have identical and non-empty values for a
    // deterministic factor. Result: Match (and do no further ER)
    // Scenario 2: Two records have non-identical and non-empty values for a
    // deterministic factor. Result: Non-match (and do no further ER)
    // Scenario 3: Two records both have empty values for all deterministic
    // factors. Result: forward on to ER
    // Scenario 4: Record A has a non-empty value for all deterministic
    // factors, and Record B has an empty value for those factors.
    // Result: Do no further ER on Record A, and forward Record B on to ER.

    @Test
    public void testBasicDeterministicAttributeMergeScenario1() throws Exception {

        List<ExternallyIdentifiableRecord> records;
        EntityResolutionResults results;
        List<ExternallyIdentifiableRecord> returnRecords;

        // Scenario 1

        records = new ArrayList<ExternallyIdentifiableRecord>();
        records.add(makeRecord("Andrew", "123", "123456789", "record1"));
        records.add(makeRecord("Joe", "123", "123456789", "record2"));

        results = service.resolveEntities(EntityResolutionConversionUtils.convertRecords(records), oldTestsAttributeParameterSet);
        returnRecords = EntityResolutionConversionUtils.convertRecordWrappers(results.getRecords());
        assertEquals(1, returnRecords.size()); // because all deterministic
                                               // factors are present and equal
    }

    @Test
    public void testBasicDeterministicAttributeMergeScenario2() throws Exception {

        List<ExternallyIdentifiableRecord> records;
        EntityResolutionResults results;
        List<ExternallyIdentifiableRecord> returnRecords;

        // Scenario 2

        records = new ArrayList<ExternallyIdentifiableRecord>();
        records.add(makeRecord("Andrew", "123", "123456789", "record1"));
        records.add(makeRecord("Joe", "124", "123456789", "record2"));

        results = service.resolveEntities(EntityResolutionConversionUtils.convertRecords(records), oldTestsAttributeParameterSet);
        returnRecords = EntityResolutionConversionUtils.convertRecordWrappers(results.getRecords());
        assertEquals(2, returnRecords.size()); // because one deterministic
                                               // factor (sid) is different

        records = new ArrayList<ExternallyIdentifiableRecord>();
        records.add(makeRecord("Andrew", "123", "123456789", "record1"));
        records.add(makeRecord("Andruw", "124", "123456789", "record2"));

        results = service.resolveEntities(EntityResolutionConversionUtils.convertRecords(records), oldTestsAttributeParameterSet);
        returnRecords = EntityResolutionConversionUtils.convertRecordWrappers(results.getRecords());
        assertEquals(2, returnRecords.size()); // showing that even though ER
                                               // would normally merge these,
                                               // we don't merge because the
                                               // det factors are unequal
    }

    @Test
    public void testBasicDeterministicAttributeMergeScenario3() throws Exception {

        List<ExternallyIdentifiableRecord> records;
        EntityResolutionResults results;
        List<ExternallyIdentifiableRecord> returnRecords;

        // Scenario 3

        records = new ArrayList<ExternallyIdentifiableRecord>();
        records.add(makeRecord("Andrew", null, null, "record1"));
        records.add(makeRecord("Andruw", null, null, "record2"));

        results = service.resolveEntities(EntityResolutionConversionUtils.convertRecords(records), oldTestsAttributeParameterSet);
        returnRecords = EntityResolutionConversionUtils.convertRecordWrappers(results.getRecords());
        assertEquals(1, returnRecords.size()); // because we forward them on to
                                               // ER, and Andrew/Andruw is
                                               // close enough to merge

        records = new ArrayList<ExternallyIdentifiableRecord>();
        records.add(makeRecord("Andrew", null, null, "record1"));
        records.add(makeRecord("Joe", null, null, "record2"));

        results = service.resolveEntities(EntityResolutionConversionUtils.convertRecords(records), oldTestsAttributeParameterSet);
        returnRecords = EntityResolutionConversionUtils.convertRecordWrappers(results.getRecords());
        assertEquals(2, returnRecords.size()); // because we forward them on to
                                               // ER, and Andrew/Joe not close
                                               // enough to merge

    }

    @Test
    public void testBasicDeterministicAttributeMergeScenario4() throws Exception {

        List<ExternallyIdentifiableRecord> records;
        EntityResolutionResults results;
        List<ExternallyIdentifiableRecord> returnRecords;

        // Scenario 4

        records = new ArrayList<ExternallyIdentifiableRecord>();
        records.add(makeRecord("Andrew", null, null, "record1"));
        records.add(makeRecord("Andruw", null, null, "record2"));
        records.add(makeRecord("Andruw", "124", "123456789", "record3"));

        results = service.resolveEntities(EntityResolutionConversionUtils.convertRecords(records), oldTestsAttributeParameterSet);
        returnRecords = EntityResolutionConversionUtils.convertRecordWrappers(results.getRecords());
        
//        note: prior to Dec 2013 refactoring, this would've resulted in 2 records
//        because we forward on records
//        1 and 2 to ER and they merge
//        (Andrew/Andruw close enuf)
//        and rec3 is separate because
//        of det factors
//        assertEquals(2, returnRecords.size());
        
        assertEquals(1, returnRecords.size());

        records = new ArrayList<ExternallyIdentifiableRecord>();
        records.add(makeRecord("Andrew", null, null, "record1"));
        records.add(makeRecord("Joe", null, null, "record2"));
        records.add(makeRecord("Andruw", "124", "123456789", "record3"));

        results = service.resolveEntities(EntityResolutionConversionUtils.convertRecords(records), oldTestsAttributeParameterSet);
        returnRecords = EntityResolutionConversionUtils.convertRecordWrappers(results.getRecords());

        // similarly, prior to Dec 2013 refactoring, this would've resulted in 3 records
        // because we forward on records
        // 1 and 2 to ER and they dont
        // merge (Andrew/Joe not close
        // enuf) and rec3 is separate
        // because of det factors
        // assertEquals(3, returnRecords.size());

        assertEquals(2, returnRecords.size());
        

    }

    private ExternallyIdentifiableRecord makeNewRealWorldScenariosRecord(String sid, String fbi, String lastName, String firstName, String DOB, String recordId) {
        return new ExternallyIdentifiableRecord(makeAttributes(new Attribute("SID", sid), new Attribute("FBI", fbi), new Attribute("LastName", lastName), new Attribute("FirstName", firstName), new Attribute("DOB", DOB)), recordId);
    }

    private ExternallyIdentifiableRecord makeNewScenariosRecord(String a1, String a2, String a3, String recordId) {
        return new ExternallyIdentifiableRecord(makeAttributes(new Attribute("A1", a1), new Attribute("A2", a2), new Attribute("A3", a3)), recordId);
    }

    private ExternallyIdentifiableRecord makeRecord(String givenName, String sid, String ssn, String recordId) {
        return new ExternallyIdentifiableRecord(makeAttributes(new Attribute("givenName", givenName), new Attribute("sid", sid), new Attribute("ssn", ssn)), recordId);
    }

    private static Map<String, Attribute> makeAttributes(Attribute... attributes) {
        Map<String, Attribute> ret = new HashMap<String, Attribute>();
        for (Attribute a : attributes) {
            ret.put(a.getType(), a);
        }
        return ret;
    }

}
