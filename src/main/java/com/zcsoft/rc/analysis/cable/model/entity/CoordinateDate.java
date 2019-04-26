package com.zcsoft.rc.analysis.cable.model.entity;

import com.zcsoft.rc.analysis.rc.model.entity.Coordinates;

import java.util.Date;

public class CoordinateDate {

    private Date date;
    private Coordinates coordinates;

    public CoordinateDate(Double longitude, Double latitude) {
        this.date = new Date();
        this.coordinates = new Coordinates(longitude, latitude);
    }

    public Coordinates getCoordinates() {
        return coordinates;
    }

    public Date getDate() {
        return date;
    }

    public boolean greaterThan(int cableLimitTime) {
        Date currentDate = new Date();

        return (currentDate.getTime()-date.getTime()-cableLimitTime >0);
    }

    public boolean isEqual(CoordinateDate coordinateDate) {
        if((coordinates.getLongitude() - coordinateDate.getCoordinates().getLongitude()) != 0) {
            return false;
        }
        if((coordinates.getLatitude() - coordinateDate.getCoordinates().getLatitude()) != 0) {
            return false;
        }

        return true;
    }

}
