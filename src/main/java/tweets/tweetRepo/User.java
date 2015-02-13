/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tweets.tweet;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.ws.rs.core.MediaType;
import org.apache.lucene.util.IOUtils;

/**
 *
 * @author chaaben mohamed
 */
public class User {
    private String mName,mLast,mMail;
    private URI mUri;
    private String mPseudo;
    //private List<Tweet> mTweets=new ArrayList<Tweet>();

    public User(String mName, String mLast, String mMail, URI uri, String pseudo) {
        this.mName = mName;
        this.mLast = mLast;
        this.mMail = mMail;
        this.mUri = uri;
        this.mPseudo = pseudo;
    }

    public String getPseudo() {
        return mPseudo;
    }
   

    public String getName() {
        return mName;
    }

    public void setName(String mName) {
        this.mName = mName;
    }

    public String getLast() {
        return mLast;
    }

    public void setLast(String mLast) {
        this.mLast = mLast;
    }

    public String getMail() {
        return mMail;
    }

    public void setMail(String mMail) {
        this.mMail = mMail;
    }

    public URI getUri() {
        return mUri;
    }

    public void setUri(URI uri) {
        this.mUri = uri;
    }

    @Override
    public String toString() {
        return "User{" + "mName=" + mName + ", mLast=" + mLast + ", mMail=" + mMail + ", uri=" + mUri + ", mPseudo=" + mPseudo + '}';
    }
    
    
    
    
    public void addTweet(Tweet t, String relationshipType, String jsonAttributes) throws URISyntaxException
    {
         URI fromUri = new URI( mUri + "/relationships" );
    String relationshipJson = generateJsonRelationship( t.getUri(),
            relationshipType, jsonAttributes );

    WebResource resource = Client.create()
            .resource( fromUri );
    // POST JSON to the relationships URI
    ClientResponse response = resource.accept( MediaType.APPLICATION_JSON )
                            .type( MediaType.APPLICATION_JSON )
                            .entity( relationshipJson )
                            .post( ClientResponse.class );

    final URI location = response.getLocation();
    System.out.println( String.format(
            "POST to [%s], status code [%d], location header [%s]",
            fromUri, response.getStatus(), location.toString() ) );

    response.close();
    }
  
   private static String generateJsonRelationship( URI endNode,
            String relationshipType, String... jsonAttributes )
    {
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

  

}