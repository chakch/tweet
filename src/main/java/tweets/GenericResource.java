/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tweets;

import java.net.URISyntaxException;
import java.util.Calendar;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.Produces;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PUT;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import tweets.tweet.Tweet;

import tweets.tweet.User;
import tweets.UserRepo;
/**
 * REST Web Service
 *
 * @author chaaben mohamed
 */
@Path("")
public class GenericResource {

    @Context
    private UriInfo context;

    /**
     * Creates a new instance of GenericResource
     */
    public GenericResource() {
    }

    /**
     * Retrieves representation of an instance of tweets.GenericResource
     * @return an instance of java.lang.String
     * @throws java.net.URISyntaxException
     * 
     */
   
   @GET
    @Path("users")
    @Produces(MediaType.TEXT_PLAIN)
    public String getUsers() throws URISyntaxException {
        User[] users =UserRepo.getUsers();
        JSONObject obj = new JSONObject();
        JSONArray arr = new JSONArray();

        for(int i = 0 ; i< users.length; i++)
        {
            obj.put("Name", users[i].getName());
             obj.put("Last Name", users[i].getLast());
            obj.put("Pseudo", users[i].getPseudo());
            obj.put("Mail", users[i].getMail());
            arr.add(obj);
           obj = new JSONObject();
        }
        return arr.toString();
    }

    /**
     * PUT method for updating or creating an instance of GenericResource
     * @param content representation for the resource
     * @return an HTTP response with content of the updated or created resource.
     */
    @POST
    @Path("users")
    @Consumes(MediaType.APPLICATION_JSON)
    public String addUser(String s) throws URISyntaxException 
    {   
        String[] store=s.replace("\"", " ").replace("{", " ").replace("}", " ").split(" , |:");
        System.out.println(store[5]);
       //  System.out.print(json.get("name"));
        User S=UserRepo.CreateUser(store[1].replaceAll(" ", ""),
                                    store[3].replaceAll(" ", ""),
                                    store[5].replaceAll(" ", "")
                                    ,store[7].replaceAll(" ", ""));
                                    System.out.println(S.toString());
                    return S.getPseudo()+"est crée" ;
         
    }
    @POST
    @Path("{Pseudo}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String addTweet(String s, @PathParam("Pseudo") String pseudo) throws URISyntaxException 
    {   
      String[] store=s.replace("\"", " ").replace("{", " ").replace("}", " ").split(" , |:");
      System.out.print(store[0]);
      System.out.print(store[1]);
      User s1 = UserRepo.getUser(pseudo); 
      store[0]=store[0].replace(" ", "");
      store[1]=store[1].replace(" ", "");
      if (store[0].equals("tweet")||store[0].equals("message")) {
           Tweet T=UserRepo.CreateTweet(store[1].replaceAll(" ", ""));
      s1.addTweet(T,"tweeted","{ \"date\" : \""+Calendar.getInstance().getTime().toString()+"\"}");
      return "tweet est ajouté avec succes";
     
      }
      if (store[0].equals("pseudo")) {
      
      User s2 = UserRepo.getUser(store[1]); 
      UserRepo.FollowUser(s1, s2);
      return "user est ajouté avec succes";
     
      }
      return "can't perform action please check your request";
      
      
      
    }

    @GET
    @Path("{Pseudo}")
    @Produces(MediaType.APPLICATION_JSON)
    public String getUser(@PathParam("Pseudo") String Pseudo) throws URISyntaxException{
        JSONObject obj = new JSONObject();
        JSONArray arr = new JSONArray();   
        User s = UserRepo.getUser(Pseudo);
        obj.put("Name", s.getName());
        obj.put("Last Name", s.getLast());
        obj.put("Pseudo", s.getPseudo());
        obj.put("Mail", s.getMail());
        arr.add(obj);
        return obj.toString();
    }
    @DELETE @Path("{Pseudo}")
   
    public String deleteRelation(@PathParam("Pseudo") String Pseudo, @Context UriInfo info) throws URISyntaxException { 
        boolean a=UserRepo.deleteRelation(UserRepo.getUser(Pseudo),UserRepo.getUser(info.getQueryParameters().getFirst("dPseudo")));
         if (a) return "relation deleted";
            return "no relation Between"+Pseudo+ info.getQueryParameters().getFirst("dPseudo");
    }
    
    @GET
    @Path("{Pseudo}/tweets")
    @Produces("application/json")
    public String getTweet(@PathParam("Pseudo") String Pseudo) throws URISyntaxException{
        Tweet[] T=UserRepo.GetTweets(UserRepo.getUser(Pseudo));
              
        JSONObject obj = new JSONObject();
        JSONArray arr = new JSONArray();
        
        for(int i = 0 ; i< T.length; i++)
        {
            obj.put("Tweet", T[i].getmMessage());
            arr.add(obj);
           obj = new JSONObject();
        }
        return arr.toString();
   
    }

    @GET
    @Path("{Pseudo}/folowers")
    @Produces(MediaType.TEXT_PLAIN)
    public String getFollowed(@PathParam("Pseudo") String Pseudo) throws URISyntaxException {
        User[] users =UserRepo.GetFollowed(UserRepo.getUser(Pseudo));
        JSONObject obj = new JSONObject();
        JSONArray arr = new JSONArray();

        for(int i = 0 ; i< users.length; i++)
        {
            
           
            obj.put("Pseudo", users[i].getPseudo());
            obj.put("Name", users[i].getName());
             obj.put("Mail", users[i].getMail());
            obj.put("Last Name", users[i].getLast());
            arr.add(obj);
           obj = new JSONObject();
        }
        return arr.toString();
    }
    
    @GET
    @Path("{Pseudo}/followings")
    @Produces(MediaType.TEXT_PLAIN)
    public String getFollowing(@PathParam("Pseudo") String Pseudo) throws URISyntaxException {
        User[] users =UserRepo.GetFollowing(UserRepo.getUser(Pseudo));
        JSONObject obj = new JSONObject();
        JSONArray arr = new JSONArray();

        for(int i = 0 ; i< users.length; i++)
        {
            obj.put("Name", users[i].getName());
             obj.put("Last Name", users[i].getLast());
            obj.put("Pseudo", users[i].getPseudo());
            obj.put("Mail", users[i].getMail());
            arr.add(obj);
           obj = new JSONObject();
        }
        return arr.toString();
    }
}
