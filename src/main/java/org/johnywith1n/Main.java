package org.johnywith1n;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.joda.time.DateTime;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import com.google.common.collect.Multisets;

public class Main {
    private final static List<TweetRecord> retweets = Collections
                                                            .synchronizedList ( new ArrayList<TweetRecord> () );

    public static void main ( String[] args ) {
        try {
            new TwitterStream ( retweets );
        } catch (IOException e1) {
            System.out
                    .println ( "Unable to establish connection to twitter stream" );
            System.exit ( 1 );
        }

        BufferedReader br = new BufferedReader ( new InputStreamReader (
                System.in ) );
        String input;

        while (true) {
            try {
                input = br.readLine ();
                int n = Integer.parseInt ( input );
                getTopRetweets ( n );
            } catch (IOException ioe) {
                System.out.println ( "IO error trying to read input!" );
                System.exit ( 1 );
            } catch (NumberFormatException e) {
                System.out.println ( "Input must be a number" );
            }
        }
    }

    public static void getTopRetweets ( int n ) {
        DateTime start = new DateTime ().minusMinutes ( n );
        Multiset<String> counter = HashMultiset.create ();
        Map<String, String> idToTextMap = new HashMap<> ();

        synchronized (retweets) {
            int size = retweets.size ();
            int i = size - 1;

            while (i > -1 && retweets.get ( i ).getTime ().isAfter ( start )) {
                i--;
            }
            List<TweetRecord> retweetsInRange = retweets.subList ( i + 1, size );

            for (TweetRecord tweet : retweetsInRange) {
                idToTextMap.put ( tweet.getId (), tweet.getText () );
                counter.add ( tweet.getId () );
            }
        }

        Multiset<String> sortedCounter = Multisets
                .copyHighestCountFirst ( counter );

        int i = 0;

        for (String id : sortedCounter.elementSet ()) {
            if (i > 9)
                break;
            i++;
            System.out
                    .println ( i + ": Retweet Text: " + idToTextMap.get ( id ) );
            System.out.println ( "Count: " + sortedCounter.count ( id ) );
        }
        System.out.println ();
    }
}
