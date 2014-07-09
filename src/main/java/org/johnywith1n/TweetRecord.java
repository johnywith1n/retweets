package org.johnywith1n;

import org.joda.time.DateTime;

/**
 * Record of a retweet
 * 
 * @author johnylam
 *
 */
public class TweetRecord {

    /** Id of the original tweet */
    private final String   id;

    /** Time it was retweet */
    private final DateTime time;

    /** Text of the original */
    private final String   text;

    public TweetRecord ( String id, DateTime time, String text ) {
        this.id = id;
        this.time = time;
        this.text = text;
    }

    public String getId () {
        return id;
    }

    public DateTime getTime () {
        return time;
    }

    public String getText () {
        return text;
    }

}
