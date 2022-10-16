package org.steamweb.models;
import com.google.gson.Gson;

public class FarmableGame {
    public String name;
    public int appId;
    public double playTime;
    public int remainingCards;
    public int droppedCards;
    public static String convertSpecialCharacters(String source) {
        String converted = source.replaceAll( "\\\\u00B0", "(o)" ).replaceAll( "\\\\u00a9", "(C)" ).replaceAll(  "\\\\u00AE","(R)" ).replaceAll( "\\\\u2122", "\\(TM\\)");
        return converted;
    }

    public void setName(String name) {
        this.name = convertSpecialCharacters(name);
    }

    public String toString() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }
}
