package com.justyn.travelmap.model;

/**
 * 通用的 Feed 条目，供首页/商城/预订复用。
 */
public class FeedItem {
    private final long id;
    private final String title;
    private final String description;
    private final String imageUrl;

    public FeedItem(long id, String title, String description, String imageUrl) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.imageUrl = imageUrl;
    }

    public long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getImageUrl() {
        return imageUrl;
    }
}
