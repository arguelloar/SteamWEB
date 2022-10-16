package org.steamweb.models;

public enum SetPrivacy {
    PRIVATE("1"),
    FRIENDS_ONLY("2"),
    PUBLIC("3");

    private String privacy;

    SetPrivacy(String privacy) {
        this.privacy = privacy;
    }

    public String toString() {
        return privacy;
    }
}
