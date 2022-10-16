package org.steamweb.models;
import com.google.gson.Gson;

public class Item {
    public String assetid;
    public String amount;
    public String icon;
    public String name;
    public String type;
    public boolean tradable;
    public String contextId;

    public String toString(){
        Gson gson = new Gson();
        return gson.toJson(this);
    }
}
