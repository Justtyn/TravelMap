package com.justyn.travelmap.model;

/**
 * 通用的 Feed 条目，供首页/商城/预订复用。
 */
public class FeedItem {
    private final long id;
    private final String title;
    private final String description;
    private final String imageUrl;
    private final String priceLabel;
    private final String extraInfo;
    private final String address;
    private final Double latitude;
    private final Double longitude;
    private final Integer stock;

    public FeedItem(long id, String title, String description, String imageUrl) {
        this(id, title, description, imageUrl, null, null, null, null, null, null);
    }

    public FeedItem(long id,
                    String title,
                    String description,
                    String imageUrl,
                    String priceLabel,
                    String extraInfo,
                    String address,
                    Double latitude,
                    Double longitude,
                    Integer stock) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.imageUrl = imageUrl;
        this.priceLabel = priceLabel;
        this.extraInfo = extraInfo;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.stock = stock;
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

    public String getPriceLabel() {
        return priceLabel;
    }

    public String getExtraInfo() {
        return extraInfo;
    }

    public String getAddress() {
        return address;
    }

    public Double getLatitude() {
        return latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public Integer getStock() {
        return stock;
    }
}
