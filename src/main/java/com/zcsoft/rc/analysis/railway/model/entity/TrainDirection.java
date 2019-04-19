package com.zcsoft.rc.analysis.railway.model.entity;

import com.zcsoft.rc.analysis.rc.model.entity.Coordinates;
import com.zcsoft.rc.warning.model.entity.TrainWarning;

public class TrainDirection {

    private String id;
    private String direction;
    private Coordinates[] coordinatesHeap;

    private boolean isBeginLessThanEnd;

    private int up;
    private int down;

    public TrainDirection(String id, Coordinates startCoordinates, Coordinates endCoordinates) {
        this.id = id;
        cleanDirection();
        coordinatesHeap = new Coordinates[5];

        if(startCoordinates.getLongitude() < endCoordinates.getLongitude()) {
            isBeginLessThanEnd = true;
        } else {
            isBeginLessThanEnd = false;
        }
    }

    protected void cleanDirection() {
        up = 0;
        down = 0;
    }

    public String getId() {
        return id;
    }

    public String getDirection() {
        return direction;
    }

    protected void decideDirection() {
        if (up > down) {
            direction = TrainWarning.DIRECTION_UP;
        }

        if(up < down){
            direction = TrainWarning.DIRECTION_DOWN;
        }

        cleanDirection();
    }

    public void addCoordinates(Coordinates coordinates) {
        for(int i=coordinatesHeap.length-1; i>0; i--) {
            coordinatesHeap[i] = coordinatesHeap[i-1];
        }
        coordinatesHeap[0] = coordinates;


        for(int i = 0; i<coordinatesHeap.length-2; i++) {
            Coordinates currentCoordinates = coordinatesHeap[i];
            Coordinates previousCoordinates = coordinatesHeap[i+1];

            if(previousCoordinates == null) {
                decideDirection();
                return;
            }

            if(currentCoordinates.getLongitude() > previousCoordinates.getLongitude() && isBeginLessThanEnd) {
                down++;

                continue;
            }

            if(!(currentCoordinates.getLongitude() > previousCoordinates.getLongitude()) && !isBeginLessThanEnd) {
                down++;

                continue;
            }

            up++;
        }

        decideDirection();
    }

    public Coordinates getLastCoordinates() {
        return coordinatesHeap[0];
    }
}
