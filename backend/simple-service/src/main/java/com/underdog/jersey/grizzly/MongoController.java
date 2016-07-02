package com.underdog.jersey.grizzly;


/**
 * Root resource (exposed at "myresource" path)
 */
public class MongoController {

    /**
     * Method handling HTTP GET requests. The returned object will be sent
     * to the client as "text/plain" media type.
     *
     * @return String that will be returned as a text/plain response.
     * @throws TwitterException 
     */
    
    
    public String autoFavorite(String queryString) {
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
