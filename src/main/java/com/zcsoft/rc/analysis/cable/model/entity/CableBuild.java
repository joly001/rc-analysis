package com.zcsoft.rc.analysis.cable.model.entity;

import java.util.LinkedList;

public class CableBuild {

    private String id;
    private int cableLimitTime;
    private LinkedList<CoordinateDate> coordinatesLinkedList;

    public CableBuild(String id, Double longitude, Double latitude, int cableLimitTime) {
        this.id = id;
        this.coordinatesLinkedList = new LinkedList<>();
        this.cableLimitTime = cableLimitTime*60*1000;

        this.coordinatesLinkedList.add(new CoordinateDate(longitude, latitude));
    }

    synchronized public void addCoordinateDate(Double longitude, Double latitude) {
        CoordinateDate coordinateDate = new CoordinateDate(longitude, latitude);

        CoordinateDate firstCoordinateDate = coordinatesLinkedList.getFirst();

        if(!coordinateDate.isEqual(firstCoordinateDate)) {
            this.coordinatesLinkedList = new LinkedList<>();
            this.coordinatesLinkedList.add(coordinateDate);

            return;
        }

        CoordinateDate lastCoordinateDate = coordinatesLinkedList.getLast();

        if(lastCoordinateDate.greaterThan(cableLimitTime)) {
            coordinatesLinkedList.removeLast();
        }

        coordinatesLinkedList.addFirst(coordinateDate);
    }

    public boolean isNotMoved() {
        CoordinateDate firstCoordinateDate = coordinatesLinkedList.getFirst();
        CoordinateDate lastCoordinateDate = coordinatesLinkedList.getLast();

        if((firstCoordinateDate.getDate().getTime() - lastCoordinateDate.getDate().getTime()) > (this.cableLimitTime*0.8)) {
            return true;
        }

        return false;
    }
}
