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

import gov.nij.er.ExternallyIdentifiableRecordFactory;
import gov.nij.er.StringDistanceScoreMatcher;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import serf.data.Attribute;
import serf.data.BasicMatcherMerger;
import serf.data.ExistentialBooleanComparator;
import serf.data.Record;
import serf.deduplication.RSwoosh;

/**
 * An OSGi service that provides the ability to resolve "entities". The interface closely follows the Stanford SERF toolkit.
 * 
 */
public class EntityResolutionService {

    private static final Log LOG = LogFactory.getLog(EntityResolutionService.class);

    /**
     * Resolve a set of entities.
     * 
     * @param records
     *            The input records, in an enhanced SERF structure
     * @param attributeParameters
     *            The information needed to control the resolution (algorithm, merge threshold, determinativeness)
     * @return A set of records with merged entities and a set of statistics for the merge
     * @throws Exception
     */
    public EntityResolutionResults resolveEntities(List<RecordWrapper> recordWrappers, Set<AttributeParameters> attributeParameters) throws Exception {
        return resolveEntities(recordWrappers, attributeParameters, Integer.MAX_VALUE);
    }

    /**
     * Resolve a set of entities.
     * 
     * @param records
     *            The input records, in an enhanced SERF structure
     * @param attributeParameters
     *            The information needed to control the resolution (algorithm, merge threshold, determinativeness)
     * @param recordLimit
     *            If the number of input records is greater than this, entity resolution will not be performed
     * @return A set of records with merged entities and a set of statistics for the merge
     * @throws Exception
     */
    public EntityResolutionResults resolveEntities(List<RecordWrapper> recordWrappers, Set<AttributeParameters> attributeParameters, int recordLimit) throws Exception {

        verifyProperSortConfig(attributeParameters);

        List<ExternallyIdentifiableRecord> returnRecordList = null;
        boolean recordLimitExceeded = false;

        if (recordWrappers.size() <= recordLimit) {

            ERSMatcherMerger matcherMerger = new ERSMatcherMerger();
            matcherMerger.init(attributeParameters);
            LOG.debug("In resolveEntities, recordWrappers=" + recordWrappers);
            Set<Record> inputRecords = new HashSet<Record>();
            List<ExternallyIdentifiableRecord> records = EntityResolutionConversionUtils.convertRecordWrappers(recordWrappers);

            inputRecords.addAll(records);
            
            Set<Record> rSwooshMerged = RSwoosh.execute(matcherMerger, inputRecords);

            LOG.debug("In resolveEntities, merged records from RSwoosh=" + rSwooshMerged);
            Set<ExternallyIdentifiableRecord> returnRecords = new HashSet<ExternallyIdentifiableRecord>();
            for (Record r : rSwooshMerged) {
                if (!(r instanceof ExternallyIdentifiableRecord)) {
                    throw new IllegalStateException("Somehow a type of Record other than an ExternallyIdentifiableRecord got into the merge results, type=" + r.getClass().getName());
                }
                returnRecords.add((ExternallyIdentifiableRecord) r);
            }

            returnRecordList = new ArrayList<ExternallyIdentifiableRecord>();
            returnRecordList.addAll(returnRecords);

        } else {
            returnRecordList = EntityResolutionConversionUtils.convertRecordWrappers(recordWrappers);
            recordLimitExceeded = true;
        }

        Collections.sort(returnRecordList, new RecordComparator(attributeParameters));

        LOG.debug("In resolveEntities, returnRecords combining DF and RSwoosh=" + returnRecordList);
        Map<String, Set<AttributeStatistics>> statistics = computeStatistics(returnRecordList, attributeParameters);
        EntityResolutionResults ret = new EntityResolutionResults(EntityResolutionConversionUtils.convertRecords(returnRecordList), statistics, recordLimitExceeded);
        return ret;
    }

    private void verifyProperSortConfig(Set<AttributeParameters> attributeParameters) {
        Set<Integer> ranks = new HashSet<Integer>();
        for (AttributeParameters ap : attributeParameters) {
            SortOrderSpecification sos = ap.getSortOrder();
            if (sos != null) {
                Integer rank = sos.getSortOrderRank();
                if (ranks.contains(rank)) {
                    throw new IllegalStateException("Duplicate sort rank of " + rank + " configured.");
                }
                ranks.add(rank);
            }
        }
    }

