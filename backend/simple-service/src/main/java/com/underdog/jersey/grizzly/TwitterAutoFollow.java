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
@Path("twitterautofollow")
public class TwitterAutoFollow {

	TwitterMongoController mongoController;
	Twitter twitter;
	Logger logger = Logger.getLogger("MyLog");  
    FileHandler fh;
    
    int MIN_FOLLOWERS_COUNT = 100;
	int MIN_FRIENDS_COUNT = 100;
	int FOLLOWERS_DIVISION = 10;
	int MIN_RT_COUNT = 10;
	
	public TwitterAutoFollow(){
		mongoController = new TwitterMongoController();
		twitter = TwitterFactory.getSingleton();
		
	    try {  

	        // This block configure the logger with handler and formatter  
	        fh = new FileHandler("logs/twitter-auto-follow.log");  
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
    public String autoFollow(@QueryParam("q") String queryString) throws TwitterException, ParseException {
    	
    	// The factory instance is re-useable and thread safe.
        
        Query query = new Query(queryString);
        query.count(10);//Get 100 tweets
                
        logger.info("The query: "+query);
        
        QueryResult result = twitter.search(query);
        
               
        int counter = 0;
        int insertedUsersCount	= 0;
        int alreadyFavoritedStats = 0;
        String followRequestStatus = "";
        
        for (Status status : result.getTweets()) {
        	
        	if(status.isFavorited()){
	        	alreadyFavoritedStats++;
        	}
        	else{
        		followRequestStatus = followUserFromStatus(status.getUser());
	        	if(followRequestStatus.equals("inserted")){
	        		insertedUsersCount++;
	        	}
	        	else if(followRequestStatus.equals("429")){
	        		break;
	        	}
        	}
        		
            counter++;
        }
                
        String outputStr	= "";
        
        outputStr += "Total count: "+counter;
        outputStr += ", Inserted users count: "+insertedUsersCount;
        outputStr += ", Already favorited stats countS: "+alreadyFavoritedStats;
        
        if(followRequestStatus.equals("429")){
        	outputStr += ", TWITTER REQUEST LIMIT REACHED";        	
        }
        
        //System.out.println(outputStr);
        logger.info(outputStr);
        return outputStr;        
    	//return query;
    }
    
    
    private String followUserFromStatus(User user){
    	logger.info("--------------------The user: @"+user.getScreenName());
    	
    	String code = "200";
    	
    	if(mongoController.isFollowedUser(user.getId()) || mongoController.isUnfollowedUser(user.getId())){
    		code = "repeated"; //Previously followed user
    	}
    	else if(user.isDefaultProfileImage()){
    		code = "spammy"; //If default profile image, then is probably spammy
    	}
    	else if(user.isProtected()){
    		code = "protected"; //Doesn't work for marketing purposes
    	}
    	else if((user.getFollowersCount() < MIN_FOLLOWERS_COUNT) && (user.getFollowersCount() < MIN_FRIENDS_COUNT)){
    		code = "small"; //The account is too small
    	}
    	/*
    	else if((user.getFollowersCount()/FOLLOWERS_DIVISION) > user.getFriendsCount()){
    		code = "lack"; //Lack of following from this account
    	}*/
    	else{
    		if(meetFollowCriteria(user)){
    			code = followUser(user);
    		}
    		else{
    			code = "needrt"; //need more RTs    			    			
    		}    		
    	}
    	    	
    	return code;
    }
    
    
    private String followUser(User user){

    	String code = "inserted";
    	
		try {
			twitter.createFriendship(user.getId());
			mongoController.addFollow(user.getId(), user.getScreenName());
		} catch (TwitterException e) {
			
			Pattern pattern = Pattern.compile("code: (\\d+) for URL");  
            Matcher matcher = pattern.matcher(e.toString());  
            
            while (matcher.find()) {
            	code = matcher.group(1);
            }  
			
			System.out.println("Error creating friendship on twitter: "+e);
		}    		
	    	
    	return code;
    }
    
    
    private boolean meetFollowCriteria(User user){
    	boolean meetFollowCriteria = false;
    	Paging paging = new Paging(1, 200);
    	ResponseList<Status> response;
    	int totalCount = 0;
    	int retweetCount = 0;
    	
    	try {
    		logger.info("The user id is: "+user.getId());
    		response = twitter.getUserTimeline(user.getId(), paging);    		
    		for (Status status : response){
    			if(status.isRetweet()){
    				retweetCount++;
    			}
    			totalCount++;
    		}
    		
		} catch (TwitterException e) {
			// TODO Auto-generated catch block
			logger.info("ERROR getUserListStatuses exception: "+e);
		}
    	
    	if((totalCount > 100) && (retweetCount > MIN_RT_COUNT)){
    		meetFollowCriteria = true;
    		logger.info("MEET follow criteria. Stats count: "+totalCount+", retweet count: "+retweetCount);
    	}
    	else{
    		logger.info("Does NOT meet follow criteria. Stats count: "+totalCount+", retweet count: "+retweetCount);
    	}
    	
    	return meetFollowCriteria;
    }
    
}
