package com.underdog.jersey.grizzly;

import java.io.IOException;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.RateLimitStatus;
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
	Logger logger = Logger.getLogger("MyLog");  
    FileHandler fh;  
	
	public AutoTwitterFavorite(){
		mongoController = new TwitterMongoController();
		twitter = TwitterFactory.getSingleton();
		
	    try {  

	        // This block configure the logger with handler and formatter  
	        fh = new FileHandler("logs/twitter-auto-favorite.log");  
	        logger.addHandler(fh);
	        SimpleFormatter formatter = new SimpleFormatter();  
	        fh.setFormatter(formatter);  

	        // the following statement is used to log any messages  
	        logger.info("Start log...");  

	    } catch (SecurityException e) {  
	        e.printStackTrace();  
	    } catch (IOException e) {  
	        e.printStackTrace();  
	    }  

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
        
        logger.info("The query: "+query);
        
        QueryResult result = twitter.search(query);
               
        int counter = 0;
        int insertedFavoritedUsers	= 0;
        int alreadyFavoritedAccount = 0;
        String favoriteRequestStatus = "200";
        
        for (Status status : result.getTweets()) {
        	favoriteRequestStatus = favoriteUserFromStatus(status);
        	
        	System.out.println("Favorited request status: "+status);
        	
        	if(favoriteRequestStatus.equals("200")){
        		insertedFavoritedUsers++;
        	}
        	else if(favoriteRequestStatus.equals("302")){
        		alreadyFavoritedAccount++;
        	}
        	else{        		
        		break;
        	}
        		
            counter++;
        }
        
        if(favoriteRequestStatus.equals("200")){
        	System.out.println("Finished successfully");
        }
        else if(favoriteRequestStatus.equals("429")){
        	logger.info("Twitter favorites limit reached");
        	System.out.println(new PrettyPrintingMap<String, RateLimitStatus>(twitter.getRateLimitStatus()));
        }
        else if(favoriteRequestStatus.equals("302")){
        	System.out.println("Found in the favorited accounts");
        }
        else{
        	logger.warning("Favorited request returned error code: "+favoriteRequestStatus);
        }
        
        
        String outputStr	= "";
        
        outputStr += "Total count: "+counter;
        outputStr += ", Favorited count: "+insertedFavoritedUsers;
        outputStr += ", Already favorited account count: "+alreadyFavoritedAccount;
        
        //System.out.println(outputStr);
        logger.info(outputStr);
        return outputStr;        
    	//return query;
    }
    
    
    private String favoriteUserFromStatus(Status status){

    	String code = "200";
    	User user = status.getUser();
    	
    	if((!mongoController.isFavoritedUser(user.getId())) && (!status.isFavorited())){
    		try {
				twitter.createFavorite(status.getId());
				mongoController.addFavorite(user.getId(), user.getScreenName());
			} catch (TwitterException e) {
				
				Pattern pattern = Pattern.compile("code: (\\d+) for URL");  
	            Matcher matcher = pattern.matcher(e.toString());  
	            
	            while (matcher.find()) {
	            	code = matcher.group(1);
	            }  
				
				System.out.println("Error creating favorite on twitter: "+e);
			}    		
    	}
    	else{
    		code = "302"; //Found
    	}
    	    	
    	return code;
    }
    
}
