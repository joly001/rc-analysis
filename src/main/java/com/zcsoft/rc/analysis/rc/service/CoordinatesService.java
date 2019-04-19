package com.zcsoft.rc.analysis.rc.service;

import com.zcsoft.rc.analysis.rc.model.entity.Coordinates;

public interface CoordinatesService {

    /**
     * 坐标是否在区间内
     * @param longitude
     * @param latitude
     * @param startCoordinates
     * @param endCoordinates
     * @return
     */
    boolean isIn(Double longitude, Double latitude, Coordinates startCoordinates, Coordinates endCoordinates);

}
