/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tweets.tweet;

import java.net.URI;
import java.util.Calendar;

/**
 *
 * @author chaaben mohamed
 */
public class Tweet {

    String mMessage;
    URI mUri;

    
    public Tweet(String mMessage, URI mUri) {
        this.mMessage = mMessage;
        this.mUri = mUri;
    }

    @Override
    public String toString() {
        return "{" + "Message:" + mMessage+"}";
    }

    
    
    
 

    public String getmMessage() {
        return mMessage;
    }

    public void setmMessage(String mMessage) {
        this.mMessage = mMessage;
    }

    public URI getUri() {
        return mUri;
    }

    public void setUri(URI mUri) {
        this.mUri = mUri;
    }
    
    
}
