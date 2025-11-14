package com.justyn.travelmap.model;

public class VisitedRecord {
    private final long visitedId;
    private final int rating;
    private final String visitDate;

    public VisitedRecord(long visitedId, int rating, String visitDate) {
        this.visitedId = visitedId;
        this.rating = rating;
        this.visitDate = visitDate;
    }

    public long getVisitedId() {
        return visitedId;
    }

    public int getRating() {
        return rating;
    }

    public String getVisitDate() {
        return visitDate;
    }
}
