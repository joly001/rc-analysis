package com.zcsoft.rc.analysis.rc.service.impl;

import com.zcsoft.rc.analysis.rc.model.entity.Coordinates;
import com.zcsoft.rc.analysis.rc.service.CoordinatesService;
import org.springframework.stereotype.Service;

@Service
public class CoordinatesServiceImpl implements CoordinatesService {

    @Override
    public boolean isIn(Double longitude, Double latitude, Coordinates startCoordinates, Coordinates endCoordinates) {
        if(longitude > endCoordinates.getLongitude() && longitude < startCoordinates.getLongitude()) {
            return true;
        }

        if(longitude >startCoordinates.getLongitude() && longitude < endCoordinates.getLongitude()) {
            return true;
        }

        if(latitude > endCoordinates.getLatitude() && latitude < startCoordinates.getLatitude()) {
            return true;
        }

        if(latitude > startCoordinates.getLatitude() && latitude < endCoordinates.getLatitude()) {
            return true;
        }

        return false;
    }

}
