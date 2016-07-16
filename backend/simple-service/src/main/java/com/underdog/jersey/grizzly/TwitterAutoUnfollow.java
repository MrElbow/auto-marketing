package com.underdog.jersey.grizzly;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
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

import twitter4j.Friendship;
import twitter4j.IDs;
import twitter4j.Paging;
import twitter4j.Query;
import twitter4j.Query.ResultType;
import twitter4j.QueryResult;
import twitter4j.RateLimitStatus;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;

/**
 * Root resource (exposed at "myresource" path)
 */
@Path("twitterautounfollow")
public class TwitterAutoUnfollow {

	TwitterMongoController mongoController;
	Twitter twitter;
	Logger logger = Logger.getLogger("MyLog");  
    FileHandler fh;
    
    
	public TwitterAutoUnfollow(){
		mongoController = new TwitterMongoController();
		twitter = TwitterFactory.getSingleton();
		
	    try {  

	        // This block configure the logger with handler and formatter  
	        fh = new FileHandler("logs/twitter-auto-unfollow.log");  
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
    public String autoUnfollow(@QueryParam("count") int unfollowCount) throws TwitterException, ParseException {
                
        long cursor = -1;
        IDs ids = twitter.getFriendsIDs(twitter.getId(), cursor, 100);
              
        
        ResponseList<Friendship> friendships = twitter.lookupFriendships(ids.getIDs());
        int notFollowedBackCount = 0;
        
        List<Long> notBackFollowers = new ArrayList<Long>();
        
        for (Friendship friendship : friendships) {
        	System.out.println("Name: "+friendship.getScreenName());
        	if(friendship.isFollowedBy()){
        		System.out.println("isFollowedBy"); //Keep
        	}
        	else{
        		System.out.println("isNOTFollowedBy"); //Remove
        		notBackFollowers.add(friendship.getId());
        		notFollowedBackCount++;
        	}
        }
        
        //To get the last not back followers
        List<Long> usersToUnfollow = notBackFollowers.subList(Math.max(notBackFollowers.size() - unfollowCount, 0), notBackFollowers.size());
        
        int unfollowedCount = 0;
        String unfollowStatus = "";
        
        for(long userToUnfollow : usersToUnfollow){
        	User user = twitter.showUser(userToUnfollow);
        	
        	unfollowStatus = unfollowUser(user); 
        	
        	if(unfollowStatus.equals("unfollowed")){
        		logger.info("Removed the user: "+user.getScreenName());
        		unfollowedCount++;
        	}
        	else if(unfollowStatus.equals("429")){
        		logger.info("ERROR - Unfollow limit reached");
        		break;
        	}
        	else{
        		System.out.println("ERROR - Error doing the unfollow: "+unfollowStatus+" with the user "+user.getScreenName());
        	}
        	        	
        }
                
        String outputStr = "Amount of people to unfollow by parameter in request: "+unfollowCount;
        outputStr += ", Unfollow count: "+unfollowedCount;
        outputStr += ", People that I was following that are not following me back: "+notFollowedBackCount;
        
        System.out.println(outputStr);
        logger.info(outputStr);
        
        return outputStr;
    }
    
    
    private String unfollowUser(User user){

    	String code = "unfollowed";
    	
		try {
			twitter.destroyFriendship(user.getId());
			mongoController.addUnfollow(user.getId(), user.getScreenName());
		} catch (TwitterException e) {
			
			Pattern pattern = Pattern.compile("code: (\\d+) for URL");  
            Matcher matcher = pattern.matcher(e.toString());  
            
            while (matcher.find()) {
            	code = matcher.group(1);
            }  
			
			System.out.println("Error destroying friendship on twitter: "+e);
		}    		
	    	
    	return code;
    }
    
    
}
