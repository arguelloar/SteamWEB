package org.steamweb;
import org.steamweb.types.Item;

public interface ISteamWEB {
    public void setSession(String session);
    public String login(String token);
    public void logout();
    public void getFarmableGames();
    public Item[] getCardsInventory();
    public String changeAvatar();
    public void clearAliases();
    public void changePrivacy();
}