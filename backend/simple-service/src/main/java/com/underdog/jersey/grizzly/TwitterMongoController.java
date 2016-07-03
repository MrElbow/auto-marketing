package com.underdog.jersey.grizzly;

import org.bson.Document;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static java.util.Arrays.asList;

import com.mongodb.Block;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

/**
 * Root resource (exposed at "myresource" path)
 */
public class TwitterMongoController {

	MongoClient mongoClient;
	MongoDatabase db;
	MongoCollection<Document> favoritedUsers;
	
	public TwitterMongoController(){
		mongoClient = new MongoClient();
		db = mongoClient.getDatabase("twitter");
		favoritedUsers	= db.getCollection("favoritedUsers");
	}
	
    /**
     * Method handling HTTP GET requests. The returned object will be sent
     * to the client as "text/plain" media type.
     *
     * @return String that will be returned as a text/plain response.
     * @throws ParseException 
     * @throws TwitterException 
     */
    
    
    public void addFavorite(long userId, String userScreenName) {
    	
    	favoritedUsers.insertOne(
    	        new Document("userId", userId)
    	                .append("userScreenName", userScreenName)
    	                .append("lastFavoritedDate", new Date()));
    	
    }    
    
    
    public boolean isFavoritedUser(long userId){
    	boolean isFavorite	= true;
    	if(favoritedUsers.count(new Document("userId", userId)) < 1){
    		isFavorite = false;
    	}
    	/*FindIterable<Document> results = favoritedUsers.find(new Document("userId", userId));
    	    	
    	results.forEach(new Block<Document>() {
    	    @Override
    	    public void apply(final Document document) {
    	        System.out.println(document);
    	    }
    	});*/    	
    	return isFavorite;
    }
    
    
    public boolean updateOneUser(long userId){
    	boolean updated	= false;
    	//favoritedUsers.updateOne(new Document("userId", userId), new Document("$currentDate", new Document("lastFavoriteFoundDate", true)))
    	return updated;
    }
        
}
