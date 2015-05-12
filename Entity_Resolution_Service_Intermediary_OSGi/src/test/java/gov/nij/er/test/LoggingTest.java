package gov.nij.er.test;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import gov.nij.bundles.intermediaries.ers.osgi.AttributeParameters;
import gov.nij.bundles.intermediaries.ers.osgi.EntityResolutionConversionUtils;
import gov.nij.bundles.intermediaries.ers.osgi.EntityResolutionResults;
import gov.nij.bundles.intermediaries.ers.osgi.EntityResolutionService;
import gov.nij.bundles.intermediaries.ers.osgi.ExternallyIdentifiableRecord;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Appender;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.WriterAppender;
import org.apache.log4j.spi.LoggingEvent;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.mockito.Mockito.*;

import serf.data.Attribute;

public class LoggingTest {

    private static final String JARO_DISTANCE_IMPL = "com.wcohen.ss.Jaro";
    private static final Log LOG = LogFactory.getLog(LoggingTest.class);

    private WriterAppender mockAppender;

    private EntityResolutionService service;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(LoggingTest.class);
        mockAppender = mock(WriterAppender.class);
        mockAppender.setThreshold(Level.WARN);
        LogManager.getRootLogger().addAppender(mockAppender);
        service = new EntityResolutionService();
    }

    @After
    public void teardown() {
        LogManager.getRootLogger().removeAppender(mockAppender);
    }

    @Test
    public void testMultipleParameterConfigurationEnhancement() throws Exception {
        List<ExternallyIdentifiableRecord> records = new ArrayList<ExternallyIdentifiableRecord>();

        Attribute a1 = new Attribute("givenName", "Jane");
        Attribute a2 = new Attribute("surName", "Doe");
        Attribute a3 = new Attribute("sid", "12345");
        ExternallyIdentifiableRecord r1 = new ExternallyIdentifiableRecord(makeAttributes(a1, a2, a3), "record1");

        a1 = new Attribute("givenName", "John");
        a2 = new Attribute("surName", "Crow");
        a3 = new Attribute("sid", "67890");
        ExternallyIdentifiableRecord r2 = new ExternallyIdentifiableRecord(makeAttributes(a1, a2, a3), "record2");

        records.add(r1);
        records.add(r2);

        Set<AttributeParameters> attributeParametersSet = new HashSet<AttributeParameters>();
        AttributeParameters ap = new AttributeParameters("givenName");
        ap.setAlgorithmClassName(JARO_DISTANCE_IMPL);
        ap.setThreshold(.8);
        attributeParametersSet.add(ap);
        ap = new AttributeParameters("surName");
        ap.setAlgorithmClassName(JARO_DISTANCE_IMPL);
        ap.setThreshold(.8);
        attributeParametersSet.add(ap);
        ap = new AttributeParameters("sid");
        ap.setAlgorithmClassName(JARO_DISTANCE_IMPL);
        ap.setThreshold(.8);
        ap.setDeterminative(true);
        attributeParametersSet.add(ap);

        @SuppressWarnings("unused")
        EntityResolutionResults results = service.resolveEntities(EntityResolutionConversionUtils.convertRecords(records), attributeParametersSet);
        ArgumentCaptor<LoggingEvent> arguments = ArgumentCaptor.forClass(LoggingEvent.class);

        // note: if this test fails, it is likely because you've changed logging in the ERS class.

        verify(mockAppender, atLeastOnce()).doAppend(arguments.capture());

        List<LoggingEvent> loggingEvents = arguments.getAllValues();
        int infoCount = 0;
        int warnCount = 0;
        for (LoggingEvent event : loggingEvents) {
            if (event.getLevel().equals(Level.INFO)) {
                infoCount++;
            } else if (event.getLevel().equals(Level.WARN)) {
                warnCount++;
            }
        }
        assertEquals(1, infoCount);
        assertEquals(0, warnCount);

        a1 = new Attribute("color", "red");
        a2 = new Attribute("make", "chevy");
        a3 = new Attribute("VIN", "V12345");
        r1 = new ExternallyIdentifiableRecord(makeAttributes(a1, a2, a3), "record1");
        a1 = new Attribute("color", "green");
        a2 = new Attribute("make", "ford");
        a3 = new Attribute("VIN", new String[] {null});
        r2 = new ExternallyIdentifiableRecord(makeAttributes(a1, a2, a3), "record2");

        records = new ArrayList<ExternallyIdentifiableRecord>();
        records.add(r1);
        records.add(r2);

        attributeParametersSet = new HashSet<AttributeParameters>();
        ap = new AttributeParameters("color");
        ap.setAlgorithmClassName(JARO_DISTANCE_IMPL);
        ap.setThreshold(.8);
        attributeParametersSet.add(ap);
        ap = new AttributeParameters("make");
        ap.setAlgorithmClassName(JARO_DISTANCE_IMPL);
        ap.setThreshold(.8);
        attributeParametersSet.add(ap);
        ap = new AttributeParameters("VIN");
        ap.setAlgorithmClassName(JARO_DISTANCE_IMPL);
        ap.setThreshold(.8);
        ap.setDeterminative(true);
        attributeParametersSet.add(ap);

        reset(mockAppender);

        results = service.resolveEntities(EntityResolutionConversionUtils.convertRecords(records), attributeParametersSet);
        arguments = ArgumentCaptor.forClass(LoggingEvent.class);
        verify(mockAppender, atLeastOnce()).doAppend(arguments.capture());

        loggingEvents = arguments.getAllValues();
        infoCount = 0;
        warnCount = 0;
        for (LoggingEvent event : loggingEvents) {
            if (event.getLevel().equals(Level.INFO)) {
                infoCount++;
            } else if (event.getLevel().equals(Level.WARN)) {
                warnCount++;
            }
        }
        assertEquals(1, infoCount);
        assertEquals(0, warnCount);

    }

    private static Map<String, Attribute> makeAttributes(Attribute... attributes) {
        Map<String, Attribute> ret = new HashMap<String, Attribute>();
        for (Attribute a : attributes) {
            ret.put(a.getType(), a);
        }
        return ret;
    }

}
