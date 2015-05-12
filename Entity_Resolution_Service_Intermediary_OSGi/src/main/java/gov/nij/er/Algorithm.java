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

/**
 * A class representing the string distance algorithms available.
 *
 */
public final class Algorithm {
	
	private String className;
	private String shortName;
	private String toolkit;
	
	/**
	 * List of string distance algorithms supported by the library.
	 */
	public static Algorithm[] SUPPORTED_ALGORITHMS = {
		new Algorithm("com.wcohen.ss.AffineGap", "Second String"),
		new Algorithm("com.wcohen.ss.ApproxNeedlemanWunsch", "Second String"),
		new Algorithm("com.wcohen.ss.DirichletJS", "Second String"),
		new Algorithm("com.wcohen.ss.Jaccard", "Second String"),
		new Algorithm("com.wcohen.ss.Jaro", "Second String"),
		new Algorithm("com.wcohen.ss.JaroWinkler", "Second String"),
		new Algorithm("com.wcohen.ss.JaroWinklerTFIDF", "Second String"),
		new Algorithm("com.wcohen.ss.JelinekMercerJS", "Second String"),
		new Algorithm("com.wcohen.ss.JensenShannonDistance", "Second String"),
		new Algorithm("com.wcohen.ss.Level2", "Second String"),
		new Algorithm("com.wcohen.ss.Level2Jaro", "Second String"),
		new Algorithm("com.wcohen.ss.Level2JaroWinkler", "Second String"),
		new Algorithm("com.wcohen.ss.Level2Levenstein", "Second String"),
		new Algorithm("com.wcohen.ss.Level2MongeElkan", "Second String"),
		new Algorithm("com.wcohen.ss.Levenstein", "Second String"),
		new Algorithm("com.wcohen.ss.Mixture", "Second String"),
		new Algorithm("com.wcohen.ss.MongeElkan", "Second String"),
		new Algorithm("com.wcohen.ss.MultiStringAvgDistance", "Second String"),
		new Algorithm("com.wcohen.ss.MultiStringDistance", "Second String"),
		new Algorithm("com.wcohen.ss.NeedlemanWunsch", "Second String"),
		new Algorithm("com.wcohen.ss.ScaledLevenstein", "Second String"),
		new Algorithm("com.wcohen.ss.SmithWaterman", "Second String"),
		new Algorithm("com.wcohen.ss.SoftTFIDF", "Second String"),
		new Algorithm("com.wcohen.ss.SoftTokenFelligiSunter", "Second String"),
		new Algorithm("com.wcohen.ss.TagLink", "Second String"),
		new Algorithm("com.wcohen.ss.TagLinkToken", "Second String"),
		new Algorithm("com.wcohen.ss.TFIDF", "Second String"),
		new Algorithm("com.wcohen.ss.TokenFelligiSunter", "Second String"),
		new Algorithm("com.wcohen.ss.UnsmoothedJS", "Second String"),
		new Algorithm("com.wcohen.ss.WinklerRescorer", "Second String"),
		new Algorithm("uk.ac.shef.wit.simmetrics.similaritymetrics.AbstractStringMetric", "Simmetrics"),
		new Algorithm("uk.ac.shef.wit.simmetrics.similaritymetrics.BlockDistance", "Simmetrics"),
		new Algorithm("uk.ac.shef.wit.simmetrics.similaritymetrics.ChapmanLengthDeviation", "Simmetrics"),
		new Algorithm("uk.ac.shef.wit.simmetrics.similaritymetrics.ChapmanMatchingSoundex", "Simmetrics"),
		new Algorithm("uk.ac.shef.wit.simmetrics.similaritymetrics.ChapmanMeanLength", "Simmetrics"),
		new Algorithm("uk.ac.shef.wit.simmetrics.similaritymetrics.ChapmanOrderedNameCompoundSimilarity", "Simmetrics"),
		new Algorithm("uk.ac.shef.wit.simmetrics.similaritymetrics.CosineSimilarity", "Simmetrics"),
		new Algorithm("uk.ac.shef.wit.simmetrics.similaritymetrics.DiceSimilarity", "Simmetrics"),
		new Algorithm("uk.ac.shef.wit.simmetrics.similaritymetrics.EuclideanDistance", "Simmetrics"),
		new Algorithm("uk.ac.shef.wit.simmetrics.similaritymetrics.InterfaceStringMetric", "Simmetrics"),
		new Algorithm("uk.ac.shef.wit.simmetrics.similaritymetrics.JaccardSimilarity", "Simmetrics"),
		new Algorithm("uk.ac.shef.wit.simmetrics.similaritymetrics.Jaro", "Simmetrics"),
		new Algorithm("uk.ac.shef.wit.simmetrics.similaritymetrics.JaroWinkler", "Simmetrics"),
		new Algorithm("uk.ac.shef.wit.simmetrics.similaritymetrics.Levenshtein", "Simmetrics"),
		new Algorithm("uk.ac.shef.wit.simmetrics.similaritymetrics.MatchingCoefficient", "Simmetrics"),
		new Algorithm("uk.ac.shef.wit.simmetrics.similaritymetrics.MongeElkan", "Simmetrics"),
		new Algorithm("uk.ac.shef.wit.simmetrics.similaritymetrics.NeedlemanWunch", "Simmetrics"),
		new Algorithm("uk.ac.shef.wit.simmetrics.similaritymetrics.OverlapCoefficient", "Simmetrics"),
		new Algorithm("uk.ac.shef.wit.simmetrics.similaritymetrics.QGramsDistance", "Simmetrics"),
		new Algorithm("uk.ac.shef.wit.simmetrics.similaritymetrics.SmithWaterman", "Simmetrics"),
		new Algorithm("uk.ac.shef.wit.simmetrics.similaritymetrics.SmithWatermanGotoh", "Simmetrics"),
		new Algorithm("uk.ac.shef.wit.simmetrics.similaritymetrics.SmithWatermanGotohWindowedAffine", "Simmetrics"),
		new Algorithm("uk.ac.shef.wit.simmetrics.similaritymetrics.Soundex", "Simmetrics"),
		new Algorithm("uk.ac.shef.wit.simmetrics.similaritymetrics.TagLink", "Simmetrics"),
		new Algorithm("uk.ac.shef.wit.simmetrics.similaritymetrics.TagLinkToken", "Simmetrics")
	};
	
	private Algorithm(String className, String toolkit)
	{
		this.className = className;
		this.shortName = className.substring(className.lastIndexOf('.') + 1);
		this.toolkit = toolkit;
	}
	
	public String toString()
	{
		return shortName + " (" + toolkit + ")";
	}

	/**
	 * Get the name of the Java class that implements this algorithm.
	 * @return the class name
	 */
	public String getClassName() {
		return className;
	}
	
	/**
	 * Factory method to create an algorithm object for the specified class name
	 * @param className the name of the algorithm class to instantiate
	 * @return the Algorithm object, or null if the specified class is not supported
	 */
	public static final Algorithm forClassName(String className)
	{
		for (Algorithm a : SUPPORTED_ALGORITHMS)
		{
			if (className.equals(a.getClassName()))
			{
				return a;
			}
		}
		return null;
	}

}
