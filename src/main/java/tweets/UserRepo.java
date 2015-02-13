/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tweets;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import tweets.tweet.Tweet;
import tweets.tweet.User;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.core.MediaType;

/**
 *
 * @author chaaben mohamed
 */
public class  UserRepo {
        public static final String BASE_URI  = "http://localhost:7474/db/data/";

    public static User CreateUser(String mName,String mLast,String mMail , String mPseudo) throws URISyntaxException{
                final String nodeEntryPointUri = BASE_URI + "node";
       // http://localhost:7474/db/data/node
                
       if (Unique(mMail)==-1){
       WebResource resource = Client.create().resource( nodeEntryPointUri );
       ClientResponse response = resource.accept( MediaType.APPLICATION_JSON )
                                            .type( MediaType.APPLICATION_JSON )
                                            .entity( "{}" )
                                            .post( ClientResponse.class );
       
       final URI location = response.getLocation();
      
        addProperty(location, "name", mName);
        addProperty(location, "LastName", mLast);
        addProperty(location, "Email", mMail);
         addProperty(location, "Pseudo", mPseudo);
        Createlabels(location, "user");
        // System.out.print(response.getStatus());
       User s= new User(mName, mLast, mMail, location,mPseudo);
        response.close();
       return s;
       }
       else {
           return null;
       }

    }
    public static Tweet CreateTweet(String mMessage){
                final String nodeEntryPointUri = BASE_URI + "node";
       // http://localhost:7474/db/data/node
       WebResource resource = Client.create()
                               .resource( nodeEntryPointUri );
       ClientResponse response = resource.accept( MediaType.APPLICATION_JSON )
                                            .type( MediaType.APPLICATION_JSON )
                                            .entity( "" )
                                            .post( ClientResponse.class );
       final URI location = response.getLocation();
       addProperty(location, "Message", mMessage);
       Createlabels(location, "tweet");
       Tweet s= new Tweet(mMessage,location);
       
        response.close();
       return s;

    }
    public static boolean FollowUser(User S1,User S2) throws URISyntaxException{
           if (checkRelation(S1, S2)) {
                  URI fromUri = new URI( S1.getUri().toString() + "/relationships" );
                  
                  String relationshipJson = generateJsonRelationship( S2.getUri(),
                          "Follows", "{ \"date\" : \""+Calendar.getInstance().getTime().toString()+"\"}");

                  WebResource resource = Client.create()
                          .resource( fromUri );
                  // POST JSON to the relationships URI
                  ClientResponse response = resource.accept( MediaType.APPLICATION_JSON )
                          .type( MediaType.APPLICATION_JSON )
                          .entity( relationshipJson )
                          .post( ClientResponse.class );

                  final URI location = response.getLocation();
                  //System.out.println( String.format(
                  //        "POST to [%s], status code [%d], location header [%s]",
                  //        fromUri, response.getStatus(), location.toString() ) );

                  response.close();

                    return true ;
              
           }
           else return false;
    }    
    private static String generateJsonRelationship( URI endNode, String relationshipType, String... jsonAttributes ) {
        StringBuilder sb = new StringBuilder();
        sb.append( "{ \"to\" : \"" );
        sb.append( endNode.toString() );
        sb.append( "\", " );

        sb.append( "\"type\" : \"" );
        sb.append( relationshipType );
        if ( jsonAttributes == null || jsonAttributes.length < 1 )
        {
            sb.append( "\"" );
        }
        else
        {
            sb.append( "\", \"data\" : " );
            for ( int i = 0; i < jsonAttributes.length; i++ )
            {
                sb.append( jsonAttributes[i] );
                if ( i < jsonAttributes.length - 1 )
                { // Miss off the final comma
                    sb.append( ", " );
                }
            }
        }

        sb.append( " }" );
        return sb.toString();
    }
    public static User[] getUsers() throws URISyntaxException   {
        URI fromUri = new URI( "http://localhost:7474/db/data/cypher" );
    

    WebResource resource = Client.create()
                .resource( fromUri );
    String cypher= "{\n" +
                    "  \"query\" : \"MATCH (n:user) RETURN n.name, n.LastName,n.Email ,id(n), n.Pseudo \",\n" +
                    "  \"params\" : {\n" +  
                    "  }\n" +
                    "}";
  
    // POST JSON to the relationships URI
    ClientResponse response = resource.accept( MediaType.APPLICATION_JSON )
                                .type( MediaType.APPLICATION_JSON )
                                .entity(cypher)
                                .post( ClientResponse.class );
       String fileContent=response.getEntity(String.class);                    
       // System.out.println(fileContent);
    response.close(); 
    GsonBuilder builder = new GsonBuilder();
   
    
        builder.registerTypeAdapter(User[].class, new JsonDeserializer<User[]>(){
               
                
            @Override
            public User[] deserialize(JsonElement je, Type type, JsonDeserializationContext jdc) throws JsonParseException {
                
               List<User> s=new ArrayList<>();
               // System.out.println(je.getAsJsonObject().get("data").getAsJsonArray().get(1));
               // JsonArray coordinatesArray = fields.getAsJsonArray();
                
                for (int i=0; i<je.getAsJsonObject().get("data").getAsJsonArray().size();i++)
                {
                     
                     String[] parts = je.getAsJsonObject().get("data").getAsJsonArray().get(i).toString().replace("\"", "").replace("[", "").replace("]", "").split(","); 
                    try {
                        s.add(new User(parts[0],parts[1],parts[2],new URI(BASE_URI+"node/"+parts[3]),parts[4] ));
                        
                        
                    } catch (URISyntaxException ex) {
                        Logger.getLogger(UserRepo.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }

                return   s.toArray(new User[s.size()]) ;
            }
        });     
        Gson gson = builder.create();
             
         User[] S = gson.fromJson(fileContent, User[].class);
         return S;
    }
    public static void addProperty(URI nodeUri, String propertyName , String propertyValue )    {
        
    
            String propertyUri = nodeUri.toString() + "/properties/" + propertyName;
        // http://localhost:7474/db/data/node/{node_id}/properties/{property_name}

        WebResource resource = Client.create()
                .resource( propertyUri );
        ClientResponse response = resource.accept( MediaType.APPLICATION_JSON )
                .type( MediaType.APPLICATION_JSON )
                .entity( "\"" + propertyValue + "\"" )
                .put( ClientResponse.class );

      //  System.out.println( String.format( "PUT to [%s], status code [%d]",
         //       propertyUri, response.getStatus() ) );
        response.close();
            }
    private static void Createlabels(URI location, String user) {
         String propertyUri = location.toString() + "/labels/"  ;
       //  System.out.println(propertyUri);
        WebResource resource = Client.create().resource( propertyUri );
        ClientResponse response = resource.accept( MediaType.APPLICATION_JSON )
                                            .type( MediaType.APPLICATION_JSON )
                                            .entity( "\""+user+"\"" )
                                            .post( ClientResponse.class );
        // System.out.println(response.getStatus());
        response.close();
    }
    private static int Unique(String mMail) throws URISyntaxException {
       URI fromUri = new URI( "http://localhost:7474/db/data/cypher" );
    

    WebResource resource = Client.create()
                .resource( fromUri );
    String cypher= "{\n" +
                    "  \"query\" : \"MATCH (n:user) WHERE n.Email='"+mMail+"' RETURN n\",\n" +
                    "  \"params\" : {\n" +  
                    "  }\n" +
                    "}";
  
    // POST JSON to the relationships URI
    ClientResponse response = resource.accept( MediaType.APPLICATION_JSON )
                                .type( MediaType.APPLICATION_JSON )
                                .entity(cypher)
                                .post( ClientResponse.class );
    
    String text =response.getEntity(String.class);
    String word = mMail;
   // System.out.println(text+word);
   // System.out.println("find"+text.indexOf(word));
    response.close();
      return text.indexOf(word);
     //  System.out.print(response.getEntity(String.class));
     
        
    }
    public static User getUser(String Pseudo) throws URISyntaxException   {    
       
       
       URI fromUri = new URI( "http://localhost:7474/db/data/cypher" );
       String fileContent = null;  
        WebResource resource = Client.create()
                    .resource( fromUri );
        String cypher= "{\n" +
                        "  \"query\" : \"MATCH (n:user) WHERE n.Pseudo='"+Pseudo+"' RETURN n.name, n.Email, n.LastName, id(n), n.Pseudo \",\n" +
                        "  \"params\" : {\n" +  
                        "  }\n" +
                        "}";

        // POST JSON to the relationships URI
        ClientResponse response = resource.accept( MediaType.APPLICATION_JSON )
                                    .type( MediaType.APPLICATION_JSON )
                                    .entity(cypher)
                                    .post( ClientResponse.class );

        //System.out.println(response.getEntity(String.class));
        fileContent=response.getEntity(String.class);
        
        response.close(); 
       
              
   
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(User.class, new JsonDeserializer<User>(){

            @Override
            public User deserialize(JsonElement je, Type type, JsonDeserializationContext jdc) throws JsonParseException {
                User s=null; 
                try {
                JsonElement fields = je.getAsJsonObject().get("data"); 
                //System.out.println(je.getAsJsonObject());
                JsonArray coordinatesArray = fields.getAsJsonArray();      
                System.out.println(coordinatesArray.get(0));
                String[] parts = coordinatesArray.get(0).toString().replace("\"", "").replace("[", "").replace("]", "").split(",");
                             
                
                    s = new User(parts[0],parts[2],parts[1],new URI(BASE_URI+"node/"+parts[3]),parts[4]);
                    
                } catch (URISyntaxException | IndexOutOfBoundsException ex) {
                    Logger.getLogger(UserRepo.class.getName()).log(Level.SEVERE, null, ex);
                }
               
                return s;
            }
        });     
        Gson gson = builder.create();
        User s = gson.fromJson(fileContent, User.class);
        return s;
        
       
        
    }
    public static boolean deleteRelation(User s1,User s2) throws URISyntaxException    { 
       URI fromUri = new URI( "http://localhost:7474/db/data/cypher" );
      
        WebResource resource = Client.create()
                    .resource( fromUri );
      String cypher= "{\n" +
                        "  \"query\" : \"MATCH (n { Email: '"+s1.getMail()+"' })-[r]->({ Email: '"+s2.getMail()+"' })  delete  r \",\n" +
                        "  \"params\" : {\n" +  
                        "  }\n" +
                        "}"; ;
        
       

        // POST JSON to the relationships URI
        ClientResponse response = resource.accept( MediaType.APPLICATION_JSON )
                                    .type( MediaType.APPLICATION_JSON )
                                    .entity(cypher)
                                    .post( ClientResponse.class );
        System.out.println(cypher);
        System.out.println("hello");    
        System.out.println(response.getEntity(String.class));
           
        response.close(); 
       
        return true;
    }
    public static boolean checkRelation(User s1,User s2) throws URISyntaxException    { 
       URI fromUri = new URI( "http://localhost:7474/db/data/cypher" );
      
        WebResource resource = Client.create()
                    .resource( fromUri );
      String cypher= "{\n" +
                        "  \"query\" : \"MATCH (n { Email: '"+s1.getMail()+"' })-[r]->({ Email: '"+s2.getMail()+"' })  return  r \",\n" +
                        "  \"params\" : {\n" +  
                        "  }\n" +
                        "}"; ;
        
       

        // POST JSON to the relationships URI
        ClientResponse response = resource.accept( MediaType.APPLICATION_JSON )
                                    .type( MediaType.APPLICATION_JSON )
                                    .entity(cypher)
                                    .post( ClientResponse.class );   
         String text =response.getEntity(String.class);
         String word = "Follows"; 
         
        response.close(); 
       if (text.indexOf(word)==-1) return true;
       else  return false;
    }
    public static Tweet[] GetTweets(User S) throws URISyntaxException    { 
       URI fromUri = new URI( "http://localhost:7474/db/data/cypher" );
      
        WebResource resource = Client.create()
                    .resource( fromUri );
      String cypher= "{\n" +
                        "  \"query\" : \" MATCH (n:user { Pseudo:'"+ S.getPseudo()+"' })--(t:tweet) RETURN t.Message, id(t) \",\n" +
                        "  \"params\" : {\n" +  
                        "  }\n" +
                        "}"; ;
        
       

        // POST JSON to the relationships URI
        ClientResponse response = resource.accept( MediaType.APPLICATION_JSON )
                                    .type( MediaType.APPLICATION_JSON )
                                    .entity(cypher)
                                    .post( ClientResponse.class );   
           
         String fileContent=response.getEntity(String.class);                    
       // System.out.println(fileContent);
    response.close(); 
    GsonBuilder builder = new GsonBuilder();
   
    
        builder.registerTypeAdapter(Tweet[].class, new JsonDeserializer<Tweet[]>(){
               
                
            @Override
            public Tweet[] deserialize(JsonElement je, Type type, JsonDeserializationContext jdc) throws JsonParseException {
                
               List<Tweet> s=new ArrayList<>();
               // System.out.println(je.getAsJsonObject().get("data").getAsJsonArray().get(1));
               // JsonArray coordinatesArray = fields.getAsJsonArray();
                
                for (int i=0; i<je.getAsJsonObject().get("data").getAsJsonArray().size();i++)
                {
                     
                     String[] parts = je.getAsJsonObject().get("data").getAsJsonArray().get(i).toString().replace("\"", "").replace("[", "").replace("]", "").split(","); 
                    try {
                        s.add(new Tweet(parts[0],new URI("http://localhost:7474/db/data/node/"+parts[1])));
                        
                        
                    } catch (URISyntaxException ex) {
                        Logger.getLogger(UserRepo.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }

                return   s.toArray(new Tweet[s.size()]) ;
            }
        });     
        
        Gson gson = builder.create();   
        Tweet[] M = gson.fromJson(fileContent, Tweet[].class);
        return M;

    }
    public static User[] GetFollowing(User S) throws URISyntaxException    { 
       URI fromUri = new URI( "http://localhost:7474/db/data/cypher" );
      
        WebResource resource = Client.create()
                    .resource( fromUri );
      String cypher= "{\n" +
                        "  \"query\" : \" MATCH (n:user{ Pseudo: '"+S.getPseudo()+"'})-[labels:Follows]->(m) RETURN m.name, m.LastName, m.Email, id(m), m.Pseudo  \",\n" +
                        "  \"params\" : {\n" +  
                        "  }\n" +
                        "}"; ;
        
       

        // POST JSON to the relationships URI
        ClientResponse response = resource.accept( MediaType.APPLICATION_JSON )
                                    .type( MediaType.APPLICATION_JSON )
                                    .entity(cypher)
                                    .post( ClientResponse.class );   
           
         String fileContent=response.getEntity(String.class);                    
       // System.out.println(fileContent);
        response.close(); 
     GsonBuilder builder = new GsonBuilder();
   
     builder.registerTypeAdapter(User[].class, new JsonDeserializer<User[]>(){
               
                
            @Override
            public User[] deserialize(JsonElement je, Type type, JsonDeserializationContext jdc) throws JsonParseException {
                
               List<User> s=new ArrayList<>();
               // System.out.println(je.getAsJsonObject().get("data").getAsJsonArray().get(1));
               // JsonArray coordinatesArray = fields.getAsJsonArray();
                
                for (int i=0; i<je.getAsJsonObject().get("data").getAsJsonArray().size();i++)
                {
                     
                     String[] parts = je.getAsJsonObject().get("data").getAsJsonArray().get(i).toString().replace("\"", "").replace("[", "").replace("]", "").split(","); 
                    try {
                        s.add(new User(parts[0],parts[1],parts[2],new URI(BASE_URI+"node/"+parts[3]),parts[4] ));
                        
                        
                    } catch (URISyntaxException ex) {
                        Logger.getLogger(UserRepo.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }

                return   s.toArray(new User[s.size()]) ;
            }
        });     
        Gson gson = builder.create();
             
         User[] R = gson.fromJson(fileContent, User[].class);
         return R;

    }
    public static User[] GetFollowed(User S) throws URISyntaxException    { 
       URI fromUri = new URI( "http://localhost:7474/db/data/cypher" );
      
        WebResource resource = Client.create()
                    .resource( fromUri );
      String cypher= "{\n" +
                        "  \"query\" : \" MATCH (n:user{ Pseudo: '"+S.getPseudo()+"'})<-[labels:Follows]-(m) RETURN m.name, m.LastName, m.Email, id(m), m.Pseudo  \",\n" +
                        "  \"params\" : {\n" +  
                        "  }\n" +
                        "}"; ;
        
       

        // POST JSON to the relationships URI
        ClientResponse response = resource.accept( MediaType.APPLICATION_JSON )
                                    .type( MediaType.APPLICATION_JSON )
                                    .entity(cypher)
                                    .post( ClientResponse.class );   
           
         String fileContent=response.getEntity(String.class);                    
       // System.out.println(fileContent);
        response.close(); 
     GsonBuilder builder = new GsonBuilder();
   
     builder.registerTypeAdapter(User[].class, new JsonDeserializer<User[]>(){
               
                
            @Override
            public User[] deserialize(JsonElement je, Type type, JsonDeserializationContext jdc) throws JsonParseException {
                
               List<User> s=new ArrayList<>();
               // System.out.println(je.getAsJsonObject().get("data").getAsJsonArray().get(1));
               // JsonArray coordinatesArray = fields.getAsJsonArray();
                
                for (int i=0; i<je.getAsJsonObject().get("data").getAsJsonArray().size();i++)
                {
                     
                     String[] parts = je.getAsJsonObject().get("data").getAsJsonArray().get(i).toString().replace("\"", "").replace("[", "").replace("]", "").split(","); 
                    try {
                        s.add(new User(parts[0],parts[1],parts[2],new URI(BASE_URI+"node/"+parts[3]),parts[4] ));
                        
                        
                    } catch (URISyntaxException ex) {
                        Logger.getLogger(UserRepo.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }

                return   s.toArray(new User[s.size()]) ;
            }
        });     
        Gson gson = builder.create();
             
         User[] R = gson.fromJson(fileContent, User[].class);
         return R;

    }


}
