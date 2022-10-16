package org.steamweb.models;
import org.steamweb.models.Item;

public interface ISteamWEB {
    public void setSession(String session);
    public String login(String token);
    public void logout();
    public void getFarmableGames();
    public Item[] getCardsInventory();
    public String changeAvatar(String url) throws Exception;
    public void clearAliases() throws Exception;
    public void changePrivacy(SetPrivacy privacy) throws Exception;
}