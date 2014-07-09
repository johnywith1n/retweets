package org.johnywith1n;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.twitter.hbc.ClientBuilder;
import com.twitter.hbc.core.Client;
import com.twitter.hbc.core.Constants;
import com.twitter.hbc.core.Hosts;
import com.twitter.hbc.core.HttpHosts;
import com.twitter.hbc.core.endpoint.StatusesSampleEndpoint;
import com.twitter.hbc.core.processor.StringDelimitedProcessor;
import com.twitter.hbc.httpclient.auth.Authentication;
import com.twitter.hbc.httpclient.auth.OAuth1;

/**
 * Creates a connection to the twitter stream and consumes retweets
 * 
 * @author johnylam
 *
 */
public class TwitterStream {
    private final ObjectMapper      mapper = new ObjectMapper ();
    private final DateTimeFormatter fmt    = DateTimeFormat
                                                   .forPattern ( "EEE MMMM dd HH:mm:ss Z YYYY" );

    public TwitterStream ( final List<TweetRecord> list )
            throws IOException {
        final BlockingQueue<String> msgQueue = new LinkedBlockingQueue<String> (
                100000 );

        Hosts hosebirdHosts = new HttpHosts ( Constants.STREAM_HOST );
        StatusesSampleEndpoint hosebirdEndpoint = new StatusesSampleEndpoint ();
        Properties prop = new Properties ();
        InputStream input = TwitterStream.class.getClassLoader ()
                .getResourceAsStream ( "app.properties" );
        prop.load ( input );
        input.close ();

        Authentication hosebirdAuth = new OAuth1 (
                prop.getProperty ( "consumerKey" ),
                prop.getProperty ( "consumerSecret" ),
                prop.getProperty ( "token" ), prop.getProperty ( "secret" ) );

        ClientBuilder builder = new ClientBuilder ()
                .name ( "Hosebird-Client-01" ).hosts ( hosebirdHosts )
                .authentication ( hosebirdAuth ).endpoint ( hosebirdEndpoint )
                .processor ( new StringDelimitedProcessor ( msgQueue ) );

        final Client hosebirdClient = builder.build ();
        hosebirdClient.connect ();

        Thread thread = new Thread ( new Runnable () {
            @Override
            public void run () {
                while (!hosebirdClient.isDone ()) {
                    String msg;
                    try {
                        msg = msgQueue.take ();

                        Map<String, Object> tweet = mapper.readValue ( msg,
                                Map.class );

                        if (tweet.containsKey ( "retweeted_status" )) {
                            Map<String, Object> originalTweet = (Map<String, Object>) tweet
                                    .get ( "retweeted_status" );
                            String id = (String) originalTweet.get ( "id_str" );
                            String text = (String) originalTweet.get ( "text" );
                            DateTime time = fmt.parseDateTime ( (String) tweet
                                    .get ( "created_at" ) );
                            list.add ( new TweetRecord ( id, time, text ) );
                        }
                    } catch (InterruptedException | IOException e) {
                        e.printStackTrace ();
                        System.exit ( 1 );
                    }
                }
            }
        } );
        thread.start ();
    }

}
