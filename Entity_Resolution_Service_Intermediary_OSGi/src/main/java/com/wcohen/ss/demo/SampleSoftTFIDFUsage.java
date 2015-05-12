package com.wcohen.ss.demo;

import com.wcohen.ss.*;
import com.wcohen.ss.api.*;
import com.wcohen.ss.tokens.*;

import java.util.*;

public class SampleSoftTFIDFUsage
{
    public static void main(String[] args)
    {
        // create a SoftTFIDF distance learner
        Tokenizer tokenizer = new SimpleTokenizer(false,true);
        double minTokenSimilarity = 0.8;
        SoftTFIDF distance = new SoftTFIDF(tokenizer,new JaroWinkler(),0.8);
        
        // train the distance on some strings - in general, this would
        // be a large corpus of existing strings, so that some
        // meaningful frequency estimates can be accumulated.  for
        // efficiency, you train on an iterator over StringWrapper
        // objects, which are produced with the 'prepare' function.

        String[] corpus = {"Yahoo Research", "Microsoft Research", "IBM Research", 
                           "Google Labs", "Bell Labs", "NEC Research Labs"};
        List list = new ArrayList();
        for (int i=0; i<corpus.length; i++) {
            list.add( distance.prepare(corpus[i]) );
        }
        distance.train( new BasicStringWrapperIterator(list.iterator()) );

        // now use the distance metric on some examples
        myCompare(distance, "Microsoft Labs", "Microsoft Research");
        myCompare(distance, "IBM Research", "Yahoo Research");
        myCompare(distance, "Microsoft Reseach", "Microsafe Research");
        myCompare(distance, "Google Labs", "Googel Research");
    }

    static void myCompare(SoftTFIDF distance, String s, String t)
    {
        // compute the similarity
        double d = distance.score(s,t);

        // print it out
        System.out.println("========================================");
        System.out.println("String s:  '"+s+"'");
        System.out.println("String t:  '"+t+"'");
        System.out.println("Similarity: "+d);

        // a sort of system-provided debug output
        System.out.println("Explanation:\n" + distance.explainScore(s,t));

        // this is equivalent to d, above, but if you compare s to
        // many strings t1, t2, ... it's a more efficient to only
        // 'prepare' s once.

        double e = distance.score( distance.prepare(s), distance.prepare(t) );
    }
}
