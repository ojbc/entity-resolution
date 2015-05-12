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

import org.apache.log4j.*;

import serf.data.*;
import uk.ac.shef.wit.simmetrics.similaritymetrics.InterfaceStringMetric;

import com.wcohen.ss.api.*;

/**
 * 
 * An implementation of the SERF AtomicMatch interface that performs matches using a particular algorithm.
 * To use, instantiate and pass in a string representing the full class name of the algorithm.  Both simmetrics
 * and secondstring algorithms are available.
 *
 */
public class StringDistanceScoreMatcher implements AtomicMatch
{
    
    public static final double DEFAULT_THRESHOLD = 0.9;

    private static final Logger LOGGER = Logger.getLogger(StringDistanceScoreMatcher.class.getName());
    
    private String algorithmClassName;
    private StringDistance stringDistance;
    private double scoreThreshold;
    
    public StringDistanceScoreMatcher(String algorithmClassName)
    {
        this.algorithmClassName = algorithmClassName;
        
    }
    
    public StringDistanceScoreMatcher init(double scoreThreshold) throws InstantiationException, IllegalAccessException, ClassNotFoundException
    {
        Class<?> algorithmClass = Class.forName(algorithmClassName);
        if (InterfaceStringMetric.class.isAssignableFrom(algorithmClass))
        {
        	stringDistance = new StringMetricStringDistance((InterfaceStringMetric) algorithmClass.newInstance());
        }
        else
        {
        	stringDistance = (StringDistance) algorithmClass.newInstance();
        }
        setThreshold(scoreThreshold);
        return this;
    }

    public void setThreshold(double scoreThreshold)
    {
        this.scoreThreshold = scoreThreshold;
    }
    
    public double getThreshold()
    {
        return scoreThreshold;
    }
    
    public void init() throws InstantiationException, IllegalAccessException, ClassNotFoundException
    {
        init(DEFAULT_THRESHOLD);
    }
    
    public boolean valuesMatch(String arg0, String arg1)
    {
        double score = score(arg0, arg1);
        boolean match = score > scoreThreshold || scoreThreshold == 0;
        LOGGER.debug("Threshold=" + scoreThreshold + ", match=" + match + (scoreThreshold == 0 ? " (zero threshold always matches)" : ""));
        return match;
    }

    public double score(String arg0, String arg1)
    {
        double score = stringDistance.score(arg0, arg1);
        LOGGER.debug(algorithmClassName + " comparing [" + arg0 + "] to [" + arg1 + "] = " + score);
        return score;
    }

}
