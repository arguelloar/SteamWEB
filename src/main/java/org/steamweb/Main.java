package org.steamweb;

import org.apache.http.config.ConnectionConfig;
import org.steamweb.models.SetPrivacy;

import java.util.Set;

public class Main {
    public static void main(String[] args) throws Exception {
        SteamWEB web = new SteamWEB();
        web.login("eyAidHlwIjogIkpXVCIsICJhbGciOiAiRWREU0EiIH0.eyAiaXNzIjogInN0ZWFtIiwgInN1YiI6ICI3NjU2MTE5ODA2ODEyNjc2OSIsICJhdWQiOiBbICJ3ZWIiLCAicmVuZXciLCAiZGVyaXZlIiBdLCAiZXhwIjogMTY4Mzc2MjE4MSwgIm5iZiI6IDE2NTY5NjIzMDMsICJpYXQiOiAxNjY1NjAyMzAzLCAianRpIjogIjBDOTVfMjE2RjlDREFfNDRFRjgiLCAib2F0IjogMTY2NTYwMjMwMywgInBlciI6IDEsICJpcF9zdWJqZWN0IjogIjIwMS4xNDMuMTA4LjI0NyIsICJpcF9jb25maXJtZXIiOiAiMjAxLjE0My4xMDguMjQ3IiB9.rzKh2HXBK6cTVNbjQE_nNz4dRKVi277QksZp88o2XCH9KpxRQEyHFginCNMwwT3YtIl-mNxGDhXULYmZH0gbBA");

    }

}
