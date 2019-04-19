package com.zcsoft.rc.analysis.warning.service;


import com.sharingif.cube.support.service.base.IBaseService;
import com.zcsoft.rc.collectors.api.rc.entity.CurrentRcRsp;
import com.zcsoft.rc.mileage.model.entity.WorkSegment;
import com.zcsoft.rc.railway.model.entity.RailwayLines;
import com.zcsoft.rc.warning.model.entity.TrainWarning;

import java.util.List;
import java.util.Map;


public interface TrainWarningService extends IBaseService<TrainWarning, String> {

    /**
     * 添加列车临站警告
     * @param id
     * @param longitude
     * @param latitude
     * @param direction
     * @param railwayLines
     * @param currentRcRsp
     */
    void addTemporaryStationWarning(String id, Double longitude, Double latitude, String direction, RailwayLines railwayLines, CurrentRcRsp currentRcRsp);

    /**
     * 关闭列车临站警告
     * @param id
     */
    void finishTemporaryStationWarning(String id);

    /**
     * 添加列车接近警告
     * @param id
     * @param longitude
     * @param latitude
     * @param direction
     * @param workSegment
     * @param currentRcRsp
     */
    void addTrainApproachingWarning(String id, Double longitude, Double latitude, String direction, WorkSegment workSegment, CurrentRcRsp currentRcRsp);

    /**
     * 关闭列车接近警告
     * @param id
     * @param currentRcRspId
     */
    void finishTrainApproachingWarning(String id, String currentRcRspId);

	
}
