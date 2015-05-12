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

import gov.nij.er.*;
import junit.framework.*;

import org.junit.*;
import org.junit.Test;

public class AlgorithmScoreTest extends TestCase
{
    
    private static final String JARO_DISTANCE_IMPL = "com.wcohen.ss.Jaro";
    private static final String JARO_WINKLER_DISTANCE_IMPL = "com.wcohen.ss.JaroWinkler";
    private static final String LEVENSTEIN_DISTANCE_IMPL = "uk.ac.shef.wit.simmetrics.similaritymetrics.Levenshtein";
    private static final String SOUNDEX_DISTANCE_IMPL = "uk.ac.shef.wit.simmetrics.similaritymetrics.Soundex";
    
    private StringDistanceScoreMatcher jaroStringDistanceScoreMatcher;
    private StringDistanceScoreMatcher jaroWinklerStringDistanceScoreMatcher;
    private StringDistanceScoreMatcher levensteinStringDistanceScoreMatcher;
    private StringDistanceScoreMatcher soundexStringDistanceScoreMatcher;

    @Before
    public void setUp() throws Exception
    {
        jaroStringDistanceScoreMatcher = new StringDistanceScoreMatcher(JARO_DISTANCE_IMPL);
        jaroStringDistanceScoreMatcher.init();
        jaroWinklerStringDistanceScoreMatcher = new StringDistanceScoreMatcher(JARO_WINKLER_DISTANCE_IMPL);
        jaroWinklerStringDistanceScoreMatcher.init();
        levensteinStringDistanceScoreMatcher = new StringDistanceScoreMatcher(LEVENSTEIN_DISTANCE_IMPL);
        levensteinStringDistanceScoreMatcher.init();
        soundexStringDistanceScoreMatcher = new StringDistanceScoreMatcher(SOUNDEX_DISTANCE_IMPL);
        soundexStringDistanceScoreMatcher.init();
    }
    
    @Test
    public void testJaro()
    {
        double score = jaroStringDistanceScoreMatcher.score("Jones", "Johnson");
        assertEquals(0.7904761904761904761904761904754, score, 0.000000000000001); // means they are within 0.000000000000001 of each other, but not necessarily equal
    }

    @Test
    public void testJaro3() {
		double score;
		score = jaroStringDistanceScoreMatcher.score("ABCVWXYZ", "CABVWXYZ");
        assertEquals(0.95833333333333333333333333333238, score, 0.000000000000001);
	}

    @Test
    public void testJaro2() {
		double score;
		score = jaroStringDistanceScoreMatcher.score("Martha", "Marhta");
        assertEquals(0.944444444444444, score, 0.000000000000001);
	}
    
    @Test
    public void testJaro4()
    {
        double score = jaroWinklerStringDistanceScoreMatcher.score("Jones", "Jones");
        assertEquals(1.0, score, 0.000000000000001);
    }
    
    @Test
    public void testJaro5()
    {
        double score = jaroWinklerStringDistanceScoreMatcher.score("Clinton", "Bush");
        assertEquals(0.0, score, 0.000000000000001);
    }
    
    @Test
    public void testJaroWinkler()
    {
        double score = jaroWinklerStringDistanceScoreMatcher.score("Jones", "Johnson");
        assertEquals(0.83238095238095238095238095238, score, 0.000000000000001);
    }
    
    @Test
    public void testJaroWinkler4()
    {
        double score = jaroWinklerStringDistanceScoreMatcher.score("Jones", "Jones");
        assertEquals(1.0, score, 0.000000000000001);
    }
    
    @Test
    public void testJaroWinkler5()
    {
        double score = jaroWinklerStringDistanceScoreMatcher.score("Clinton", "Bush");
        assertEquals(0.0, score, 0.000000000000001);
    }

    @Test
    public void testJaroWinkler3() {
		double score;
		score = jaroWinklerStringDistanceScoreMatcher.score("ABCVWXYZ", "CABVWXYZ");
        assertEquals(0.95833333333333333333333333333238, score, 0.000000000000001);
	}

    @Test
    public void testJaroWinkler2() {
		double score;
		score = jaroWinklerStringDistanceScoreMatcher.score("Martha", "Marhta");
        assertEquals(0.9611111111111111, score, 0.000000000000001);
	}
    
    @Test
    public void testLevenstein()
    {
        double score = levensteinStringDistanceScoreMatcher.score("kitten", "sitting");
        // the implementation of Levenstein in simmetrics scales the true Levenstein distance to a range of 0..1 by dividing
        // by the length of the larger string
        // in this test case, the true distance is 3, the length of the larger string (sitting) is 7, and then the reciprocal
        // percentage is taken
        assertEquals(1.0 - (3.0/7.0), score, 0.0000001);
    }

    @Test
    public void testLevenstein3() {
    	double score = levensteinStringDistanceScoreMatcher.score("Saturday", "Sunday");
        assertEquals(1.0 - (3.0/8.0), score, 0.0000001);
	}

    @Test
    public void testLevenstein2() {
    	double score = levensteinStringDistanceScoreMatcher.score("Saturday", "Saturday");
        assertEquals(1.0, score);
	}
    
    @Test
    public void testLevenstein4() {
    	double score = levensteinStringDistanceScoreMatcher.score("Clinton", "Bush");
        assertEquals(0.0, score);
	}

}
