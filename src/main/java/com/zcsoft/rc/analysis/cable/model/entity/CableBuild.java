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

    public void addCoordinateDate(Double longitude, Double latitude) {
        CoordinateDate coordinateDate = new CoordinateDate(longitude, latitude);

        CoordinateDate lastCoordinateDate = coordinatesLinkedList.getLast();

        if(lastCoordinateDate.greaterThan(cableLimitTime)) {
            coordinatesLinkedList.removeLast();
        }

        coordinatesLinkedList.addFirst(coordinateDate);
    }

    protected boolean isNotMoved(int currentIndex) {
        if(currentIndex+1 >= coordinatesLinkedList.size()) {
            return true;
        }

        CoordinateDate currentCoordinateDate = coordinatesLinkedList.get(currentIndex);
        CoordinateDate previousCoordinateDate = coordinatesLinkedList.get(currentIndex+1);

        if(currentCoordinateDate.isEqual(previousCoordinateDate)) {
            return isNotMoved(currentIndex++);
        } else {
            return false;
        }
    }

    public boolean isNotMoved() {
        CoordinateDate firstCoordinateDate = coordinatesLinkedList.getFirst();
        CoordinateDate lastCoordinateDate = coordinatesLinkedList.getLast();

        if((firstCoordinateDate.getDate().getTime() - lastCoordinateDate.getDate().getTime()) < (cableLimitTime*0.8)) {
            return false;
        }

        return isNotMoved(0);
    }
}
