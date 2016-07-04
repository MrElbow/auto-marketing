package com.underdog.jersey.grizzly;

import java.text.ParseException;
import java.util.Date;
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
import twitter4j.User;

/**
 * Root resource (exposed at "myresource" path)
 */
@Path("autotwitterfavorite")
public class AutoTwitterFavorite {

	TwitterMongoController mongoController;
	Twitter twitter;
	
	public AutoTwitterFavorite(){
		mongoController = new TwitterMongoController();
		twitter = TwitterFactory.getSingleton();
	}
    /**
     * Method handling HTTP GET requests. The returned object will be sent
     * to the client as "text/plain" media type.
     *
     * @return String that will be returned as a text/plain response.
     * @throws TwitterException 
     * @throws ParseException 
     */
    @POST
    /*@Produces(MediaType.TEXT_PLAIN)*/
    public String autoFavorite(@QueryParam("q") String queryString) throws TwitterException, ParseException {
    	
    	// The factory instance is re-useable and thread safe.
        
        Query query = new Query(queryString);
        query.count(100);//Get 100 tweets
        
        QueryResult result = twitter.search(query);
               
        int counter = 0;
        int insertedFavoritedUsers	= 0;
        
        for (Status status : result.getTweets()) {
        	if(favoriteUserFromStatus(status)){
        		insertedFavoritedUsers++;
        	}
        	
            counter++;
        }
        
        
        System.out.println("Total count: "+counter);
        System.out.println("Favorited count: "+insertedFavoritedUsers);
        
        return "Got it!";        
    	//return query;
    }
    
    
    private boolean favoriteUserFromStatus(Status status){

    	boolean inserted = false;
    	User user = status.getUser();
    	
    	if((!mongoController.isFavoritedUser(user.getId())) && (!status.isFavorited())){
    		try {
				twitter.createFavorite(status.getId());
				mongoController.addFavorite(user.getId(), user.getScreenName());
	    		inserted = true;
			} catch (TwitterException e) {
				System.out.println("Error creating favorite on twitter: "+e);
			}    		
    	}
    	    	
    	return inserted;
    }
    
}
