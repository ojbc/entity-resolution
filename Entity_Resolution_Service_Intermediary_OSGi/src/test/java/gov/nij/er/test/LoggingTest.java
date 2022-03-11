package gov.nij.er.test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import gov.nij.bundles.intermediaries.ers.osgi.AttributeParameters;
import gov.nij.bundles.intermediaries.ers.osgi.EntityResolutionConversionUtils;
import gov.nij.bundles.intermediaries.ers.osgi.EntityResolutionResults;
import gov.nij.bundles.intermediaries.ers.osgi.EntityResolutionService;
import gov.nij.bundles.intermediaries.ers.osgi.ExternallyIdentifiableRecord;
import serf.data.Attribute;

@RunWith(MockitoJUnitRunner.class)
@Ignore //TODO need to fix the junit test with the log4j2 APIs.  
public class LoggingTest {

    private static final String JARO_DISTANCE_IMPL = "com.wcohen.ss.Jaro";
    @SuppressWarnings("unused")
	private static final Log LOG = LogFactory.getLog(LoggingTest.class);

    @Mock
    private Appender mockAppender;
    private List<LogEvent> capturedEvents = new ArrayList<>();
    private EntityResolutionService service;
	@Before
    public void setup() {
//        MockitoAnnotations.openMocks(LoggingTest.class);
//        mockAppender = mock(Appender.class);
	    when(mockAppender.getName()).thenReturn("MockAppender");
	    when(mockAppender.isStarted()).thenReturn(true);
	    when(mockAppender.isStopped()).thenReturn(false);

	    // when append is called, convert the event to 
	    // immutable and add it to the event list
	    doAnswer(new Answer<Void>() {
	    	@Override
	    	public Void answer(InvocationOnMock invocation) {
	    		Object[] arguments = invocation.getArguments();
	    		if (arguments != null && arguments.length > 0 && arguments[0] != null  ) {
	             capturedEvents.add(((LogEvent) arguments[0]).toImmutable());
	    		}
	            return null;
             }
	    }).when(mockAppender).append(Mockito.any());
	    Logger logger = (Logger)LogManager.getRootLogger(); 
        logger.setLevel(Level.WARN);
        logger.addAppender(mockAppender);
        service = new EntityResolutionService();
    }

    @After
    public void teardown() {
        Logger logger = (Logger)LogManager.getRootLogger(); 
        logger.removeAppender(mockAppender);
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
        ArgumentCaptor<LogEvent> arguments = ArgumentCaptor.forClass(LogEvent.class);

        // note: if this test fails, it is likely because you've changed logging in the ERS class.

        verify(mockAppender, atLeastOnce()).append(arguments.capture());

        List<LogEvent> loggingEvents = arguments.getAllValues();
        int infoCount = 0;
        int warnCount = 0;
        for (LogEvent event : loggingEvents) {
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
        arguments = ArgumentCaptor.forClass(LogEvent.class);
        verify(mockAppender, atLeastOnce()).append(arguments.capture());

        loggingEvents = arguments.getAllValues();
        infoCount = 0;
        warnCount = 0;
        for (LogEvent event : loggingEvents) {
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
