package com.underdog.jersey.grizzly;

import org.bson.Document;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

import static java.util.Arrays.asList;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;

/**
 * Root resource (exposed at "myresource" path)
 */
public class TwitterMongoController {

	MongoClient mongoClient;
	MongoDatabase db;
	
	public TwitterMongoController(){
		mongoClient = new MongoClient();
		db = mongoClient.getDatabase("twitter");
	}
	
    /**
     * Method handling HTTP GET requests. The returned object will be sent
     * to the client as "text/plain" media type.
     *
     * @return String that will be returned as a text/plain response.
     * @throws TwitterException 
     */
    
    
    public String autoFavorite(String queryString) {
    	/*DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ENGLISH);
    	db.getCollection("favoritedUsers").insertOne(
    	        new Document("userId","32103983120812038102381023802138")
    	                .append("user", "testUser")
    	                .append("date", format.parse("2014-10-01T00:00:00Z")));*/
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
