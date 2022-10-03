package org.steamweb.types;
import com.google.gson.Gson;
import org.apache.commons.codec.binary.Hex;
import java.security.SecureRandom;

public class Session {
    private String steamCookie;
    private String sessionid;
    private String steamId;
    private String browserId;


    public String getBrowserId() {
        return this.browserId;
    }
    public void setBrowserId(String browserId) {
        this.browserId = browserId;
    }
    //Generates sessionId
    public void generateSessionId(){
        byte[] resBuf = new byte[12];
        new SecureRandom().nextBytes(resBuf);
        this.sessionid = Hex.encodeHexString(resBuf);
    }
    public String getSessionid() {
        return this.sessionid;
    }
    public void setSessionid(String sessionid){
        this.sessionid = sessionid;

    }
    public void setSteamCookie(String cookie) {
        this.steamCookie = cookie;
    }
    //Info getters.
    public String getSteamCookie() {
        return this.steamCookie;
    }


    public String getSteamId() {
        return this.steamId;
    }

    public void setSteamId(String steamId) {
        this.steamId = steamId;
    }

    //Return Session Information
    public String toString() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }
}


