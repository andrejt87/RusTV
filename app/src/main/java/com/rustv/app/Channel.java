package com.rustv.app;

import com.google.gson.annotations.SerializedName;

public class Channel {
    @SerializedName("title")
    public String title;

    @SerializedName("category")
    public String category;

    @SerializedName("url")
    public String url;
}
