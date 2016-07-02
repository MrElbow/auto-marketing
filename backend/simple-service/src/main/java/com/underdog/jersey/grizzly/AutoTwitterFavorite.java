package com.underdog.jersey.grizzly;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;

/**
 * Root resource (exposed at "myresource" path)
 */
@Path("autotwitterfavorite")
public class AutoTwitterFavorite {

    /**
     * Method handling HTTP GET requests. The returned object will be sent
     * to the client as "text/plain" media type.
     *
     * @return String that will be returned as a text/plain response.
     * @throws TwitterException 
     */
    @POST
    /*@Produces(MediaType.TEXT_PLAIN)*/
    public String autoFavorite(@QueryParam("q") String queryString) throws TwitterException {
    	/*
    	// The factory instance is re-useable and thread safe.
        Twitter twitter = TwitterFactory.getSingleton();
        Query query = new Query("\"mi blog\"");
        //query.count(100);//Get 100 tweets
        
        QueryResult result = twitter.search(query);
               
        int counter = 0;
        long lastId	= 0;
        
        for (Status status : result.getTweets()) {
            System.out.println("---The entire status: "+status.toString());
            counter++;
            lastId	= status.getId();
            System.out.println("Counter: "+counter);
        }
		
        twitter.createFavorite(lastId);
		            	*/
        return "Got it!";        
    	//return query;
    }
}