    private Map<String, Set<AttributeStatistics>> computeStatistics(List<ExternallyIdentifiableRecord> records, Set<AttributeParameters> attributeParameters) {
        // the reason we do this after the fact, rather than computing the
        // statistics as we go, is to leave the RSwoosh implementation
        // from SERF as intact as possible. Since it does not compute stats, we
        // do so in a second pass after the fact.
        Map<String, Set<AttributeStatistics>> ret = new HashMap<String, Set<AttributeStatistics>>();
        for (ExternallyIdentifiableRecord record : records) {
            Set<AttributeStatistics> statSet = new HashSet<AttributeStatistics>();
            ret.put(record.getExternalId(), statSet);
            for (AttributeParameters ap : attributeParameters) {
                double mean = computeMean(record, ap);
                double sd = computeStandardDeviation(record, ap);
                AttributeStatistics stats = new AttributeStatistics(ap.getAttributeName());
                // note: for the initial pilot, we are returning zero for these
                // values, because we need to re-assess what these
                // metrics mean and do not have time to do so prior to the first
                // federated query demo. consider this sample case.
                // You have a record that is a merge of 10 original records, and
                // because 9 of them had identical values for an
                // attribute, there are only two attribute values in the
                // resultant merged record. if we compute the mean as the
                // mean of the pairwise distance values, we would get a very
                // skewed measure, since it would be one distance divided by
                // two, when in fact 10 original records were involved. So we
                // either need to change RSwoosh to keep track of stats
                // as we go, or else come up with some other "quality" measure
                // that accounts for attribute value merging.
                stats.setAverageStringDistance(mean);
                stats.setStandardDeviationStringDistance(sd);
                statSet.add(stats);
            }
        }
        return ret;
    }

    private double computeStandardDeviation(ExternallyIdentifiableRecord record, AttributeParameters attributeParameters) {
        return 0;
    }

    private double computeMean(ExternallyIdentifiableRecord record, AttributeParameters attributeParameters) {
        return 0;
    }

    private static final class ERSMatcherMerger extends BasicMatcherMerger {

        private Map<String, ExistentialBooleanComparator> comparatorMap = new HashMap<String, ExistentialBooleanComparator>();
        private Set<AttributeParameters> attributeParameters;

        public ERSMatcherMerger() {
            super._factory = new ExternallyIdentifiableRecordFactory();
        }

        public void init(Set<AttributeParameters> attributeParameters) throws Exception {
            this.attributeParameters = attributeParameters;
            LOG.info("Initializing ERSMatcherMerger with parameters " + attributeParameters);
            for (AttributeParameters ap : attributeParameters) {
                if (ap == null || ap.getAttributeName() == null || ap.getAlgorithmClassName() == null) {
                    throw new IllegalArgumentException("AttributeParameters object has a null object.");
                }

                StringDistanceScoreMatcher matcher = new StringDistanceScoreMatcher(ap.getAlgorithmClassName());
                matcher.init(ap.getThreshold());
                comparatorMap.put(ap.getAttributeName(), new ExistentialBooleanComparator(matcher));
            }
        }

        private static final int MATCH = 1;
        private static final int NO_MATCH = 2;
        private static final int MATCH_INDETERMINATE = 3;

        protected boolean matchInternal(Record r1, Record r2) {

            Map<String, Attribute> r1attr = r1.getAttributes();
            Map<String, Attribute> r2attr = r2.getAttributes();

            if (!haveSameAttributes(r1attr, r2attr)) {
                return false;
            }

            int deterministicMatch = matchDeterministicAttributes(r1, r2);

            if (deterministicMatch == MATCH_INDETERMINATE) {
                LOG.debug("Indeterminate result from deterministic evaluation");
                boolean nonDeterministicAttributeExists = false;
                for (String s1 : comparatorMap.keySet()) {
                    Attribute a1 = r1attr.get(s1);
                    if (a1 == null) {
                        LOG.warn("Record does not contain specified attribute " + s1 + ", record=" + r1);
                    }
                    Attribute a2 = r2attr.get(s1);
                    if (a2 == null) {
                        LOG.warn("Record does not contain specified attribute " + s1 + ", record=" + r2);
                    }
                    if (!attributeIsDeterminative(s1)) {
                        nonDeterministicAttributeExists = true;
                        LOG.debug("Non deterministic match evaluation on attribute " + s1);
                        ExistentialBooleanComparator ebc = comparatorMap.get(s1);
                        if (!ebc.attributesMatch(a1, a2)) {
                            LOG.debug("Attribute a1=" + a1 + " and a2=" + a2 + " do not match, thus records do not match");
                            return false;
                        }
                    }
                }
                LOG.debug(nonDeterministicAttributeExists ? "Records match" : "Records do not match because the deterministic factors match was indeterminate, and there were no non-deterministic attributes");
                return nonDeterministicAttributeExists;
            }

            return deterministicMatch == MATCH;

        }

