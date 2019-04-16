package com.zcsoft.rc.analysis.warning.service;


import com.sharingif.cube.support.service.base.IBaseService;
import com.zcsoft.rc.collectors.api.rc.entity.CurrentRcRsp;
import com.zcsoft.rc.warning.model.entity.TrainWarning;

import java.util.List;


public interface TrainWarningService extends IBaseService<TrainWarning, String> {

    /**
     * 添加列车临站警告
     * @param id
     * @param longitude
     * @param latitude
     * @param direction
     * @param railwayLinesId
     * @param railwayLinesName
     * @param currentRcRspList
     */
    void addTemporaryStationWarning(String id, Double longitude, Double latitude, String direction, String railwayLinesId, String railwayLinesName, List<CurrentRcRsp> currentRcRspList);

    /**
     * 关闭列车临站警告
     * @param id
     */
    void finishTemporaryStationWarning(String id);

    /**
     * 添加列车接近警告
     * @param id
     * @param type
     * @param longitude
     * @param latitude
     * @param direction
     * @param workSegmentId
     * @param workSegmentName
     */
    void addTrainApproachingWarning(String id, String type, Double longitude, Double latitude, String direction, String workSegmentId, String workSegmentName);

    /**
     * 关闭列车接近警告
     * @param id
     */
    void finishTrainApproachingWarning(String id);

	
}
