package com.zcsoft.rc.analysis.rc.service.impl;

import com.zcsoft.rc.analysis.rc.model.entity.Coordinates;
import com.zcsoft.rc.analysis.rc.service.CoordinatesService;
import org.springframework.stereotype.Service;

@Service
public class CoordinatesServiceImpl implements CoordinatesService {

    @Override
    public boolean isIn(Double longitude, Coordinates startCoordinates, Coordinates endCoordinates) {
        if(longitude > endCoordinates.getLongitude() && longitude < startCoordinates.getLongitude()) {
            return true;
        }

        if(longitude >startCoordinates.getLongitude() && longitude < endCoordinates.getLongitude()) {
            return true;
        }

        return false;
    }

}
