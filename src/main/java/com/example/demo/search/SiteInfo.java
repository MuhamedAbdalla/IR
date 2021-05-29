package com.example.demo.search;

public class SiteInfo {
    private int id;
    private String url;
    private String content;

    public SiteInfo(String url, String content) {
        this.url = url;
        this.content = content;
        this.id = -1;
    }

    public SiteInfo(String url, String content, int id) {
        this.url = url;
        this.content = content;
        this.id = id;
    }

    public String getUrl() {
        return this.url;
    }

    public String getContent() {
        return this.content;
    }

    public int getId() {
        return id;
    }
}
