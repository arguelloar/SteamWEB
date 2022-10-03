package org.steamweb;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import org.apache.http.*;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.steamweb.types.*;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class SteamWEB implements ISteamWEB {
    private Session session = new Session();

    public void setSession(String session) {
        try {
            JsonObject json = new Gson().fromJson(session, JsonObject.class);
            this.session.setSteamId(json.get("steamId").toString());
            this.session.setSessionid(json.get("sessionId").toString());
            this.session.setSteamCookie(json.get("cookies").toString());
        } catch (Exception e) {
            throw new JsonSyntaxException("Not a session.");
        }

        // very login
    }

    /**
     * @param token refresh_token or access_token
     * @return
     */
    public String login(String token) {
        try {
            this.loginWithRefreshToken(token);
        } catch (Exception e) {
            throw new RuntimeException("Error logging in " + e);
        }
        return this.session.toString();
    }

    //Login with Refresh Token(2 year expire)
    private void loginWithRefreshToken(String token) throws Exception {
        this.session.generateSessionId();

        //Params for the post request
        List<NameValuePair> jsonParams = new ArrayList<>(2);
        jsonParams.add(new BasicNameValuePair("nonce", token));
        jsonParams.add(new BasicNameValuePair("sessionid", this.session.getSessionid()));
        jsonParams.add(new BasicNameValuePair("redir", "https://store.steampowered.com/login/?redir=&dir_ssl=1&snr=1_4_4__global-header"));
        //Start async client
        CloseableHttpAsyncClient httpclient = HttpAsyncClients.custom()
                .setDefaultRequestConfig(RequestConfig.custom().setCookieSpec(CookieSpecs.STANDARD).build()).build();
        try {
            httpclient.start();
            // Execute request
            HttpPost jsonRequest = new HttpPost("https://login.steampowered.com/jwt/finalizelogin");

            //We set parameters for the request
            jsonRequest.setEntity(new UrlEncodedFormEntity(jsonParams));
            //Getting response from server
            HttpResponse jsonResponse = httpclient.execute(jsonRequest, null).get();
            HttpEntity entity = jsonResponse.getEntity();

            //Converting response
            String json = EntityUtils.toString(entity, StandardCharsets.UTF_8);
            JSONObject setTokenJSON = new JSONObject(json);
            this.session.setSteamId(setTokenJSON.getString("steamID"));
            String auth = setTokenJSON.getJSONArray("transfer_info").getJSONObject(0).getJSONObject("params").getString("auth");
            String nonce = setTokenJSON.getJSONArray("transfer_info").getJSONObject(0).getJSONObject("params").getString("nonce");

            //Getting steam login cookie
            HttpPost cookieRequest = new HttpPost("https://store.steampowered.com/login/settoken");
            List<NameValuePair> cookieGetParams = new ArrayList<NameValuePair>(2);
            cookieGetParams.add(new BasicNameValuePair("nonce", nonce));
            cookieGetParams.add(new BasicNameValuePair("auth", auth));
            cookieGetParams.add(new BasicNameValuePair("steamID", this.session.getSteamId()));
            cookieRequest.setEntity(new UrlEncodedFormEntity(cookieGetParams));
            HttpResponse cookieResponse = httpclient.execute(cookieRequest, null).get();
            String cookie = cookieResponse.getFirstHeader("Set-Cookie").toString();
            this.session.setSteamCookie(StringUtils.substringBetween(cookie, "steamLoginSecure=", ";"));
            //Checking if we're logged in
            verifyLogin(authenticatedClient());
        } finally {
            httpclient.close();
        }

    }

    //Login with Access Token(2 days expire) not implemented yet...
    private void loginWithAcessToken(String token) {

    }

    //Verifying Log In
    private void verifyLogin(CloseableHttpAsyncClient client) throws Exception {
        //Request to GetNotificationCounts
        client.start();
        try{
            HttpGet req = new HttpGet("https://steamcommunity.com/actions/GetNotificationCounts");
            HttpResponse resp = client.execute(req, null).get();
            HttpEntity entity = resp.getEntity();
            String confirm = EntityUtils.toString(entity);
            //Checking if we're logged in
            if (confirm.contains("notifications")) {
                System.out.println("Logged in --> " + confirm);
            } else {
                System.out.println("Not logged in -->" + confirm);
            }
        }finally {
            client.close();
        }
    }


    public void logout() {
        this.session.setSteamId(null);
        this.session.setSteamCookie(null);
        this.session.setSessionid(null);
        this.session.setBrowserId(null);
        System.out.println("Log in information has been destroyed ==>" + this.session.toString());
    }



    public void getFarmableGames() {
        try {
            FarmableGame[] set = new FarmableGame[farmableGamesBody().select("a[href]").size()];
            List<FarmableGame> farmableGames = new ArrayList<>();
            int counter = 0;
            //Due to HTML containing differents attributes and elements gotta use a few If statements for each class attribute.
            for (Element element: farmableGamesBody().select("div.badge_row_inner")){
                if(element.select("span.progress_info_bold").toString().contains("No card drops remaining")) continue;
                if(element.select("span.progress_info_bold").toString().contains("tasks completed")) continue;
                if(!element.toString().contains("progress_info_bold")) continue;

                set[counter] = new FarmableGame();

                //Setting the name
                if(StringUtils.substringBetween(element.select("a[href]").toString(),"&quot;","&quot;") == null) {
                    String name = element.select("div.badge_title").toString();
                    set[counter].setName(StringUtils.substringBetween(name,"\n "," &nbsp;"));
                }else{
                    String name = element.select("a[href]").toString();
                    set[counter].setName(StringUtils.substringBetween(name,"&quot;","&quot;"));
                }
                //Setting the appid
                set[counter].appId = Integer.parseInt(
                        StringUtils.substringBetween(element.toString(),"gamebadge_","_"));

                //Setting playtime
                if(StringUtils.substringBetween(element.select("div.badge_title_stats_playtime").toString(),"; "," hrs") == null){
                    set[counter].playTime = 0;
                }else{
                    String playTime = StringUtils.substringBetween(element.select("div.badge_title_stats_playtime").toString(),"; "," hrs");
                    set[counter].playTime = Double.parseDouble(playTime);
                }

                //Setting cards dropped
                set[counter].droppedCards = Integer.parseInt(
                        StringUtils.substringBetween(
                                element.toString(),"Card drops received: ","\n"));

                //Setting remaining cards
                if (element.toString().contains("progress_info_bold")){
                    set[counter].remainingCards = Integer.parseInt(
                            StringUtils.substringBetween(
                                    element.toString(),"<span class=\"progress_info_bold\">"," card"));
                }else {
                    set[counter].remainingCards = Integer.parseInt(
                            StringUtils.substringBetween(
                                    element.toString(),"You can get "," more trading cards by playing"));
                }
                farmableGames.add(set[counter]);
                counter++;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    private Elements farmableGamesBody() throws Exception {
        CloseableHttpAsyncClient client = authenticatedClient();
        client.start();
        try{
            //Getting HTML body from badges page
            HttpGet farmableBody = new HttpGet("https://steamcommunity.com/id/"+getProfileName()+"/badges");
            HttpResponse resp = client.execute(farmableBody, null).get();
            HttpEntity respEntity = resp.getEntity();
            String body = EntityUtils.toString(respEntity);
            Document farmableGamesBody = Jsoup.parse(body);
            Elements parsedBody = farmableGamesBody.select("div.badges_sheet");
            return parsedBody;
        }finally {
            client.close();
        }
    }


    public Item[] getCardsInventory(){
        try {
            //Getting JSON
            JSONObject body = cardsJSON();

            //This one for amount of cards
            JSONObject cardAmount = body.getJSONObject("rgInventory");
            HashMap<String,Integer> classID = new HashMap<>();
            //Iterating for amount of cards for each class id
            for (Iterator i = cardAmount.keySet().iterator(); i.hasNext();){
                //id will be every Object from JSON
                String key = (String) i.next();
                JSONObject id = cardAmount.getJSONObject(key);

                //We assign a classid as the key value of the hashmap
                String hash = id.get("classid").toString();
                int amount = Integer.parseInt(id.get("amount").toString());

                //We check if the classid repeats and we imcrement
                if(classID.containsKey(hash)){
                    amount = classID.get(hash)+1;
                }
                classID.put(hash,amount);
            }
            List<Item> items = new ArrayList<>();

            //This one is for cards info
            JSONObject cards = body.getJSONObject("rgDescriptions");
            //Iterating for each card info
            for (Iterator i = cards.keySet().iterator(); i.hasNext ();){
                String key = (String) i.next ();
                JSONObject val = (JSONObject) cards.get(key);
                if(val.get("type").toString().contains("Profile") ||
                        val.get("type").toString().contains("Emoticon")) continue;
                Item card = new Item();

                //Getting amount of cards from HashMap
                String id = val.get("classid").toString();
                card.amount = classID.get(id).toString();

                //Every other Item attribute
                card.icon = val.get("icon_url").toString();
                card.type = val.get("type").toString();
                card.assetid = val.get("market_fee_app").toString();
                card.name = val.get("name").toString();
                if(val.get("tradable").toString() == "0"){
                    card.tradable = false;
                }else {
                    card.tradable = true;
                }
                if(!val.keySet().contains("owner_actions")) continue;
                if(val.get("owner_actions") == null) continue;
                String contextID = val.getJSONArray("owner_actions").toString();
                card.contextId = StringUtils.substringBetween(contextID,""+card.assetid+", ",",");
                if(card.contextId == null) continue;
                items.add(card);
            }
            Item[] itemsArray = items.toArray(new Item[items.size()]);
            return itemsArray;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private JSONObject cardsJSON()throws Exception{
        CloseableHttpAsyncClient client = authenticatedClient();
        client.start();
        try {
            //Just getting the JSON Response
            HttpGet request = new HttpGet("https://steamcommunity.com/id/"+this.getProfileName()+"/inventory/json/753/6/");
            HttpResponse response = client.execute(request, null).get();
            HttpEntity ent = response.getEntity();
            JSONObject invJSON = new JSONObject(EntityUtils.toString(ent));
            return invJSON;
        }finally {
            client.close();
        }
    }


    public String changeAvatar(){
        CloseableHttpAsyncClient client = authenticatedClient();
        client.start();
        try {
            File avatar = new File("C:\\Users\\Gonna\\Desktop\\test.png");
            FileBody avatarBody = new FileBody(avatar, ContentType.DEFAULT_BINARY);
            HttpPost req = new HttpPost("https://steamcommunity.com/actions/FileUploader/");
            StringBody steamId = new StringBody(this.session.getSteamId(), ContentType.MULTIPART_FORM_DATA);
            StringBody type = new StringBody("player_avatar_image", ContentType.MULTIPART_FORM_DATA);
            MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create();
            entityBuilder.addPart("avatar", avatarBody);
            entityBuilder.addPart("sId",steamId);
            entityBuilder.addPart("type",type);
            HttpEntity entity = entityBuilder.build();
            req.setEntity(entity);
            return null;
        }finally{

        }
    }

    private String getProfileName()throws Exception{
        CloseableHttpAsyncClient client = authenticatedClient();
        client.start();
        try{
            HttpGet steamProfileReq = new HttpGet("https://steamcommunity.com/profiles/"+this.session.getSteamId()+"/");
            HttpResponse steamProfileResp = client.execute(steamProfileReq,null).get();
            HttpEntity entity = steamProfileResp.getEntity();
            String body = EntityUtils.toString(entity);
            return StringUtils.substringBetween(body,"href=\"https://steamcommunity.com/id/","/");
        }finally {
            client.close();
        }
    }


    public void clearAliases() {

    }


    public void changePrivacy() {

    }

    private CloseableHttpAsyncClient authenticatedClient() {
        BasicCookieStore Cookies = new BasicCookieStore();
        BasicClientCookie steamCookie = new BasicClientCookie("steamLoginSecure", this.session.getSteamCookie());
        steamCookie.setDomain("steamcommunity.com");
        steamCookie.setPath("/");
        Cookies.addCookie(steamCookie);
        CloseableHttpAsyncClient client = HttpAsyncClients.custom()
                .setDefaultRequestConfig(RequestConfig.custom().
                        setCookieSpec(CookieSpecs.STANDARD).build()).
                setDefaultCookieStore(Cookies).build();
        return client;
    }
}