        private int matchDeterministicAttributes(Record r1, Record r2) {
            Map<String, Attribute> r1attr = r1.getAttributes();
            Map<String, Attribute> r2attr = r2.getAttributes();
            boolean r1r2PairsAllNull = true;
            for (String s1 : comparatorMap.keySet()) {

                if (attributeIsDeterminative(s1)) {
                    
                    LOG.debug("Evaluating deterministic attribute " + s1);
                    
                    Attribute a1 = r1attr.get(s1);
                    boolean a1AllNull = attributeAllNull(a1);
                    Attribute a2 = r2attr.get(s1);
                    boolean a2AllNull = attributeAllNull(a2);

                    if (!(a1AllNull || a2AllNull)) {
                        if (!identical(a1, a2)) {
                            LOG.debug("Records do not match due to unequal, non-null deterministic attributes");
                            return NO_MATCH;
                        }
                        r1r2PairsAllNull = false;
                    }
                }
            }
            return (r1r2PairsAllNull) ? MATCH_INDETERMINATE : MATCH;
        }

        private boolean attributeAllNull(Attribute a) {
            boolean ret = false;
            if (a != null) {
                Iterator<String> values = a.iterator();
                boolean allNull = true;
                while (values.hasNext() && allNull) {
                    if (values.next() != null) {
                        allNull = false;
                    }
                }
                ret = allNull;
            }
            return ret;
        }

        private boolean attributeIsDeterminative(String s1) {
            for (AttributeParameters ap : attributeParameters) {
                if (s1.equals(ap.getAttributeName()) && ap.isDeterminative()) {
                    return true;
                }
            }
            return false;
        }

        private boolean identical(Attribute p1, Attribute p2) {
            if (p1 == null || p2 == null)
                return false;

            Iterator<String> i1 = p1.iterator();

            while (i1.hasNext()) {
                String s1 = (String) i1.next();
                Iterator<String> i2 = p2.iterator();

                while (i2.hasNext()) {
                    String s2 = (String) i2.next();

                    if (!stringsSame(s1, s2)) {
                        return false;
                    }
                }
            }

            return true;
        }

        private boolean stringsSame(String s1, String s2) {
            return s1 != null && s2 != null && s1.equals(s2);
        }

        private boolean haveSameAttributes(Map<String, Attribute> r1attr, Map<String, Attribute> r2attr) {
            return (r1attr == null && r2attr == null) || (r1attr.isEmpty() && r2attr.isEmpty()) || (r1attr.keySet().containsAll(r2attr.keySet()) && r2attr.keySet().containsAll(r1attr.keySet()));
        }

    }

    private static final class RecordComparator implements Comparator<ExternallyIdentifiableRecord> {

        private List<AttributeParameters> attributeParameters;

        public RecordComparator(Set<AttributeParameters> attributeParameters) {
            this.attributeParameters = new ArrayList<AttributeParameters>();
            this.attributeParameters.addAll(attributeParameters);
            Collections.sort(this.attributeParameters, new AttributeParametersComparator());
        }

        @Override
        public int compare(ExternallyIdentifiableRecord r1, ExternallyIdentifiableRecord r2) {
            for (AttributeParameters ap : attributeParameters) {
                SortOrderSpecification sos = ap.getSortOrder();
                if (sos != null) {
                    int reverseFactor = sos.getSortOrder().equals(SortOrderSpecification.SORT_ORDER_ASCENDING) ? 1 : -1;
                    Attribute a1 = r1.getAttribute(ap.getAttributeName());
                    Attribute a2 = r2.getAttribute(ap.getAttributeName());
                    if (!((a1 == null && a2 == null) || (a1.equals(a2)))) {
                        return a1.compareTo(a2) * reverseFactor;
                    }
                }
            }
            return 0;
        }

        private static final class AttributeParametersComparator implements Comparator<AttributeParameters> {

            @Override
            public int compare(AttributeParameters o1, AttributeParameters o2) {
                int ret = 0;
                if (o1 != null && o2 != null) {
                    SortOrderSpecification sos1 = o1.getSortOrder();
                    SortOrderSpecification sos2 = o2.getSortOrder();
                    if (sos1 != null && sos2 == null) {
                        ret = 1;
                    } else if (sos2 != null && sos1 == null) {
                        ret = -1;
                    } else if (sos1 != null && sos2 != null) {
                        int rank1 = sos1.getSortOrderRank();
                        int rank2 = sos2.getSortOrderRank();
                        if (rank1 != rank2) {
                            ret = (rank1 > rank2 ? 1 : -1);
                        }
                    }
                }
                return ret;
            }

        }

    }

}
